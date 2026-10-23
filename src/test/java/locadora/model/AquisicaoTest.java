package locadora.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AquisicaoTest {

    @Test
    void custoTotalMultiplicaCustoUnitarioPelaQuantidade() {
        Aquisicao aquisicao = new Aquisicao(0, new Dvd(1, "Matrix", "", 1999, 5), 4, new BigDecimal("29.90"),
                "Distribuidora Norte", LocalDate.of(2026, 10, 1), null);

        assertEquals(new BigDecimal("119.60"), aquisicao.custoTotal());
    }
}
