package locadora.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Funcionario que usa o sistema. A senha e guardada como hash SHA-256 com sal aleatorio, nunca em texto puro.
 */
public class Funcionario extends Pessoa {

    private static final SecureRandom ALEATORIO = new SecureRandom();

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

    /**
     * Gera um sal novo e guarda o hash da senha.
     */
    public void definirSenha(String senha) {
        byte[] bytes = new byte[16];
        ALEATORIO.nextBytes(bytes);
        sal = HexFormat.of().formatHex(bytes);
        senhaHash = hash(sal, senha);
    }

    /**
     * Confere a senha digitada contra o hash guardado.
     */
    public boolean senhaConfere(String senha) {
        return senhaHash != null && senhaHash.equals(hash(sal, senha));
    }

    public boolean gerente() {
        return cargo == Cargo.GERENTE;
    }

    static String hash(String sal, String senha) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest((sal + senha).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel nesta JVM", e);
        }
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
