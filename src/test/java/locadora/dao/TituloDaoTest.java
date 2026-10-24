package locadora.dao;

import locadora.BancoTeste;
import locadora.model.BluRay;
import locadora.model.Cd;
import locadora.model.Dvd;
import locadora.model.FitaVideo;
import locadora.model.Titulo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TituloDaoTest {

    @TempDir
    Path dir;

    private TituloDao titulos;

    @BeforeEach
    void abrir() {
        titulos = new BancoTeste(dir).titulos;
    }

    @Test
    void inserirPreencheOIdEBuscarDevolveASubclasseCerta() {
        List<Titulo> originais = List.of(
                new BluRay(0, "Duna", "Ficcao cientifica", 2021, 3),
                new Dvd(0, "Matrix", "Acao", 1999, 5),
                new Cd(0, "Acabou Chorare", "MPB", 1972, 7),
                new FitaVideo(0, "Tubarao", "Suspense", 1975, 2));

        originais.forEach(titulos::inserir);

        for (Titulo original : originais) {
            assertTrue(original.getId() > 0);
            Titulo lido = titulos.buscar(original.getId());
            assertEquals(original.getClass(), lido.getClass());
            assertEquals(original.getNome(), lido.getNome());
            assertEquals(original.getDescricao(), lido.getDescricao());
            assertEquals(original.getAno(), lido.getAno());
            assertEquals(original.getPrazoMaximoDias(), lido.getPrazoMaximoDias());
        }
    }

    @Test
    void atualizarGravaNovosDadosInclusiveOTipo() {
        Titulo matrix = new Dvd(0, "Matrix", "Acao", 1999, 5);
        titulos.inserir(matrix);

        titulos.atualizar(new BluRay(matrix.getId(), "Matrix Remasterizado", "Acao em 4K", 2021, 4));

        Titulo lido = titulos.buscar(matrix.getId());
        assertInstanceOf(BluRay.class, lido);
        assertEquals("Matrix Remasterizado", lido.getNome());
        assertEquals(4, lido.getPrazoMaximoDias());
    }

    @Test
    void listarOrdenaPorNomeSemDiferenciarMaiusculas() {
        titulos.inserir(new Dvd(0, "matrix", "", 1999, 5));
        titulos.inserir(new Cd(0, "Clube da Esquina", "", 1972, 7));
        titulos.inserir(new BluRay(0, "Avatar", "", 2009, 3));

        List<String> nomes = titulos.listar().stream().map(Titulo::getNome).toList();

        assertEquals(List.of("Avatar", "Clube da Esquina", "matrix"), nomes);
    }

    @Test
    void buscarInexistenteDevolveNulo() {
        assertNull(titulos.buscar(42));
    }

    @Test
    void instanciarRecusaTipoDesconhecido() {
        assertThrows(IllegalArgumentException.class, () -> TituloDao.instanciar("VHS", 0, "X", "", 2000, 3));
    }

    @Test
    void capaGravadaVoltaIgualESoTitulosComCapaAparecemNoMapa() {
        Titulo comCapa = new Dvd(0, "Matrix", "", 1999, 5);
        Titulo semCapa = new Dvd(0, "Bacurau", "", 2019, 5);
        titulos.inserir(comCapa);
        titulos.inserir(semCapa);
        byte[] imagem = {1, 2, 3, 4};

        titulos.salvarCapa(comCapa.getId(), imagem);

        assertArrayEquals(imagem, titulos.capa(comCapa.getId()));
        assertNull(titulos.capa(semCapa.getId()));
        assertEquals(List.of(comCapa.getId()), List.copyOf(titulos.capas().keySet()));
        assertArrayEquals(imagem, titulos.capas().get(comCapa.getId()));
    }
}
