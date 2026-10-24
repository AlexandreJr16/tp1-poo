package locadora;

import locadora.dao.AquisicaoDao;
import locadora.dao.ClienteDao;
import locadora.dao.Conexao;
import locadora.dao.ExemplarDao;
import locadora.dao.FuncionarioDao;
import locadora.dao.LocacaoDao;
import locadora.dao.PerdaDao;
import locadora.dao.ReservaDao;
import locadora.dao.TituloDao;
import locadora.model.Aquisicao;
import locadora.model.Cargo;
import locadora.model.Cliente;
import locadora.model.Dvd;
import locadora.model.Exemplar;
import locadora.model.Funcionario;
import locadora.model.Situacao;
import locadora.model.Titulo;
import locadora.service.AquisicaoService;
import locadora.service.ArquivoService;
import locadora.service.AuthService;
import locadora.service.BuscaService;
import locadora.service.CapaService;
import locadora.service.ClienteService;
import locadora.service.DashboardService;
import locadora.service.FuncionarioService;
import locadora.service.LocacaoService;
import locadora.service.PerdaService;
import locadora.service.ReservaService;
import locadora.service.RegraException;
import locadora.service.TituloService;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/* Banco de teste num arquivo temporario, com DAOs, services e um relogio controlado.
   - Dados:
     cpf: devolve um cpf valido ainda nao usado neste banco
     dvd: grava um DVD com prazo de 5 dias
     cliente: grava um cliente ativo
     funcionario: grava um funcionario com a senha "123"
     adquirir: grava uma aquisicao com N exemplares disponiveis de custo 30,00
   - Sessao:
     logarGerente, logarAtendente: entram com um funcionario do cargo, criando se preciso
   - Verificacao:
     assertRegra: confere que a acao quebra uma regra com a mensagem amigavel esperada */
public class BancoTeste {

    public static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private static final List<String> CPFS = List.of(
            "239.159.970-64", "613.801.808-72", "624.734.278-01", "133.558.845-02",
            "600.518.000-20", "406.326.503-03", "081.589.085-00", "158.444.743-50",
            "668.990.851-86", "113.630.343-08", "968.042.622-04", "349.437.637-90",
            "052.384.578-20", "568.849.112-47", "041.324.506-35", "838.923.203-06");

    public final RelogioTeste relogio = new RelogioTeste(HOJE);
    public final Conexao conexao;
    public final TituloDao titulos;
    public final ClienteDao clientes;
    public final FuncionarioDao funcionarios;
    public final ExemplarDao exemplares;
    public final ReservaDao reservas;
    public final AquisicaoDao aquisicoes;
    public final LocacaoDao locacoes;
    public final PerdaDao perdas;
    public final AuthService auth;
    public final TituloService tituloService;
    public final ClienteService clienteService;
    public final FuncionarioService funcionarioService;
    public final LocacaoService locacaoService;
    public final AquisicaoService aquisicaoService;
    public final PerdaService perdaService;
    public final ReservaService reservaService;
    public final DashboardService dashboardService;
    public final BuscaService buscaService;
    public final ArquivoService arquivoService;
    public final CapaService capaService;
    private int proximoCpf;

    public BancoTeste(Path dir) {
        AuthService.sair();
        conexao = new Conexao(dir.resolve("teste.db"));
        conexao.criarSchemaSeVazio();
        titulos = new TituloDao(conexao);
        clientes = new ClienteDao(conexao);
        funcionarios = new FuncionarioDao(conexao);
        exemplares = new ExemplarDao(conexao, titulos);
        reservas = new ReservaDao(conexao, titulos, clientes, funcionarios, exemplares);
        aquisicoes = new AquisicaoDao(conexao, titulos, funcionarios, exemplares, reservas);
        locacoes = new LocacaoDao(conexao, exemplares, clientes, funcionarios, reservas);
        perdas = new PerdaDao(conexao, exemplares, funcionarios, locacoes, reservas);
        auth = new AuthService(funcionarios);
        tituloService = new TituloService(titulos, exemplares, aquisicoes, locacoes, perdas, relogio);
        clienteService = new ClienteService(clientes, locacoes, reservas);
        funcionarioService = new FuncionarioService(funcionarios);
        locacaoService = new LocacaoService(locacoes, exemplares, clientes, reservas, relogio);
        aquisicaoService = new AquisicaoService(aquisicoes, titulos, exemplares, reservas, relogio);
        perdaService = new PerdaService(perdas, exemplares, locacoes, reservas, relogio);
        reservaService = new ReservaService(reservas, titulos, clientes, exemplares, locacoes, relogio);
        dashboardService = new DashboardService(locacaoService, reservaService, tituloService);
        buscaService = new BuscaService(titulos, clientes, exemplares);
        arquivoService = new ArquivoService(conexao, relogio);
        capaService = new CapaService(titulos, "http://127.0.0.1:9");
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

    public List<Exemplar> adquirir(Titulo titulo, int quantidade, Funcionario funcionario) {
        BigDecimal custo = new BigDecimal("30.00");
        int sequencia = exemplares.proximaSequencia(titulo.getId());
        List<Exemplar> novos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++)
            novos.add(new Exemplar(0, Exemplar.codigo(titulo.getId(), sequencia + i), titulo, custo, Situacao.DISPONIVEL));
        aquisicoes.registrar(new Aquisicao(0, titulo, quantidade, custo, "Distribuidora Teste", HOJE, funcionario), novos, List.of());
        return novos;
    }

    public static void assertRegra(String mensagem, Executable acao) {
        assertEquals(mensagem, assertThrows(RegraException.class, acao).getMessage());
    }

    public Funcionario logarGerente() {
        return logar("gerente", Cargo.GERENTE);
    }

    public Funcionario logarAtendente() {
        return logar("atendente", Cargo.ATENDENTE);
    }

    private Funcionario logar(String login, Cargo cargo) {
        if (funcionarios.buscarPorLogin(login) == null)
            funcionario(login, cargo);
        return auth.entrar(login, "123");
    }
}
