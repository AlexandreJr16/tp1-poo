package locadora.model;

import java.math.BigDecimal;

public class Exemplar {

    private final int id;
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

    public static String codigo(int tituloId, int sequencia) {
        return "";
    }

    public boolean disponivel() {
        return false;
    }

    public void alugar() {
    }

    public void devolver() {
    }

    public void reservar() {
    }

    public void liberar() {
    }

    public void perder() {
    }

    public int getId() {
        return id;
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
