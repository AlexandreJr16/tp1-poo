package locadora;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/* Relogio controlado pelos testes: comeca num dia fixo e so anda quando o teste manda.
   avancar: pula N dias
   hoje: dia atual do relogio */
public class RelogioTeste extends Clock {

    private Instant agora;

    public RelogioTeste(LocalDate dia) {
        agora = dia.atTime(12, 0).toInstant(ZoneOffset.UTC);
    }

    public void avancar(int dias) {
        agora = agora.plus(Duration.ofDays(dias));
    }

    public LocalDate hoje() {
        return LocalDate.ofInstant(agora, ZoneOffset.UTC);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return this;
    }

    @Override
    public Instant instant() {
        return agora;
    }
}
