package locadora.service;

import locadora.BancoTeste;
import locadora.model.Cliente;
import locadora.model.Recomendacao;
import locadora.model.Titulo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecomendacaoServiceTest {

    @TempDir
    Path dir;

    private BancoTeste banco;
    private Titulo matrix;
    private Titulo duna;
    private Titulo interestelar;
    private Titulo bacurau;
    private Cliente ana;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
        banco.logarGerente();
        matrix = titulo("Matrix");
        duna = titulo("Duna");
        interestelar = titulo("Interestelar");
        bacurau = titulo("Bacurau");
        ana = banco.cliente("Ana");
    }

    private Titulo titulo(String nome) {
        Titulo titulo = banco.dvd(nome);
        banco.adquirir(titulo, 1, banco.logarGerente());
        return titulo;
    }

    private void alugou(Cliente cliente, Titulo... titulos) {
        for (Titulo titulo : titulos) {
            int exemplarId = banco.exemplares.listarPorTitulo(titulo.getId()).get(0).getId();
            banco.locacaoService.devolver(banco.locacaoService.locar(cliente.getId(), exemplarId).getId());
        }
    }

    private static List<String> nomes(List<Recomendacao> recomendacoes) {
        return recomendacoes.stream().map(r -> r.titulo().getNome()).toList();
    }

    private static List<String> motivos(List<Recomendacao> recomendacoes) {
        return recomendacoes.stream().map(r -> r.porque() == null ? null : r.porque().getNome()).toList();
    }

    @Test
    void recomendaOQueQuemAlugouOMesmoTituloTambemAlugou() {
        alugou(ana, matrix);
        alugou(banco.cliente("Bruno"), matrix, duna, interestelar);
        alugou(banco.cliente("Carla"), matrix, duna);
        alugou(banco.cliente("Diego"), bacurau);

        List<Recomendacao> recomendacoes = banco.locacaoService.recomendacoes(ana.getId());

        assertEquals(List.of("Duna", "Interestelar", "Bacurau"), nomes(recomendacoes));
        assertEquals(Arrays.asList("Matrix", "Matrix", null), motivos(recomendacoes));
    }

    @Test
    void clienteSemHistoricoRecebeOsMaisAlugados() {
        alugou(banco.cliente("Bruno"), matrix, duna, interestelar);
        alugou(banco.cliente("Carla"), matrix, duna, bacurau);
        alugou(banco.cliente("Diego"), matrix);

        List<Recomendacao> recomendacoes = banco.locacaoService.recomendacoes(ana.getId());

        assertEquals(List.of("Matrix", "Duna", "Interestelar", "Bacurau"), nomes(recomendacoes));
        recomendacoes.forEach(r -> assertNull(r.porque()));
    }

    @Test
    void listaDeRecomendacoesTemNoMaximoCincoTitulos() {
        Cliente bruno = banco.cliente("Bruno");
        alugou(ana, matrix);
        alugou(bruno, matrix, duna, interestelar, bacurau);
        for (int i = 0; i < 4; i++)
            alugou(bruno, titulo("Filme " + i));

        assertEquals(LocacaoService.LIMITE_DE_RECOMENDACOES, banco.locacaoService.recomendacoes(ana.getId()).size());
    }
}
