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
import locadora.service.TituloService;
import locadora.view.LoginFrame;
import locadora.view.Tema;
import locadora.view.componente.Mensagens;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Ponto de entrada: liga o tema, prepara o banco (cria e popula com dados e capas na primeira execucao) e abre o login.
 */
public class Main {

    private static final Path BANCO = Path.of("locadora.db");
    private static final String WIKIPEDIA = "https://pt.wikipedia.org/w/api.php";

    public record Servicos(AuthService auth, TituloService titulos, ClienteService clientes, FuncionarioService funcionarios,
                           LocacaoService locacoes, AquisicaoService aquisicoes, PerdaService perdas, ReservaService reservas,
                           DashboardService painel, BuscaService busca, ArquivoService arquivos, CapaService capas) {
    }

    /**
     * Liga o tema, prepara o banco e abre a tela de login.
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((linha, erro) -> SwingUtilities.invokeLater(() -> Mensagens.inesperado(null, erro)));
        Tema.instalar();
        Servicos servicos = montar(BANCO, Clock.systemDefaultZone());
        SwingUtilities.invokeLater(() -> new LoginFrame(servicos).setVisible(true));
    }

    /**
     * Cria a conexao, os DAOs e os services ja ligados entre si.
     */
    public static Servicos montar(Path arquivo, Clock relogio) {
        Conexao conexao = new Conexao(arquivo);
        boolean novo = conexao.criarSchemaSeVazio();
        if (novo)
            conexao.executarScript("/seed.sql");
        TituloDao titulos = new TituloDao(conexao);
        CapaService capas = new CapaService(titulos, WIKIPEDIA);
        if (novo)
            capas.carregarEmbutidas();
        ClienteDao clientes = new ClienteDao(conexao);
        FuncionarioDao funcionarios = new FuncionarioDao(conexao);
        ExemplarDao exemplares = new ExemplarDao(conexao, titulos);
        ReservaDao reservas = new ReservaDao(conexao, titulos, clientes, funcionarios, exemplares);
        AquisicaoDao aquisicoes = new AquisicaoDao(conexao, titulos, funcionarios, exemplares, reservas);
        LocacaoDao locacoes = new LocacaoDao(conexao, exemplares, clientes, funcionarios, reservas, titulos);
        PerdaDao perdas = new PerdaDao(conexao, exemplares, funcionarios, locacoes, reservas);

        TituloService tituloService = new TituloService(titulos, exemplares, aquisicoes, locacoes, perdas, relogio);
        LocacaoService locacaoService = new LocacaoService(locacoes, exemplares, clientes, reservas, relogio);
        ReservaService reservaService = new ReservaService(reservas, titulos, clientes, exemplares, locacoes, relogio);
        return new Servicos(new AuthService(funcionarios), tituloService, new ClienteService(clientes, locacoes, reservas),
                new FuncionarioService(funcionarios), locacaoService, new AquisicaoService(aquisicoes, titulos, exemplares, reservas, relogio),
                new PerdaService(perdas, exemplares, locacoes, reservas, relogio), reservaService,
                new DashboardService(locacaoService, reservaService, tituloService), new BuscaService(titulos, clientes, exemplares),
                new ArquivoService(conexao, relogio), capas);
    }
}
