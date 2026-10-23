package locadora.model;

public abstract class Pessoa {

    private int id;
    private final String nome;
    private final String cpf;
    private final String telefone;
    private final String email;

    protected Pessoa(int id, String nome, String cpf, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.cpf = somenteDigitos(cpf);
        this.telefone = telefone;
        this.email = email;
    }

    public static boolean cpfValido(String cpf) {
        String digitos = somenteDigitos(cpf);
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1)
            return false;
        return digitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
                && digitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
    }

    // soma ponderada dos primeiros digitos com pesos decrescentes a partir de quantidade + 1
    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++)
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    public String cpfFormatado() {
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
