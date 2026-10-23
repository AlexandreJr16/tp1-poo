package locadora.service;

import locadora.BancoTeste;
import locadora.model.Aquisicao;
import locadora.model.Cliente;
import locadora.model.Exemplar;
import locadora.model.Funcionario;
import locadora.model.Locacao;
import locadora.model.Perda;
import locadora.model.Reserva;
import locadora.model.Situacao;
import locadora.model.SituacaoReserva;
import locadora.model.Titulo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcervoServiceTest {

    private static final BigDecimal CUSTO = new BigDecimal("25.00");

    @TempDir
    Path dir;

    private BancoTeste banco;
    private Funcionario gerente;
    private Titulo matrix;
    private Cliente ana;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
        gerente = banco.logarGerente();
        matrix = banco.dvd("Matrix");
        ana = banco.cliente("Ana");
    }

    private static void assertRegra(String mensagem, Executable acao) {
        assertEquals(mensagem, assertThrows(RegraException.class, acao).getMessage());
    }

    private List<Exemplar> exemplaresDaMatrix() {
        return banco.exemplares.listarPorTitulo(matrix.getId());
    }

    @Test
    void aquisicaoGeraExemplaresSequenciaisComCustoEFuncionario() {
        Aquisicao primeira = banco.aquisicaoService.registrar(matrix.getId(), 3, CUSTO, "  Distribuidora Norte ");
        banco.aquisicaoService.registrar(matrix.getId(), 2, CUSTO, "Distribuidora Norte");

        assertEquals(List.of("T1-001", "T1-002", "T1-003", "T1-004", "T1-005"), exemplaresDaMatrix().stream().map(Exemplar::getCodigo).toList());
        assertTrue(exemplaresDaMatrix().stream().allMatch(e -> e.disponivel() && e.getCusto().equals(CUSTO)));
        Aquisicao lida = banco.aquisicaoService.listar().get(1);
        assertEquals(primeira.getId(), lida.getId());
        assertEquals("Distribuidora Norte", lida.getFornecedor());
        assertEquals(gerente.getId(), lida.getFuncionario().getId());
        assertEquals(BancoTeste.HOJE, lida.getData());
        assertEquals(new BigDecimal("75.00"), lida.custoTotal());
    }

    @Test
    void soGerenteRegistraAquisicao() {
        banco.logarAtendente();

        assertRegra("Apenas gerentes podem fazer isso.", () -> banco.aquisicaoService.registrar(matrix.getId(), 1, CUSTO, "Distribuidora"));
    }

    @Test
    void aquisicaoInvalidaGeraMensagemAmigavel() {
        assertRegra("Título não encontrado.", () -> banco.aquisicaoService.registrar(999, 1, CUSTO, "Distribuidora"));
        assertRegra("A quantidade deve ser de 1 a 100.", () -> banco.aquisicaoService.registrar(matrix.getId(), 0, CUSTO, "Distribuidora"));
        assertRegra("A quantidade deve ser de 1 a 100.", () -> banco.aquisicaoService.registrar(matrix.getId(), 101, CUSTO, "Distribuidora"));
        assertRegra("Informe um custo unitário maior que zero.", () -> banco.aquisicaoService.registrar(matrix.getId(), 1, BigDecimal.ZERO, "Distribuidora"));
        assertRegra("Informe um custo unitário maior que zero.", () -> banco.aquisicaoService.registrar(matrix.getId(), 1, null, "Distribuidora"));
        assertRegra("Informe o fornecedor.", () -> banco.aquisicaoService.registrar(matrix.getId(), 1, CUSTO, " "));
        assertTrue(exemplaresDaMatrix().isEmpty());
    }

    @Test
    void aquisicaoAtendePrimeiroQuemEstaNaFila() {
        Reserva bruno = Reserva.abrir(matrix, banco.cliente("Bruno"), gerente, BancoTeste.HOJE);
        Reserva carla = Reserva.abrir(matrix, banco.cliente("Carla"), gerente, BancoTeste.HOJE);
        banco.reservas.inserir(bruno);
        banco.reservas.inserir(carla);

        banco.aquisicaoService.registrar(matrix.getId(), 3, CUSTO, "Distribuidora");

        assertEquals(List.of(Situacao.RESERVADO, Situacao.RESERVADO, Situacao.DISPONIVEL),
                exemplaresDaMatrix().stream().map(Exemplar::getSituacao).toList());
        assertEquals("T1-001", banco.reservas.buscar(bruno.getId()).getExemplar().getCodigo());
        assertEquals("T1-002", banco.reservas.buscar(carla.getId()).getExemplar().getCodigo());
        assertEquals(SituacaoReserva.PRONTA, banco.reservas.buscar(carla.getId()).getSituacao());
    }

    @Test
    void perdaDeExemplarDisponivelTiraDoAcervoParaSempre() {
        Exemplar exemplar = banco.adquirir(matrix, 1, gerente).get(0);

        Perda perda = banco.perdaService.registrar(exemplar.getId(), "Disco riscado");

        assertEquals(Situacao.PERDIDO, banco.exemplares.buscar(exemplar.getId()).getSituacao());
        Perda lida = banco.perdaService.listar().get(0);
        assertEquals(perda.getId(), lida.getId());
        assertEquals(gerente.getId(), lida.getFuncionario().getId());
        assertEquals(BancoTeste.HOJE, lida.getData());
        assertNull(lida.getLocacao());
        assertRegra("Este exemplar foi dado como perdido.", () -> banco.locacaoService.locar(ana.getId(), exemplar.getId()));
        assertRegra("Este exemplar já foi dado como perdido.", () -> banco.perdaService.registrar(exemplar.getId(), "De novo"));
    }

    @Test
    void perdaDeExemplarAlugadoEncerraALocacaoECobraCustoMaisMulta() {
        Exemplar exemplar = banco.adquirir(matrix, 1, gerente).get(0);
        Locacao locacao = banco.locacaoService.locar(ana.getId(), exemplar.getId());
        banco.relogio.avancar(7);
        assertEquals(new BigDecimal("37.00"), banco.perdaService.previaCobranca(exemplar.getId()));

        Perda perda = banco.perdaService.registrar(exemplar.getId(), "Cliente perdeu");

        Locacao encerrada = banco.locacaoService.buscar(locacao.getId());
        assertFalse(encerrada.ativa());
        assertEquals(new BigDecimal("37.00"), encerrada.getMulta());
        assertTrue(encerrada.multaPendente());
        assertEquals(locacao.getId(), perda.getLocacao().getId());
        assertEquals(new BigDecimal("37.00"), banco.clienteService.multasPendentes(ana.getId()));
        assertEquals(Situacao.PERDIDO, banco.exemplares.buscar(exemplar.getId()).getSituacao());
    }

    @Test
    void perdaDeExemplarReservadoDevolveAReservaParaAFila() {
        Reserva reserva = Reserva.abrir(matrix, ana, gerente, BancoTeste.HOJE);
        banco.reservas.inserir(reserva);
        banco.aquisicaoService.registrar(matrix.getId(), 1, CUSTO, "Distribuidora");
        Exemplar separado = exemplaresDaMatrix().get(0);
        assertEquals(new BigDecimal("0.00"), banco.perdaService.previaCobranca(separado.getId()));

        banco.perdaService.registrar(separado.getId(), "Caixa quebrada");

        Reserva lida = banco.reservas.buscar(reserva.getId());
        assertEquals(SituacaoReserva.AGUARDANDO, lida.getSituacao());
        assertNull(lida.getExemplar());
    }

    @Test
    void perdaInvalidaGeraMensagemAmigavel() {
        Exemplar exemplar = banco.adquirir(matrix, 1, gerente).get(0);

        assertRegra("Informe o motivo da perda.", () -> banco.perdaService.registrar(exemplar.getId(), "  "));
        assertRegra("Exemplar não encontrado.", () -> banco.perdaService.registrar(999, "Sumiu"));
        banco.logarAtendente();
        assertRegra("Apenas gerentes podem fazer isso.", () -> banco.perdaService.registrar(exemplar.getId(), "Sumiu"));
    }
}
