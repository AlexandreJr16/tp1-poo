package locadora.service;

import locadora.BancoTeste;
import locadora.model.Cliente;
import locadora.model.Exemplar;
import locadora.model.Funcionario;
import locadora.model.Locacao;
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

import static locadora.BancoTeste.assertRegra;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocacaoServiceTest {

    @TempDir
    Path dir;

    private BancoTeste banco;
    private LocacaoService servico;
    private Funcionario gerente;
    private Cliente ana;
    private Titulo matrix;
    private List<Exemplar> exemplares;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
        servico = banco.locacaoService;
        gerente = banco.logarGerente();
        ana = banco.cliente("Ana");
        matrix = banco.dvd("Matrix");
        exemplares = banco.adquirir(matrix, 4, gerente);
    }

    private int idDo(int indice) {
        return exemplares.get(indice).getId();
    }

    private Reserva reservaPronta(Cliente cliente, int indice) {
        Reserva reserva = Reserva.abrir(matrix, cliente, gerente, BancoTeste.HOJE);
        banco.reservas.inserir(reserva);
        Exemplar exemplar = banco.exemplares.buscar(idDo(indice));
        reserva.preparar(exemplar);
        banco.reservas.atualizar(reserva);
        banco.exemplares.atualizarSituacao(exemplar);
        return reserva;
    }

    @Test
    void locarRegistraOFuncionarioLogadoEOPrazoDoTitulo() {
        Funcionario atendente = banco.logarAtendente();

        Locacao locacao = servico.locar(ana.getId(), idDo(0));

        Locacao lida = servico.buscar(locacao.getId());
        assertEquals(atendente.getId(), lida.getFuncionarioLocacao().getId());
        assertEquals(LocalDate.of(2026, 10, 6), lida.getDataPrevista());
        assertEquals(new BigDecimal("8.00"), lida.getValorLocacao());
        assertEquals(Situacao.ALUGADO, lida.getExemplar().getSituacao());
        assertEquals(List.of(locacao.getId()), servico.ativas().stream().map(Locacao::getId).toList());
    }

    @Test
    void locarExigeLogin() {
        AuthService.sair();

        assertRegra("Faça login para continuar.", () -> servico.locar(ana.getId(), idDo(0)));
    }

    @Test
    void clienteInativoNaoAluga() {
        Cliente inativo = new Cliente(0, "Otavio", banco.cpf(), "", "", "", false);
        banco.clientes.inserir(inativo);

        assertRegra("Cliente inativo não pode alugar.", () -> servico.locar(inativo.getId(), idDo(0)));
    }

    @Test
    void clienteComMultaPendenteNaoAlugaAtePagar() {
        Locacao atrasada = servico.locar(ana.getId(), idDo(0));
        banco.relogio.avancar(8);
        servico.devolver(atrasada.getId());

        assertRegra("O cliente tem R$ 10,50 em multas pendentes. Registre o pagamento antes de alugar.",
                () -> servico.locar(ana.getId(), idDo(1)));

        servico.pagarMulta(atrasada.getId());
        servico.locar(ana.getId(), idDo(1));
        assertTrue(servico.comMultaPendente().isEmpty());
    }

    @Test
    void clienteAlugaNoMaximoTresAoMesmoTempo() {
        servico.locar(ana.getId(), idDo(0));
        servico.locar(ana.getId(), idDo(1));
        servico.locar(ana.getId(), idDo(2));

        assertRegra("O cliente já tem 3 locações em aberto.", () -> servico.locar(ana.getId(), idDo(3)));
    }

    @Test
    void naoAlugaExemplarAlugadoOuPerdido() {
        servico.locar(ana.getId(), idDo(0));
        Exemplar perdido = banco.exemplares.buscar(idDo(1));
        perdido.perder();
        banco.exemplares.atualizarSituacao(perdido);
        Cliente bruno = banco.cliente("Bruno");

        assertRegra("Este exemplar já está alugado.", () -> servico.locar(bruno.getId(), idDo(0)));
        assertRegra("Este exemplar foi dado como perdido.", () -> servico.locar(bruno.getId(), idDo(1)));
        assertRegra("Exemplar não encontrado.", () -> servico.locar(bruno.getId(), 999));
        assertRegra("Cliente não encontrado.", () -> servico.locar(999, idDo(2)));
    }

    @Test
    void exemplarReservadoSoSaiParaOClienteDaReserva() {
        Cliente bruno = banco.cliente("Bruno");
        Reserva reserva = reservaPronta(bruno, 0);

        assertRegra("Este exemplar está separado para a reserva de Bruno.", () -> servico.locar(ana.getId(), idDo(0)));

        servico.locar(bruno.getId(), idDo(0));
        assertEquals(SituacaoReserva.ATENDIDA, banco.reservas.buscar(reserva.getId()).getSituacao());
    }

    @Test
    void devolverNoPrazoNaoCobraMulta() {
        Locacao locacao = servico.locar(ana.getId(), idDo(0));
        banco.relogio.avancar(5);

        Locacao devolvida = servico.devolver(locacao.getId());

        assertEquals(new BigDecimal("0.00"), devolvida.getMulta());
        assertEquals(Situacao.DISPONIVEL, banco.exemplares.buscar(idDo(0)).getSituacao());
        assertTrue(servico.ativas().isEmpty());
    }

    @Test
    void previaDaMultaBateComAMultaDaDevolucao() {
        Locacao locacao = servico.locar(ana.getId(), idDo(0));
        banco.relogio.avancar(8);

        BigDecimal previa = servico.previaMulta(locacao.getId());
        Locacao devolvida = servico.devolver(locacao.getId());

        assertEquals(new BigDecimal("10.50"), previa);
        assertEquals(previa, devolvida.getMulta());
        assertEquals(LocalDate.of(2026, 10, 9), devolvida.getDataDevolucao());
    }

    @Test
    void devolucaoRegistraQuemDevolveuSeparadoDeQuemAlugou() {
        Funcionario atendente = banco.logarAtendente();
        Locacao locacao = servico.locar(ana.getId(), idDo(0));

        banco.logarGerente();
        servico.devolver(locacao.getId());

        Locacao lida = servico.buscar(locacao.getId());
        assertEquals(atendente.getId(), lida.getFuncionarioLocacao().getId());
        assertEquals(gerente.getId(), lida.getFuncionarioDevolucao().getId());
    }

    @Test
    void devolucaoSeparaOExemplarParaOPrimeiroDaFila() {
        Locacao locacao = servico.locar(ana.getId(), idDo(0));
        Reserva primeira = Reserva.abrir(matrix, banco.cliente("Bruno"), gerente, LocalDate.of(2026, 10, 1));
        Reserva segunda = Reserva.abrir(matrix, banco.cliente("Carla"), gerente, LocalDate.of(2026, 10, 2));
        banco.reservas.inserir(primeira);
        banco.reservas.inserir(segunda);

        servico.devolver(locacao.getId());

        assertEquals(Situacao.RESERVADO, banco.exemplares.buscar(idDo(0)).getSituacao());
        assertEquals(SituacaoReserva.PRONTA, banco.reservas.buscar(primeira.getId()).getSituacao());
        assertEquals(SituacaoReserva.AGUARDANDO, banco.reservas.buscar(segunda.getId()).getSituacao());
    }

    @Test
    void naoDevolveDuasVezesNemPagaSemMulta() {
        Locacao locacao = servico.locar(ana.getId(), idDo(0));
        servico.devolver(locacao.getId());

        assertRegra("Esta locação já foi devolvida.", () -> servico.devolver(locacao.getId()));
        assertRegra("Esta locação não tem multa pendente.", () -> servico.pagarMulta(locacao.getId()));
        assertRegra("Locação não encontrada.", () -> servico.devolver(999));
    }

    @Test
    void renovarEstendeOPrazoUmaVezSo() {
        Locacao locacao = servico.locar(ana.getId(), idDo(0));
        banco.relogio.avancar(2);

        Locacao renovada = servico.renovar(locacao.getId());

        assertEquals(LocalDate.of(2026, 10, 11), renovada.getDataPrevista());
        assertEquals(LocalDate.of(2026, 10, 11), servico.buscar(locacao.getId()).getDataPrevista());
        assertRegra("Esta locação já foi renovada uma vez.", () -> servico.renovar(locacao.getId()));
    }

    @Test
    void naoRenovaAtrasadaDevolvidaOuComFila() {
        Locacao atrasada = servico.locar(ana.getId(), idDo(0));
        Locacao comFila = servico.locar(ana.getId(), idDo(1));
        Locacao devolvida = servico.locar(ana.getId(), idDo(2));
        servico.devolver(devolvida.getId());
        banco.reservas.inserir(Reserva.abrir(matrix, banco.cliente("Bruno"), gerente, BancoTeste.HOJE));

        assertRegra("Há clientes na fila de reserva deste título.", () -> servico.renovar(comFila.getId()));
        assertRegra("Esta locação já foi devolvida.", () -> servico.renovar(devolvida.getId()));
        banco.relogio.avancar(6);
        assertRegra("Locação atrasada não pode ser renovada.", () -> servico.renovar(atrasada.getId()));
    }

    @Test
    void atrasadasDependemDoDiaDeHoje() {
        servico.locar(ana.getId(), idDo(0));
        assertTrue(servico.atrasadas().isEmpty());

        banco.relogio.avancar(6);

        assertEquals(1, servico.atrasadas().size());
        assertEquals(LocalDate.of(2026, 10, 7), servico.hoje());
        assertEquals(1, servico.listar().size());
    }

    @Test
    void alugaveisMostraDisponiveisEOsSeparadosParaOProprioCliente() {
        Cliente bruno = banco.cliente("Bruno");
        reservaPronta(bruno, 0);
        servico.locar(ana.getId(), idDo(1));

        List<String> paraAna = servico.alugaveisPara(ana.getId()).stream().map(Exemplar::getCodigo).toList();
        List<String> paraBruno = servico.alugaveisPara(bruno.getId()).stream().map(Exemplar::getCodigo).toList();

        assertEquals(List.of("T1-003", "T1-004"), paraAna);
        assertEquals(List.of("T1-001", "T1-003", "T1-004"), paraBruno);
        assertFalse(paraAna.contains("T1-002"));
    }

    @Test
    void formatoMostraDinheiroEDataNoPadraoBrasileiro() {
        assertEquals("R$ 1.234,50", Formato.dinheiro(new BigDecimal("1234.5")));
        assertEquals("R$ 0,00", Formato.dinheiro(BigDecimal.ZERO));
        assertEquals("01/10/2026", Formato.data(BancoTeste.HOJE));
        assertEquals("", Formato.data(null));
    }
}
