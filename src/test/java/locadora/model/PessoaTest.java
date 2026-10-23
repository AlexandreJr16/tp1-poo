package locadora.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PessoaTest {

    @Test
    void cpfValidoAceitaComESemPontuacao() {
        assertTrue(Pessoa.cpfValido("529.982.247-25"));
        assertTrue(Pessoa.cpfValido("52998224725"));
    }

    @Test
    void cpfComDigitoVerificadorErradoEInvalido() {
        assertFalse(Pessoa.cpfValido("529.982.247-24"));
        assertFalse(Pessoa.cpfValido("529.982.247-15"));
    }

    @Test
    void cpfComDigitosRepetidosOuTamanhoErradoEInvalido() {
        assertFalse(Pessoa.cpfValido("111.111.111-11"));
        assertFalse(Pessoa.cpfValido("5299822472"));
        assertFalse(Pessoa.cpfValido(""));
        assertFalse(Pessoa.cpfValido(null));
    }

    @Test
    void cpfFicaGuardadoSoComDigitosEFormataNaExibicao() {
        Cliente cliente = new Cliente(1, "Ana Souza", "529.982.247-25", "92 99999-0000", "ana@email.com", "Rua A, 10", true);

        assertEquals("52998224725", cliente.getCpf());
        assertEquals("529.982.247-25", cliente.cpfFormatado());
    }
}
