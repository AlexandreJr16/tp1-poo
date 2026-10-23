package locadora.model;

import java.time.LocalDate;

public record Evento(LocalDate data, String descricao, String funcionario) {
}
