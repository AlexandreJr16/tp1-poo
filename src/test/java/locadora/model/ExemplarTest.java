package locadora.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExemplarTest {

    private Exemplar exemplar(Situacao situacao) {
        return new Exemplar(1, "T2-001", new Dvd(2, "Matrix", "Acao", 1999, 5), new BigDecimal("30.00"), situacao);
    }

    @Test
    void codigoEhSequencialPorTitulo() {
        assertEquals("T12-004", Exemplar.codigo(12, 4));
        assertEquals("T3-120", Exemplar.codigo(3, 120));
    }

    @Test
    void alugaExemplarDisponivelOuReservado() {
        Exemplar disponivel = exemplar(Situacao.DISPONIVEL);
        Exemplar reservado = exemplar(Situacao.RESERVADO);

        disponivel.alugar();
        reservado.alugar();

        assertEquals(Situacao.ALUGADO, disponivel.getSituacao());
        assertEquals(Situacao.ALUGADO, reservado.getSituacao());
    }

    @Test
    void naoAlugaExemplarJaAlugado() {
        assertThrows(IllegalStateException.class, () -> exemplar(Situacao.ALUGADO).alugar());
    }

    @Test
    void devolverVoltaParaDisponivel() {
        Exemplar alugado = exemplar(Situacao.ALUGADO);

        alugado.devolver();

        assertTrue(alugado.disponivel());
        assertThrows(IllegalStateException.class, alugado::devolver);
    }

    @Test
    void reservarSeparaDisponivelELiberarDevolve() {
        Exemplar exemplar = exemplar(Situacao.DISPONIVEL);

        exemplar.reservar();
        assertEquals(Situacao.RESERVADO, exemplar.getSituacao());
        assertFalse(exemplar.disponivel());

        exemplar.liberar();
        assertTrue(exemplar.disponivel());
        assertThrows(IllegalStateException.class, exemplar::liberar);
    }

    @Test
    void perdidoNuncaVoltaASerAlugavel() {
        Exemplar exemplar = exemplar(Situacao.ALUGADO);

        exemplar.perder();

        assertEquals(Situacao.PERDIDO, exemplar.getSituacao());
        assertThrows(IllegalStateException.class, exemplar::alugar);
        assertThrows(IllegalStateException.class, exemplar::devolver);
        assertThrows(IllegalStateException.class, exemplar::reservar);
        assertThrows(IllegalStateException.class, exemplar::perder);
    }

    @Test
    void situacaoTemRotuloParaTela() {
        assertEquals("Disponível", Situacao.DISPONIVEL.toString());
        assertEquals("Alugado", Situacao.ALUGADO.toString());
        assertEquals("Reservado", Situacao.RESERVADO.toString());
        assertEquals("Perdido", Situacao.PERDIDO.toString());
    }
}
