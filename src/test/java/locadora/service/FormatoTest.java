package locadora.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static locadora.BancoTeste.assertRegra;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormatoTest {

    @Test
    void leDinheiroDigitadoNoPadraoBrasileiro() {
        assertEquals(new BigDecimal("29.90"), Formato.lerDinheiro("29,90"));
        assertEquals(new BigDecimal("1234.50"), Formato.lerDinheiro(" R$ 1.234,5 "));
        assertEquals(new BigDecimal("15.00"), Formato.lerDinheiro("15"));
    }

    @Test
    void dinheiroDigitadoErradoGeraMensagemAmigavel() {
        assertRegra("Informe um valor como 29,90.", () -> Formato.lerDinheiro("vinte"));
        assertRegra("Informe um valor como 29,90.", () -> Formato.lerDinheiro(""));
    }

    @Test
    void dataPorExtensoParaOPainel() {
        assertEquals("quinta-feira, 1 de outubro de 2026", Formato.dataPorExtenso(LocalDate.of(2026, 10, 1)));
    }

    @Test
    void parecidoToleraErroDeDigitacaoAcentoEPontuacao() {
        assertTrue(Formato.parecido("estar wars", "Star Wars: O Império Contra-Ataca"));
        assertTrue(Formato.parecido("imperio contra ataca", "Star Wars: O Império Contra-Ataca"));
        assertTrue(Formato.parecido("matirx", "Matrix"));
        assertTrue(Formato.parecido("TUBARAO", "Tubarão"));
        assertTrue(Formato.parecido("", "Matrix"));
    }

    @Test
    void parecidoNaoConfundeTitulosDiferentesNemAproximaTermoCurto() {
        assertFalse(Formato.parecido("duna", "Cidade de Deus"));
        assertFalse(Formato.parecido("bruno", "Ana Beatriz Souza"));
        assertFalse(Formato.parecido("mtx", "Matrix"));
    }
}
