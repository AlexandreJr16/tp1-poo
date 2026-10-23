package locadora.model;

public class Cliente extends Pessoa {

    private final String endereco;
    private final boolean ativo;

    public Cliente(int id, String nome, String cpf, String telefone, String email, String endereco, boolean ativo) {
        super(id, nome, cpf, telefone, email);
        this.endereco = endereco;
        this.ativo = ativo;
    }

    public String getEndereco() {
        return endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
