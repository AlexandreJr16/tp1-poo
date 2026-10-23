package locadora.service;

import locadora.BancoTeste;
import locadora.model.BluRay;
import locadora.model.Cliente;
import locadora.model.Evento;
import locadora.model.Exemplar;
import locadora.model.Funcionario;
import locadora.model.Locacao;
import locadora.model.Titulo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PainelServiceTest {

    @TempDir
    Path dir;

    private BancoTeste banco;
    private Funcionario gerente;
    private Cliente ana;
    private Titulo matrix;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
        gerente = banco.logarGerente();
        ana = banco.cliente("Ana Souza");
        matrix = banco.dvd("Matrix");
    }

    @Test
    void resumoDoPainelContaLocacoesMultasTitulosFaturamentoEReservas() {
        List<Exemplar> matrixes = banco.adquirir(matrix, 3, gerente);
        Titulo duna = new BluRay(0, "Duna", "", 2021, 3);
        banco.titulos.inserir(duna);
        Exemplar dunaExemplar = banco.adquirir(duna, 1, gerente).get(0);
        Titulo bacurau = banco.dvd("Bacurau");
        Locacao antiga = Locacao.abrir(matrixes.get(2), banco.cliente("Davi"), gerente, LocalDate.of(2026, 9, 20));
        banco.locacoes.registrar(antiga, null);
        banco.locacaoService.devolver(antiga.getId());
        Locacao daAna = banco.locacaoService.locar(ana.getId(), matrixes.get(0).getId());
        Locacao doBruno = banco.locacaoService.locar(banco.cliente("Bruno").getId(), dunaExemplar.getId());
        Locacao daCarla = banco.locacaoService.locar(banco.cliente("Carla").getId(), matrixes.get(1).getId());
        banco.locacaoService.devolver(daCarla.getId());
        banco.reservaService.reservar(ana.getId(), bacurau.getId());
        banco.aquisicaoService.registrar(bacurau.getId(), 1, new BigDecimal("20.00"), "Distribuidora");
        banco.relogio.avancar(4);

        DashboardService.Resumo resumo = banco.dashboardService.resumo();

        assertEquals(2, resumo.locacoesAtivas());
        assertEquals(1, resumo.atrasadas());
        assertEquals(new BigDecimal("21.00"), resumo.multasEmAberto());
        assertEquals(3, resumo.titulos());
        assertEquals(new BigDecimal("28.00"), resumo.faturamentoMes());
        assertEquals(1, resumo.reservasProntas());
        assertEquals(List.of(daAna.getId()), banco.dashboardService.proximasDevolucoes().stream().map(Locacao::getId).toList());
        assertEquals(List.of(doBruno.getId()), banco.locacaoService.atrasadas().stream().map(Locacao::getId).toList());
        assertEquals("Bacurau", banco.reservaService.prontas().get(0).getTitulo().getNome());
    }

    @Test
    void buscaGlobalAchaTituloSemDiferenciarAcentoEMaiusculas() {
        banco.titulos.inserir(new BluRay(0, "Tubarão", "", 1975, 3));

        List<BuscaService.Resultado> resultados = banco.buscaService.buscar("TUBARAO");

        assertEquals(1, resultados.size());
        assertEquals(BuscaService.Tipo.TITULO, resultados.get(0).tipo());
        assertEquals("Título", resultados.get(0).tipo().toString());
        assertEquals("Tubarão", resultados.get(0).rotulo());
        assertEquals("Blu-ray · 1975", resultados.get(0).detalhe());
    }

    @Test
    void buscaGlobalAchaClientePorNomeOuCpf() {
        String cpfFormatado = ana.cpfFormatado();

        List<BuscaService.Resultado> porNome = banco.buscaService.buscar("souza");
        List<BuscaService.Resultado> porCpf = banco.buscaService.buscar(cpfFormatado.substring(0, 7));

        assertEquals(List.of(ana.getId()), porNome.stream().map(BuscaService.Resultado::id).toList());
        assertEquals(List.of(ana.getId()), porCpf.stream().map(BuscaService.Resultado::id).toList());
        assertEquals(BuscaService.Tipo.CLIENTE, porCpf.get(0).tipo());
        assertEquals("CPF " + cpfFormatado, porCpf.get(0).detalhe());
    }

    @Test
    void buscaGlobalAchaExemplarPeloCodigoEApontaParaOTitulo() {
        banco.adquirir(matrix, 2, gerente);

        List<BuscaService.Resultado> resultados = banco.buscaService.buscar("t1-00");

        assertEquals(List.of("T1-001", "T1-002"), resultados.stream().map(BuscaService.Resultado::rotulo).toList());
        assertEquals(BuscaService.Tipo.EXEMPLAR, resultados.get(0).tipo());
        assertEquals(matrix.getId(), resultados.get(0).id());
        assertEquals("Matrix · Disponível", resultados.get(0).detalhe());
    }

    @Test
    void buscaGlobalVaziaOuCurtaNaoTrazNadaEMuitaCoisaVemLimitada() {
        for (int i = 0; i < 15; i++)
            banco.dvd("Filme " + i);

        assertTrue(banco.buscaService.buscar("  ").isEmpty());
        assertTrue(banco.buscaService.buscar("f").isEmpty());
        assertEquals(BuscaService.LIMITE, banco.buscaService.buscar("filme").size());
    }

    @Test
    void linhaDoTempoContaAHistoriaDoExemplar() {
        Exemplar exemplar = banco.adquirir(matrix, 1, gerente).get(0);
        Locacao daAna = banco.locacaoService.locar(ana.getId(), exemplar.getId());
        banco.relogio.avancar(8);
        banco.locacaoService.devolver(daAna.getId());
        banco.locacaoService.pagarMulta(daAna.getId());
        banco.locacaoService.locar(banco.cliente("Bruno").getId(), exemplar.getId());
        banco.relogio.avancar(3);
        banco.perdaService.registrar(exemplar.getId(), "Cliente perdeu o disco");

        List<Evento> eventos = banco.tituloService.linhaDoTempo(exemplar.getId());

        assertEquals(List.of(
                "Adquirido de Distribuidora Teste por R$ 30,00",
                "Alugado para Ana Souza",
                "Devolvido por Ana Souza com multa de R$ 10,50",
                "Alugado para Bruno",
                "Dado como perdido: Cliente perdeu o disco. Cobrado R$ 30,00 de Bruno"), eventos.stream().map(Evento::descricao).toList());
        assertEquals(List.of(BancoTeste.HOJE, BancoTeste.HOJE, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 12)),
                eventos.stream().map(Evento::data).toList());
        assertTrue(eventos.stream().allMatch(e -> e.funcionario().equals("gerente")));
    }

    @Test
    void linhaDoTempoDeExemplarParadoSoTemAAquisicao() {
        Exemplar exemplar = banco.adquirir(matrix, 1, gerente).get(0);

        List<Evento> eventos = banco.tituloService.linhaDoTempo(exemplar.getId());

        assertEquals(1, eventos.size());
        assertEquals("gerente", eventos.get(0).funcionario());
    }
}
