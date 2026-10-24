package locadora.service;

import locadora.BancoTeste;
import locadora.model.Dvd;
import locadora.model.Titulo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapaServiceTest {

    @TempDir
    Path dir;

    private BancoTeste banco;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
    }

    private static String fixture(String nome) throws IOException {
        try (InputStream entrada = CapaServiceTest.class.getResourceAsStream("/wikipedia/" + nome)) {
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void lerPegaAImagemESinopseDoPrimeiroResultado() throws IOException {
        CapaService.Pagina pagina = CapaService.ler(fixture("matrix.json")).orElseThrow();

        assertTrue(pagina.imagem().startsWith("https://upload.wikimedia.org/wikipedia/pt/c/c1/The_Matrix_Poster.jpg"));
        assertTrue(pagina.sinopse().startsWith("The Matrix (bra/prt: Matrix) é um filme australo-estadunidense de 1999"));
        assertTrue(pagina.sinopse().length() <= CapaService.TAMANHO_DA_SINOPSE);
    }

    @Test
    void lerPulaResultadoSemImagem() throws IOException {
        CapaService.Pagina pagina = CapaService.ler(fixture("clube-da-esquina.json")).orElseThrow();

        assertTrue(pagina.imagem().contains("Milton_Nascimento_-_Clube_da_Esquina.jpg"));
        assertTrue(pagina.sinopse().startsWith("Clube da Esquina é um álbum colaborativo"));
    }

    @Test
    void lerSemResultadoDevolveVazio() {
        assertEquals(Optional.empty(), CapaService.ler("{\"batchcomplete\":true}"));
        assertEquals(Optional.empty(), CapaService.ler("You are making too many requests"));
    }

    @Test
    void sinopseCurtaFicaInteiraELongaCortaNoFimDeUmaFrase() {
        assertEquals("Um filme \"bom\".", CapaService.sinopse("Um filme \\\"bom\\\"."));
        String longa = "Primeira frase curta. " + "palavra ".repeat(60) + "fim.";

        assertEquals("Primeira frase curta.", CapaService.sinopse(longa));
    }

    @Test
    void semInternetABuscaDevolveVazioSemErro() {
        CapaService offline = new CapaService(banco.titulos, "http://127.0.0.1:9");

        Optional<CapaService.Achado> achado = assertTimeout(Duration.ofSeconds(8), () -> offline.buscar("Matrix", Dvd.TIPO));

        assertEquals(Optional.empty(), achado);
        assertEquals(Optional.empty(), offline.buscar("  ", Dvd.TIPO));
    }

    @Test
    void capaGuardadaVoltaPeloTitulo() {
        Titulo matrix = banco.dvd("Matrix");
        Titulo bacurau = banco.dvd("Bacurau");
        byte[] imagem = {9, 8, 7};

        banco.capaService.guardar(matrix.getId(), imagem);

        assertArrayEquals(imagem, banco.capaService.capa(matrix.getId()));
        assertNull(banco.capaService.capa(bacurau.getId()));
        assertEquals(1, banco.capaService.capas().size());
    }
}
