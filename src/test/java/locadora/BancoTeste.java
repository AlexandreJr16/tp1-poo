package locadora;

import locadora.dao.ClienteDao;
import locadora.dao.Conexao;
import locadora.dao.FuncionarioDao;
import locadora.dao.TituloDao;
import locadora.model.Cargo;
import locadora.model.Cliente;
import locadora.model.Dvd;
import locadora.model.Funcionario;
import locadora.model.Titulo;

import java.nio.file.Path;
import java.util.List;

/* Banco de teste num arquivo temporario, com os DAOs prontos e atalhos para criar dados.
   - Dados:
     cpf: devolve um cpf valido ainda nao usado neste banco
     dvd: grava um DVD com prazo de 5 dias
     cliente: grava um cliente ativo
     funcionario: grava um funcionario com a senha "123" */
public class BancoTeste {

    private static final List<String> CPFS = List.of(
            "239.159.970-64", "613.801.808-72", "624.734.278-01", "133.558.845-02",
            "600.518.000-20", "406.326.503-03", "081.589.085-00", "158.444.743-50",
            "668.990.851-86", "113.630.343-08", "968.042.622-04", "349.437.637-90",
            "052.384.578-20", "568.849.112-47", "041.324.506-35", "838.923.203-06");

    public final Conexao conexao;
    public final TituloDao titulos;
    public final ClienteDao clientes;
    public final FuncionarioDao funcionarios;
    private int proximoCpf;

    public BancoTeste(Path dir) {
        conexao = new Conexao(dir.resolve("teste.db"));
        conexao.criarSchemaSeVazio();
        titulos = new TituloDao(conexao);
        clientes = new ClienteDao(conexao);
        funcionarios = new FuncionarioDao(conexao);
    }

    public String cpf() {
        return CPFS.get(proximoCpf++);
    }

    public Titulo dvd(String nome) {
        Titulo titulo = new Dvd(0, nome, "", 2000, 5);
        titulos.inserir(titulo);
        return titulo;
    }

    public Cliente cliente(String nome) {
        Cliente cliente = new Cliente(0, nome, cpf(), "92 99999-0000", "", "", true);
        clientes.inserir(cliente);
        return cliente;
    }

    public Funcionario funcionario(String login, Cargo cargo) {
        Funcionario funcionario = new Funcionario(0, login, cpf(), "", "", login, null, null, cargo);
        funcionario.definirSenha("123");
        funcionarios.inserir(funcionario);
        return funcionario;
    }
}
