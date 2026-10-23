package locadora.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocacaoTest {

    private static final LocalDate PRIMEIRO_DE_OUTUBRO = LocalDate.of(2026, 10, 1);

    private final Cliente cliente = new Cliente(1, "Ana Souza", "52998224725", "92 99999-0000", "ana@email.com", "Rua A, 10", true);
    private final Funcionario atendente = new Funcionario(1, "Bruno Lima", "52998224725", "", "", "bruno", null, null, Cargo.ATENDENTE);
    private final Funcionario gerente = new Funcionario(2, "Carla Dias", "52998224725", "", "", "carla", null, null, Cargo.GERENTE);

    private Exemplar exemplarDe(Titulo titulo) {
        return new Exemplar(1, Exemplar.codigo(titulo.getId(), 1), titulo, new BigDecimal("30.00"), Situacao.DISPONIVEL);
    }

    private Locacao locarDvd() {
        return Locacao.abrir(exemplarDe(new Dvd(2, "Matrix", "Acao", 1999, 5)), cliente, atendente, PRIMEIRO_DE_OUTUBRO);
    }

    @Test
    void abrirAlugaExemplarECalculaPrazoEValor() {
        Exemplar exemplar = exemplarDe(new Dvd(2, "Matrix", "Acao", 1999, 5));

        Locacao locacao = Locacao.abrir(exemplar, cliente, atendente, PRIMEIRO_DE_OUTUBRO);

        assertEquals(Situacao.ALUGADO, exemplar.getSituacao());
        assertEquals(LocalDate.of(2026, 10, 6), locacao.getDataPrevista());
        assertEquals(new BigDecimal("8.00"), locacao.getValorLocacao());
        assertSame(atendente, locacao.getFuncionarioLocacao());
        assertSame(cliente, locacao.getCliente());
        assertEquals(PRIMEIRO_DE_OUTUBRO, locacao.getDataLocacao());
        assertTrue(locacao.ativa());
    }

    @Test
    void abrirComExemplarIndisponivelFalha() {
        Exemplar exemplar = exemplarDe(new Dvd(2, "Matrix", "Acao", 1999, 5));
        exemplar.perder();

        assertThrows(IllegalStateException.class, () -> Locacao.abrir(exemplar, cliente, atendente, PRIMEIRO_DE_OUTUBRO));
    }

    @Test
    void devolverNoPrazoNaoGeraMulta() {
        Locacao locacao = locarDvd();

        locacao.devolver(LocalDate.of(2026, 10, 6), gerente);

        assertEquals(new BigDecimal("0.00"), locacao.getMulta());
        assertFalse(locacao.multaPendente());
        assertFalse(locacao.ativa());
        assertSame(gerente, locacao.getFuncionarioDevolucao());
        assertEquals(LocalDate.of(2026, 10, 6), locacao.getDataDevolucao());
        assertEquals(Situacao.DISPONIVEL, locacao.getExemplar().getSituacao());
    }

    @Test
    void devolverComAtrasoCobraMultaPorTipo() {
        List<Titulo> titulos = List.of(
                new BluRay(1, "Duna", "", 2021, 3),
                new Dvd(2, "Matrix", "", 1999, 5),
                new Cd(3, "Acabou Chorare", "", 1972, 7),
                new FitaVideo(4, "Tubarao", "", 1975, 2));
        List<String> esperadas = List.of("10.00", "7.00", "4.00", "3.00");

        for (int i = 0; i < titulos.size(); i++) {
            Titulo titulo = titulos.get(i);
            Locacao locacao = Locacao.abrir(exemplarDe(titulo), cliente, atendente, PRIMEIRO_DE_OUTUBRO);
            locacao.devolver(PRIMEIRO_DE_OUTUBRO.plusDays(titulo.getPrazoMaximoDias() + 2), gerente);
            assertEquals(new BigDecimal(esperadas.get(i)), locacao.getMulta(), titulo.tipo());
        }
    }

    @Test
    void multaAteDaPreviaSemDevolver() {
        Locacao locacao = locarDvd();

        assertEquals(new BigDecimal("10.50"), locacao.multaAte(LocalDate.of(2026, 10, 9)));
        assertEquals(new BigDecimal("0.00"), locacao.multaAte(LocalDate.of(2026, 10, 3)));
        assertTrue(locacao.ativa());
    }

    @Test
    void atrasadaSoDepoisDaDataPrevistaEnquantoAtiva() {
        Locacao locacao = locarDvd();

        assertFalse(locacao.atrasada(LocalDate.of(2026, 10, 6)));
        assertTrue(locacao.atrasada(LocalDate.of(2026, 10, 7)));
        assertEquals(3, locacao.diasAtraso(LocalDate.of(2026, 10, 9)));

        locacao.devolver(LocalDate.of(2026, 10, 9), atendente);
        assertFalse(locacao.atrasada(LocalDate.of(2026, 10, 20)));
    }

    @Test
    void devolverDuasVezesFalha() {
        Locacao locacao = locarDvd();
        locacao.devolver(LocalDate.of(2026, 10, 2), atendente);

        assertThrows(IllegalStateException.class, () -> locacao.devolver(LocalDate.of(2026, 10, 3), atendente));
    }

    @Test
    void multaFicaPendenteAteSerPaga() {
        Locacao locacao = locarDvd();
        locacao.devolver(LocalDate.of(2026, 10, 8), atendente);
        assertTrue(locacao.multaPendente());
        assertFalse(locacao.multaPaga());

        locacao.pagarMulta(LocalDate.of(2026, 10, 10));

        assertFalse(locacao.multaPendente());
        assertTrue(locacao.multaPaga());
        assertEquals(LocalDate.of(2026, 10, 10), locacao.getDataPagamentoMulta());
    }

    @Test
    void pagarSemMultaPendenteFalha() {
        Locacao locacao = locarDvd();
        locacao.devolver(LocalDate.of(2026, 10, 2), atendente);

        assertThrows(IllegalStateException.class, () -> locacao.pagarMulta(LocalDate.of(2026, 10, 2)));
    }

    @Test
    void renovarEstendePrazoUmaUnicaVez() {
        Locacao locacao = locarDvd();
        assertTrue(locacao.podeRenovar(LocalDate.of(2026, 10, 3)));

        locacao.renovar(LocalDate.of(2026, 10, 3));

        assertEquals(LocalDate.of(2026, 10, 11), locacao.getDataPrevista());
        assertTrue(locacao.isRenovada());
        assertFalse(locacao.podeRenovar(LocalDate.of(2026, 10, 4)));
        assertThrows(IllegalStateException.class, () -> locacao.renovar(LocalDate.of(2026, 10, 4)));
    }

    @Test
    void naoRenovaDepoisDoVencimentoNemDevolvida() {
        Locacao vencida = locarDvd();
        Locacao devolvida = locarDvd();
        devolvida.devolver(LocalDate.of(2026, 10, 2), atendente);

        assertTrue(vencida.podeRenovar(LocalDate.of(2026, 10, 6)));
        assertFalse(vencida.podeRenovar(LocalDate.of(2026, 10, 7)));
        assertFalse(devolvida.podeRenovar(LocalDate.of(2026, 10, 2)));
    }

    @Test
    void encerrarPorPerdaCobraCustoMaisMultaAteAData() {
        Locacao locacao = locarDvd();

        locacao.encerrarPorPerda(LocalDate.of(2026, 10, 8), gerente);

        assertEquals(new BigDecimal("37.00"), locacao.getMulta());
        assertEquals(Situacao.PERDIDO, locacao.getExemplar().getSituacao());
        assertFalse(locacao.ativa());
        assertTrue(locacao.multaPendente());
        assertSame(gerente, locacao.getFuncionarioDevolucao());
    }
}
