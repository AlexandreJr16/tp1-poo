package locadora.view.componente;

import locadora.model.Exemplar;
import locadora.model.Locacao;

import java.time.LocalDate;

/* Textos curtos repetidos em varias telas.
   situacao: como uma locacao esta hoje (em aberto, atrasada, multa pendente...)
   exemplar: codigo e nome do titulo do exemplar */
public final class Descricoes {

    private Descricoes() {
    }

    public static String situacao(Locacao locacao, LocalDate hoje) {
        if (locacao.atrasada(hoje))
            return "Atrasada há " + dias(locacao.diasAtraso(hoje));
        if (locacao.ativa())
            return locacao.isRenovada() ? "Em aberto (renovada)" : "Em aberto";
        if (locacao.multaPendente())
            return "Multa pendente";
        return locacao.multaPaga() ? "Devolvida, multa paga" : "Devolvida";
    }

    public static String exemplar(Exemplar exemplar) {
        return exemplar.getCodigo() + " · " + exemplar.getTitulo().getNome();
    }

    private static String dias(long quantidade) {
        return quantidade + (quantidade == 1 ? " dia" : " dias");
    }
}
