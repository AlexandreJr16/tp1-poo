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
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaServiceTest {

    @TempDir
    Path dir;

    private BancoTeste banco;
    private ReservaService servico;
    private Funcionario gerente;
    private Titulo matrix;
    private Exemplar exemplar;
    private Cliente ana;
    private Cliente bruno;
    private Locacao locacaoDoBruno;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
        servico = banco.reservaService;
        gerente = banco.logarGerente();
        matrix = banco.dvd("Matrix");
        exemplar = banco.adquirir(matrix, 1, gerente).get(0);
        ana = banco.cliente("Ana");
        bruno = banco.cliente("Bruno");
        locacaoDoBruno = banco.locacaoService.locar(bruno.getId(), exemplar.getId());
    }

    private static void assertRegra(String mensagem, Executable acao) {
        assertEquals(mensagem, assertThrows(RegraException.class, acao).getMessage());
    }

    @Test
    void reservarTituloIndisponivelEntraNaFila() {
        Funcionario atendente = banco.logarAtendente();

        Reserva reserva = servico.reservar(ana.getId(), matrix.getId());

        Reserva lida = banco.reservas.buscar(reserva.getId());
        assertEquals(SituacaoReserva.AGUARDANDO, lida.getSituacao());
        assertEquals(atendente.getId(), lida.getFuncionario().getId());
        assertEquals(BancoTeste.HOJE, lida.getData());
        assertEquals(1, servico.posicao(lida));
        assertEquals(List.of(reserva.getId()), servico.fila(matrix.getId()).stream().map(Reserva::getId).toList());
    }

    @Test
    void reservaInvalidaGeraMensagemAmigavel() {
        Titulo duna = banco.dvd("Duna");
        banco.adquirir(duna, 1, gerente);
        Cliente inativo = new Cliente(0, "Otavio", banco.cpf(), "", "", "", false);
        banco.clientes.inserir(inativo);
        servico.reservar(ana.getId(), matrix.getId());

        assertRegra("Há exemplar disponível deste título. Faça a locação direto.", () -> servico.reservar(ana.getId(), duna.getId()));
        assertRegra("O cliente já tem uma reserva ativa para este título.", () -> servico.reservar(ana.getId(), matrix.getId()));
        assertRegra("O cliente já está com este título alugado.", () -> servico.reservar(bruno.getId(), matrix.getId()));
        assertRegra("Cliente inativo não pode reservar.", () -> servico.reservar(inativo.getId(), matrix.getId()));
        assertRegra("Cliente não encontrado.", () -> servico.reservar(999, matrix.getId()));
        assertRegra("Título não encontrado.", () -> servico.reservar(ana.getId(), 999));
    }

    @Test
    void tituloSemExemplaresPodeSerReservadoEAquisicaoAtende() {
        Titulo duna = banco.dvd("Duna");

        Reserva reserva = servico.reservar(ana.getId(), duna.getId());
        banco.aquisicaoService.registrar(duna.getId(), 1, new BigDecimal("20.00"), "Distribuidora");

        assertEquals(SituacaoReserva.PRONTA, banco.reservas.buscar(reserva.getId()).getSituacao());
        assertEquals(List.of(reserva.getId()), servico.prontas().stream().map(Reserva::getId).toList());
        assertEquals(0, servico.posicao(banco.reservas.buscar(reserva.getId())));
    }

    @Test
    void cancelarReservaAguardandoTiraDaFila() {
        Reserva primeira = servico.reservar(ana.getId(), matrix.getId());
        Reserva segunda = servico.reservar(banco.cliente("Carla").getId(), matrix.getId());

        servico.cancelar(primeira.getId());

        assertEquals(SituacaoReserva.CANCELADA, banco.reservas.buscar(primeira.getId()).getSituacao());
        assertEquals(1, servico.posicao(banco.reservas.buscar(segunda.getId())));
        assertRegra("Esta reserva já foi encerrada.", () -> servico.cancelar(primeira.getId()));
        assertRegra("Reserva não encontrada.", () -> servico.cancelar(999));
    }

    @Test
    void cancelarReservaProntaPassaOExemplarParaOProximo() {
        Reserva primeira = servico.reservar(ana.getId(), matrix.getId());
        Reserva segunda = servico.reservar(banco.cliente("Carla").getId(), matrix.getId());
        banco.locacaoService.devolver(locacaoDoBruno.getId());

        servico.cancelar(primeira.getId());

        Reserva lida = banco.reservas.buscar(segunda.getId());
        assertEquals(SituacaoReserva.PRONTA, lida.getSituacao());
        assertEquals(exemplar.getId(), lida.getExemplar().getId());
        assertEquals(Situacao.RESERVADO, banco.exemplares.buscar(exemplar.getId()).getSituacao());
    }

    @Test
    void cancelarReservaProntaSemFilaLiberaOExemplar() {
        Reserva reserva = servico.reservar(ana.getId(), matrix.getId());
        banco.locacaoService.devolver(locacaoDoBruno.getId());

        servico.cancelar(reserva.getId());

        assertEquals(Situacao.DISPONIVEL, banco.exemplares.buscar(exemplar.getId()).getSituacao());
        assertTrue(servico.prontas().isEmpty());
        assertTrue(servico.ativas().isEmpty());
    }

    @Test
    void cancelarExigeLogin() {
        Reserva reserva = servico.reservar(ana.getId(), matrix.getId());
        AuthService.sair();

        assertRegra("Faça login para continuar.", () -> servico.cancelar(reserva.getId()));
        assertRegra("Faça login para continuar.", () -> servico.reservar(ana.getId(), matrix.getId()));
    }
}
