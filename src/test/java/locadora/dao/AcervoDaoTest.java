package locadora.dao;

import locadora.BancoTeste;
import locadora.model.Aquisicao;
import locadora.model.Cargo;
import locadora.model.Cliente;
import locadora.model.Estoque;
import locadora.model.Exemplar;
import locadora.model.Funcionario;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcervoDaoTest {

    @TempDir
    Path dir;

    private BancoTeste banco;
    private Funcionario gerente;
    private Titulo matrix;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
        gerente = banco.funcionario("admin", Cargo.GERENTE);
        matrix = banco.dvd("Matrix");
    }

    @Test
    void aquisicoesGeramCodigosSequenciaisPorTitulo() {
        banco.adquirir(matrix, 3, gerente);
        banco.adquirir(matrix, 2, gerente);
        banco.adquirir(banco.dvd("Duna"), 1, gerente);

        List<Exemplar> exemplares = banco.exemplares.listarPorTitulo(matrix.getId());

        assertEquals(List.of("T1-001", "T1-002", "T1-003", "T1-004", "T1-005"), exemplares.stream().map(Exemplar::getCodigo).toList());
        assertTrue(exemplares.stream().allMatch(e -> e.getId() > 0 && e.disponivel()));
        assertEquals(new BigDecimal("30.00"), exemplares.get(0).getCusto());
        assertEquals("Matrix", exemplares.get(0).getTitulo().getNome());
        assertEquals(6, banco.exemplares.proximaSequencia(matrix.getId()));
    }

    @Test
    void atualizarSituacaoPersisteEBuscarPorCodigoAcha() {
        Exemplar exemplar = banco.adquirir(matrix, 1, gerente).get(0);

        exemplar.alugar();
        banco.exemplares.atualizarSituacao(exemplar);

        assertEquals(Situacao.ALUGADO, banco.exemplares.buscar(exemplar.getId()).getSituacao());
        assertEquals(exemplar.getId(), banco.exemplares.buscarPorCodigo("T1-001").getId());
        assertNull(banco.exemplares.buscarPorCodigo("T9-999"));
    }

    @Test
    void estoqueContaExemplaresPorSituacaoDeCadaTitulo() {
        List<Exemplar> exemplares = banco.adquirir(matrix, 4, gerente);
        Titulo duna = banco.dvd("Duna");
        exemplares.get(0).alugar();
        exemplares.get(1).perder();
        exemplares.get(2).reservar();
        exemplares.subList(0, 3).forEach(banco.exemplares::atualizarSituacao);

        Map<Integer, Estoque> estoque = banco.exemplares.estoquePorTitulo();

        assertEquals(new Estoque(1, 1, 1, 1), estoque.get(matrix.getId()));
        assertEquals(4, estoque.get(matrix.getId()).total());
        assertEquals(3, estoque.get(matrix.getId()).noAcervo());
        assertNull(estoque.get(duna.getId()));
        assertEquals(List.of("T1-004"), banco.exemplares.listarPorSituacao(Situacao.DISPONIVEL).stream().map(Exemplar::getCodigo).toList());
    }

    @Test
    void aquisicaoGuardaTituloFornecedorCustoEFuncionario() {
        banco.adquirir(matrix, 2, gerente);

        Aquisicao aquisicao = banco.aquisicoes.listar().get(0);

        assertEquals("Matrix", aquisicao.getTitulo().getNome());
        assertEquals(2, aquisicao.getQuantidade());
        assertEquals(new BigDecimal("30.00"), aquisicao.getCustoUnitario());
        assertEquals("Distribuidora Teste", aquisicao.getFornecedor());
        assertEquals(BancoTeste.HOJE, aquisicao.getData());
        assertEquals("admin", aquisicao.getFuncionario().getLogin());
        Exemplar primeiro = banco.exemplares.listarPorTitulo(matrix.getId()).get(0);
        assertEquals(aquisicao.getId(), banco.aquisicoes.buscarDoExemplar(primeiro.getId()).getId());
    }

    @Test
    void aquisicaoDesfazTudoSeUmExemplarFalhar() {
        List<Exemplar> repetidos = List.of(
                new Exemplar(0, "T1-001", matrix, BigDecimal.TEN, Situacao.DISPONIVEL),
                new Exemplar(0, "T1-001", matrix, BigDecimal.TEN, Situacao.DISPONIVEL));
        Aquisicao aquisicao = new Aquisicao(0, matrix, 2, BigDecimal.TEN, "Distribuidora", BancoTeste.HOJE, gerente);

        assertThrows(BancoException.class, () -> banco.aquisicoes.registrar(aquisicao, repetidos, List.of()));

        assertTrue(banco.aquisicoes.listar().isEmpty());
        assertTrue(banco.exemplares.listarPorTitulo(matrix.getId()).isEmpty());
    }

    @Test
    void aquisicaoPreparaAsReservasQueEstavamNaFila() {
        Reserva reserva = Reserva.abrir(matrix, banco.cliente("Ana"), gerente, BancoTeste.HOJE);
        banco.reservas.inserir(reserva);
        Exemplar novo = new Exemplar(0, Exemplar.codigo(matrix.getId(), 1), matrix, BigDecimal.TEN, Situacao.DISPONIVEL);
        reserva.preparar(novo);

        banco.aquisicoes.registrar(new Aquisicao(0, matrix, 1, BigDecimal.TEN, "Distribuidora", BancoTeste.HOJE, gerente),
                List.of(novo), List.of(reserva));

        Reserva lida = banco.reservas.buscar(reserva.getId());
        assertEquals(SituacaoReserva.PRONTA, lida.getSituacao());
        assertEquals(novo.getId(), lida.getExemplar().getId());
        assertEquals(Situacao.RESERVADO, lida.getExemplar().getSituacao());
        assertEquals(reserva.getId(), banco.reservas.prontaDoExemplar(novo.getId()).getId());
    }

    @Test
    void reservaGuardaDadosEFilaSegueOrdemDeChegada() {
        Cliente ana = banco.cliente("Ana");
        Reserva terceira = reservar(ana, LocalDate.of(2026, 10, 3));
        Reserva primeira = reservar(banco.cliente("Bruno"), LocalDate.of(2026, 10, 1));
        Reserva segunda = reservar(banco.cliente("Carla"), LocalDate.of(2026, 10, 1));
        Reserva cancelada = reservar(banco.cliente("Davi"), LocalDate.of(2026, 9, 30));
        cancelada.cancelar();
        banco.reservas.atualizar(cancelada);

        List<Integer> fila = banco.reservas.fila(matrix.getId()).stream().map(Reserva::getId).toList();

        assertEquals(List.of(primeira.getId(), segunda.getId(), terceira.getId()), fila);
        Reserva lida = banco.reservas.buscar(terceira.getId());
        assertEquals("Ana", lida.getCliente().getNome());
        assertEquals("Matrix", lida.getTitulo().getNome());
        assertEquals("admin", lida.getFuncionario().getLogin());
        assertEquals(LocalDate.of(2026, 10, 3), lida.getData());
        assertNull(lida.getExemplar());
        assertEquals(List.of(terceira.getId()), banco.reservas.doCliente(ana.getId()).stream().map(Reserva::getId).toList());
        assertEquals(3, banco.reservas.ativas().size());
    }

    @Test
    void cancelamentoPassaOExemplarParaOProximoDaFila() {
        Exemplar exemplar = banco.adquirir(matrix, 1, gerente).get(0);
        Reserva primeira = reservar(banco.cliente("Ana"), LocalDate.of(2026, 10, 1));
        Reserva segunda = reservar(banco.cliente("Bruno"), LocalDate.of(2026, 10, 2));
        primeira.preparar(exemplar);
        banco.reservas.atualizar(primeira);
        banco.exemplares.atualizarSituacao(exemplar);

        primeira.cancelar();
        segunda.preparar(primeira.getExemplar());
        banco.reservas.registrarCancelamento(primeira, segunda);

        assertEquals(SituacaoReserva.CANCELADA, banco.reservas.buscar(primeira.getId()).getSituacao());
        assertEquals(SituacaoReserva.PRONTA, banco.reservas.buscar(segunda.getId()).getSituacao());
        assertEquals(Situacao.RESERVADO, banco.exemplares.buscar(exemplar.getId()).getSituacao());
        assertEquals(segunda.getId(), banco.reservas.prontaDoExemplar(exemplar.getId()).getId());
    }

    @Test
    void cancelamentoSemFilaLiberaOExemplar() {
        Exemplar exemplar = banco.adquirir(matrix, 1, gerente).get(0);
        Reserva reserva = reservar(banco.cliente("Ana"), LocalDate.of(2026, 10, 1));
        reserva.preparar(exemplar);
        banco.reservas.atualizar(reserva);
        banco.exemplares.atualizarSituacao(exemplar);

        reserva.cancelar();
        banco.reservas.registrarCancelamento(reserva, null);

        assertEquals(Situacao.DISPONIVEL, banco.exemplares.buscar(exemplar.getId()).getSituacao());
        assertNull(banco.reservas.prontaDoExemplar(exemplar.getId()));
    }

    private Reserva reservar(Cliente cliente, LocalDate data) {
        Reserva reserva = Reserva.abrir(matrix, cliente, gerente, data);
        banco.reservas.inserir(reserva);
        return reserva;
    }
}
