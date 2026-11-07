package locadora.model;

import java.math.BigDecimal;

/**
 * Copia fisica de um titulo, identificada por um codigo como T7-004. As trocas de situacao ficam aqui e recusam transicoes invalidas.
 */
public class Exemplar {

    private int id;
    private final String codigo;
    private final Titulo titulo;
    private final BigDecimal custo;
    private Situacao situacao;

    public Exemplar(int id, String codigo, Titulo titulo, BigDecimal custo, Situacao situacao) {
        this.id = id;
        this.codigo = codigo;
        this.titulo = titulo;
        this.custo = custo;
        this.situacao = situacao;
    }

    /**
     * Codigo do exemplar no formato T&lt;titulo&gt;-&lt;sequencia&gt;, por exemplo T7-004.
     */
    public static String codigo(int tituloId, int sequencia) {
        return String.format("T%d-%03d", tituloId, sequencia);
    }

    public boolean disponivel() {
        return situacao == Situacao.DISPONIVEL;
    }

    /**
     * Marca como alugado; so vale para exemplar disponivel ou separado em reserva.
     */
    public void alugar() {
        exigir(situacao == Situacao.DISPONIVEL || situacao == Situacao.RESERVADO, "alugado");
        situacao = Situacao.ALUGADO;
    }

    /**
     * Volta para disponivel; so vale para exemplar alugado.
     */
    public void devolver() {
        exigir(situacao == Situacao.ALUGADO, "devolvido");
        situacao = Situacao.DISPONIVEL;
    }

    /**
     * Separa o exemplar para uma reserva; so vale para exemplar disponivel.
     */
    public void reservar() {
        exigir(situacao == Situacao.DISPONIVEL, "reservado");
        situacao = Situacao.RESERVADO;
    }

    /**
     * Desfaz a separacao da reserva, voltando para disponivel.
     */
    public void liberar() {
        exigir(situacao == Situacao.RESERVADO, "liberado");
        situacao = Situacao.DISPONIVEL;
    }

    /**
     * Marca como perdido. Um exemplar perdido nunca mais volta a ser alugado.
     */
    public void perder() {
        exigir(situacao != Situacao.PERDIDO, "dado como perdido");
        situacao = Situacao.PERDIDO;
    }

    private void exigir(boolean permitido, String acao) {
        if (!permitido)
            throw new IllegalStateException("O exemplar " + codigo + " está " + situacao.toString().toLowerCase() + " e não pode ser " + acao + ".");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Titulo getTitulo() {
        return titulo;
    }

    public BigDecimal getCusto() {
        return custo;
    }

    public Situacao getSituacao() {
        return situacao;
    }
}
