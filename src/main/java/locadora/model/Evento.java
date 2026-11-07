package locadora.model;

import java.time.LocalDate;

/**
 * Um acontecimento na linha do tempo de um exemplar (compra, locacao, devolucao, perda), com a data e o funcionario.
 */
public record Evento(LocalDate data, String descricao, String funcionario) {
}
