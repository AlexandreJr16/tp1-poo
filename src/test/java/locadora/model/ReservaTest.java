package locadora.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaTest {

    private final Titulo matrix = new Dvd(2, "Matrix", "Acao", 1999, 5);
    private final Cliente cliente = new Cliente(1, "Ana Souza", "52998224725", "", "", "", true);
    private final Funcionario atendente = new Funcionario(1, "Bruno Lima", "52998224725", "", "", "bruno", null, null, Cargo.ATENDENTE);
    private final Exemplar exemplar = new Exemplar(7, "T2-001", matrix, new BigDecimal("30.00"), Situacao.DISPONIVEL);

    private Reserva novaReserva() {
        return Reserva.abrir(matrix, cliente, atendente, LocalDate.of(2026, 10, 1));
    }

    @Test
    void novaReservaAguardaNaFilaSemExemplar() {
        Reserva reserva = novaReserva();

        assertEquals(SituacaoReserva.AGUARDANDO, reserva.getSituacao());
        assertTrue(reserva.ativa());
        assertNull(reserva.getExemplar());
        assertSame(matrix, reserva.getTitulo());
        assertSame(cliente, reserva.getCliente());
    }

    @Test
    void prepararSeparaExemplarParaOCliente() {
        Reserva reserva = novaReserva();

        reserva.preparar(exemplar);

        assertEquals(SituacaoReserva.PRONTA, reserva.getSituacao());
        assertSame(exemplar, reserva.getExemplar());
        assertEquals(Situacao.RESERVADO, exemplar.getSituacao());
        assertThrows(IllegalStateException.class, () -> reserva.preparar(exemplar));
    }

    @Test
    void atenderSoReservaPronta() {
        Reserva reserva = novaReserva();
        assertThrows(IllegalStateException.class, reserva::atender);

        reserva.preparar(exemplar);
        reserva.atender();

        assertEquals(SituacaoReserva.ATENDIDA, reserva.getSituacao());
        assertFalse(reserva.ativa());
    }

    @Test
    void cancelarReservaProntaLiberaOExemplar() {
        Reserva reserva = novaReserva();
        reserva.preparar(exemplar);

        reserva.cancelar();

        assertEquals(SituacaoReserva.CANCELADA, reserva.getSituacao());
        assertTrue(exemplar.disponivel());
        assertThrows(IllegalStateException.class, reserva::cancelar);
    }

    @Test
    void cancelarReservaAguardandoSoEncerra() {
        Reserva reserva = novaReserva();

        reserva.cancelar();

        assertEquals(SituacaoReserva.CANCELADA, reserva.getSituacao());
        assertFalse(reserva.ativa());
    }

    @Test
    void voltarParaFilaSoltaOExemplar() {
        Reserva reserva = novaReserva();
        reserva.preparar(exemplar);

        reserva.voltarParaFila();

        assertEquals(SituacaoReserva.AGUARDANDO, reserva.getSituacao());
        assertNull(reserva.getExemplar());
        assertThrows(IllegalStateException.class, reserva::voltarParaFila);
    }

    @Test
    void situacaoDaReservaTemRotuloParaTela() {
        assertEquals("Aguardando", SituacaoReserva.AGUARDANDO.toString());
        assertEquals("Pronta para retirada", SituacaoReserva.PRONTA.toString());
        assertEquals("Atendida", SituacaoReserva.ATENDIDA.toString());
        assertEquals("Cancelada", SituacaoReserva.CANCELADA.toString());
    }
}
