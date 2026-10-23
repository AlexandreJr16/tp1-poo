package locadora.model;

public class Funcionario extends Pessoa {

    private final String login;
    private String senhaHash;
    private String sal;
    private final Cargo cargo;

    public Funcionario(int id, String nome, String cpf, String telefone, String email,
                       String login, String senhaHash, String sal, Cargo cargo) {
        super(id, nome, cpf, telefone, email);
        this.login = login;
        this.senhaHash = senhaHash;
        this.sal = sal;
        this.cargo = cargo;
    }

    public void definirSenha(String senha) {
        throw new UnsupportedOperationException();
    }

    public boolean senhaConfere(String senha) {
        throw new UnsupportedOperationException();
    }

    public boolean gerente() {
        throw new UnsupportedOperationException();
    }

    static String hash(String sal, String senha) {
        throw new UnsupportedOperationException();
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getSal() {
        return sal;
    }

    public Cargo getCargo() {
        return cargo;
    }
}
