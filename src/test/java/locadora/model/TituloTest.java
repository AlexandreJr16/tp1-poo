package locadora.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TituloTest {

    private final Titulo bluRay = new BluRay(1, "Duna", "Ficcao cientifica", 2021, 3);
    private final Titulo dvd = new Dvd(2, "Matrix", "Acao", 1999, 5);
    private final Titulo cd = new Cd(3, "Acabou Chorare", "MPB", 1972, 7);
    private final Titulo fita = new FitaVideo(4, "Tubarao", "Suspense", 1975, 2);

    @Test
    void cadaTipoTemMultaDiariaPropria() {
        assertEquals(new BigDecimal("5.00"), bluRay.valorMultaDiaria());
        assertEquals(new BigDecimal("3.50"), dvd.valorMultaDiaria());
        assertEquals(new BigDecimal("2.00"), cd.valorMultaDiaria());
        assertEquals(new BigDecimal("1.50"), fita.valorMultaDiaria());
    }

    @Test
    void cadaTipoTemValorDeLocacaoProprio() {
        assertEquals(new BigDecimal("12.00"), bluRay.valorLocacao());
        assertEquals(new BigDecimal("8.00"), dvd.valorLocacao());
        assertEquals(new BigDecimal("5.00"), cd.valorLocacao());
        assertEquals(new BigDecimal("4.00"), fita.valorLocacao());
    }

    @Test
    void tipoIdentificaCadaMidia() {
        List<String> tipos = List.of(bluRay, dvd, cd, fita).stream().map(Titulo::tipo).toList();

        assertEquals(List.of("Blu-ray", "DVD", "CD", "Fita"), tipos);
    }

    @Test
    void devolucaoPrevistaSomaOPrazoDoTitulo() {
        LocalDate hoje = LocalDate.of(2026, 10, 30);

        assertEquals(LocalDate.of(2026, 11, 2), bluRay.devolucaoPrevista(hoje));
        assertEquals(LocalDate.of(2026, 11, 4), dvd.devolucaoPrevista(hoje));
    }
}
