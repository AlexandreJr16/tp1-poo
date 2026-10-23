package locadora.dao;

import locadora.BancoTeste;
import locadora.model.Cargo;
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
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovimentoDaoTest {

    @TempDir
    Path dir;

    private BancoTeste banco;
    private Funcionario gerente;
    private Funcionario atendente;
    private Cliente ana;
    private Titulo matrix;
    private List<Exemplar> exemplares;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
        gerente = banco.funcionario("admin", Cargo.GERENTE);
        atendente = banco.funcionario("bruno", Cargo.ATENDENTE);
        ana = banco.cliente("Ana");
        matrix = banco.dvd("Matrix");
        exemplares = banco.adquirir(matrix, 2, gerente);
    }

    private Locacao locar(Exemplar exemplar, LocalDate data) {
        Locacao locacao = Locacao.abrir(exemplar, ana, atendente, data);
        banco.locacoes.registrar(locacao, null);
        return locacao;
    }

    @Test
    void registrarGravaLocacaoEMarcaExemplarAlugado() {
        Locacao locacao = locar(exemplares.get(0), BancoTeste.HOJE);

        Locacao lida = banco.locacoes.buscar(locacao.getId());
        assertEquals("T1-001", lida.getExemplar().getCodigo());
        assertEquals(Situacao.ALUGADO, lida.getExemplar().getSituacao());
        assertEquals("Ana", lida.getCliente().getNome());
        assertEquals("bruno", lida.getFuncionarioLocacao().getLogin());
        assertNull(lida.getFuncionarioDevolucao());
        assertEquals(BancoTeste.HOJE, lida.getDataLocacao());
        assertEquals(LocalDate.of(2026, 10, 6), lida.getDataPrevista());
        assertNull(lida.getDataDevolucao());
        assertEquals(new BigDecimal("8.00"), lida.getValorLocacao());
        assertEquals(new BigDecimal("0.00"), lida.getMulta());
        assertFalse(lida.isRenovada());
        assertEquals(locacao.getId(), banco.locacoes.ativaDoExemplar(exemplares.get(0).getId()).getId());
        assertNull(banco.locacoes.ativaDoExemplar(exemplares.get(1).getId()));
    }

    @Test
    void registrarAtendendoReservaMarcaAReservaComoAtendida() {
        Reserva reserva = Reserva.abrir(matrix, ana, atendente, BancoTeste.HOJE);
        banco.reservas.inserir(reserva);
        reserva.preparar(exemplares.get(0));
        banco.reservas.atualizar(reserva);
        banco.exemplares.atualizarSituacao(exemplares.get(0));

        Locacao locacao = Locacao.abrir(exemplares.get(0), ana, atendente, BancoTeste.HOJE);
        reserva.atender();
        banco.locacoes.registrar(locacao, reserva);

        assertEquals(SituacaoReserva.ATENDIDA, banco.reservas.buscar(reserva.getId()).getSituacao());
        assertEquals(Situacao.ALUGADO, banco.exemplares.buscar(exemplares.get(0).getId()).getSituacao());
    }

    @Test
    void devolucaoGravaMultaEFuncionarioEPreparaAProximaReserva() {
        Locacao locacao = locar(exemplares.get(0), BancoTeste.HOJE);
        Reserva reserva = Reserva.abrir(matrix, banco.cliente("Bruna"), atendente, BancoTeste.HOJE);
        banco.reservas.inserir(reserva);

        locacao.devolver(LocalDate.of(2026, 10, 9), gerente);
        reserva.preparar(locacao.getExemplar());
        banco.locacoes.registrarDevolucao(locacao, reserva);

        Locacao lida = banco.locacoes.buscar(locacao.getId());
        assertEquals(LocalDate.of(2026, 10, 9), lida.getDataDevolucao());
        assertEquals("admin", lida.getFuncionarioDevolucao().getLogin());
        assertEquals(new BigDecimal("10.50"), lida.getMulta());
        assertEquals(Situacao.RESERVADO, lida.getExemplar().getSituacao());
        assertEquals(reserva.getId(), banco.reservas.prontaDoExemplar(exemplares.get(0).getId()).getId());
        assertTrue(banco.locacoes.ativas().isEmpty());
    }

    @Test
    void atualizarGravaRenovacaoEPagamentoDeMulta() {
        Locacao renovada = locar(exemplares.get(0), BancoTeste.HOJE);
        Locacao atrasada = locar(exemplares.get(1), BancoTeste.HOJE);
        renovada.renovar(LocalDate.of(2026, 10, 3));
        atrasada.devolver(LocalDate.of(2026, 10, 8), gerente);
        banco.locacoes.registrarDevolucao(atrasada, null);
        assertEquals(List.of(atrasada.getId()), banco.locacoes.comMultaPendente().stream().map(Locacao::getId).toList());

        atrasada.pagarMulta(LocalDate.of(2026, 10, 10));
        banco.locacoes.atualizar(renovada);
        banco.locacoes.atualizar(atrasada);

        assertTrue(banco.locacoes.buscar(renovada.getId()).isRenovada());
        assertEquals(LocalDate.of(2026, 10, 11), banco.locacoes.buscar(renovada.getId()).getDataPrevista());
        assertEquals(LocalDate.of(2026, 10, 10), banco.locacoes.buscar(atrasada.getId()).getDataPagamentoMulta());
        assertTrue(banco.locacoes.comMultaPendente().isEmpty());
    }

    @Test
    void consultasPorClienteExemplarETodas() {
        Locacao primeira = locar(exemplares.get(0), LocalDate.of(2026, 10, 1));
        primeira.devolver(LocalDate.of(2026, 10, 2), atendente);
        banco.locacoes.registrarDevolucao(primeira, null);
        Locacao segunda = locar(exemplares.get(0), LocalDate.of(2026, 10, 3));
        Locacao outra = locar(exemplares.get(1), LocalDate.of(2026, 10, 4));

        assertEquals(List.of(primeira.getId(), segunda.getId()),
                banco.locacoes.doExemplar(exemplares.get(0).getId()).stream().map(Locacao::getId).toList());
        assertEquals(List.of(outra.getId(), segunda.getId(), primeira.getId()),
                banco.locacoes.doCliente(ana.getId()).stream().map(Locacao::getId).toList());
        assertEquals(List.of(outra.getId(), segunda.getId(), primeira.getId()),
                banco.locacoes.listar().stream().map(Locacao::getId).toList());
        assertEquals(List.of(segunda.getId(), outra.getId()),
                banco.locacoes.ativas().stream().map(Locacao::getId).toList());
    }

    @Test
    void faturamentoSomaLocacoesEMultasPagasNoPeriodo() {
        Locacao primeira = locar(exemplares.get(0), LocalDate.of(2026, 10, 1));
        locar(exemplares.get(1), LocalDate.of(2026, 10, 5));
        primeira.devolver(LocalDate.of(2026, 10, 9), atendente);
        primeira.pagarMulta(LocalDate.of(2026, 10, 10));
        banco.locacoes.registrarDevolucao(primeira, null);

        assertEquals(new BigDecimal("26.50"), banco.locacoes.faturamentoEntre(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)));
        assertEquals(new BigDecimal("10.50"), banco.locacoes.faturamentoEntre(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 31)));
        assertEquals(new BigDecimal("0.00"), banco.locacoes.faturamentoEntre(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)));
    }

    @Test
    void perdaDeExemplarAlugadoEncerraALocacaoComCobranca() {
        Locacao locacao = locar(exemplares.get(0), BancoTeste.HOJE);
        locacao.encerrarPorPerda(LocalDate.of(2026, 10, 8), gerente);

        banco.perdas.registrar(new Perda(0, locacao.getExemplar(), "Cliente perdeu o disco", LocalDate.of(2026, 10, 8), gerente, locacao), null);

        Perda perda = banco.perdas.listar().get(0);
        assertEquals("T1-001", perda.getExemplar().getCodigo());
        assertEquals(Situacao.PERDIDO, perda.getExemplar().getSituacao());
        assertEquals("Cliente perdeu o disco", perda.getMotivo());
        assertEquals(LocalDate.of(2026, 10, 8), perda.getData());
        assertEquals("admin", perda.getFuncionario().getLogin());
        assertEquals(locacao.getId(), perda.getLocacao().getId());
        assertEquals(new BigDecimal("37.00"), banco.locacoes.buscar(locacao.getId()).getMulta());
        assertEquals(perda.getId(), banco.perdas.doExemplar(exemplares.get(0).getId()).getId());
    }

    @Test
    void perdaDeExemplarReservadoDevolveAReservaParaAFila() {
        Reserva reserva = Reserva.abrir(matrix, ana, atendente, BancoTeste.HOJE);
        banco.reservas.inserir(reserva);
        reserva.preparar(exemplares.get(1));
        banco.reservas.atualizar(reserva);

        reserva.voltarParaFila();
        exemplares.get(1).perder();
        banco.perdas.registrar(new Perda(0, exemplares.get(1), "Caixa quebrada", BancoTeste.HOJE, gerente, null), reserva);

        assertEquals(SituacaoReserva.AGUARDANDO, banco.reservas.buscar(reserva.getId()).getSituacao());
        assertNull(banco.reservas.buscar(reserva.getId()).getExemplar());
        assertEquals(Situacao.PERDIDO, banco.exemplares.buscar(exemplares.get(1).getId()).getSituacao());
        assertNull(banco.perdas.listar().get(0).getLocacao());
        assertNull(banco.perdas.doExemplar(exemplares.get(0).getId()));
    }
}
