package locadora.model;

public abstract class Pessoa {

    private final int id;
    private final String nome;
    private final String cpf;
    private final String telefone;
    private final String email;

    protected Pessoa(int id, String nome, String cpf, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
    }

    public static boolean cpfValido(String cpf) {
        throw new UnsupportedOperationException();
    }

    public String cpfFormatado() {
        throw new UnsupportedOperationException();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }
}
