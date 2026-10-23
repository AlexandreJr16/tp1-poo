package locadora.view;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Funcionario;
import locadora.service.AuthService;
import locadora.service.BuscaService;
import locadora.view.componente.BarraLateral;
import locadora.view.componente.Eventos;
import locadora.view.componente.Mensagens;
import locadora.view.componente.Toast;
import locadora.view.dialogo.BuscaGlobalDialog;
import locadora.view.dialogo.SenhaDialog;
import locadora.view.painel.AquisicoesPanel;
import locadora.view.painel.ClientesPanel;
import locadora.view.painel.DashboardPanel;
import locadora.view.painel.FuncionariosPanel;
import locadora.view.painel.LocacoesPanel;
import locadora.view.painel.Painel;
import locadora.view.painel.PerdasPanel;
import locadora.view.painel.ReservasPanel;
import locadora.view.painel.TitulosPanel;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/* Janela principal: barra lateral com as telas que o cargo do funcionario pode usar e o conteudo trocado por CardLayout.
   - Navegacao:
     mostrar: abre uma tela pelo nome e recarrega os dados dela
   - Atalhos: Ctrl+K abre a busca global e Ctrl+1 a Ctrl+8 abrem as telas na ordem da barra lateral */
public class MainFrame extends JFrame {

    public static final String PAINEL = "Painel";
    public static final String TITULOS = "Títulos";
    public static final String CLIENTES = "Clientes";
    public static final String LOCACOES = "Locações";
    public static final String RESERVAS = "Reservas";
    public static final String AQUISICOES = "Aquisições";
    public static final String PERDAS = "Perdas";
    public static final String FUNCIONARIOS = "Funcionários";

    private final Main.Servicos servicos;
    private final CardLayout cartoes = new CardLayout();
    private final JPanel conteudo = new JPanel(cartoes);
    private final Map<String, Painel> paineis = new LinkedHashMap<>();
    private final BarraLateral barra;
    private final TitulosPanel titulos;
    private final ClientesPanel clientes;
    private String atual;

    public MainFrame(Main.Servicos servicos) {
        super("Locadora");
        this.servicos = servicos;
        Funcionario logado = AuthService.logado();
        barra = new BarraLateral(logado, this::abrirBusca);
        titulos = new TitulosPanel(servicos);
        clientes = new ClientesPanel(servicos);

        adicionar(PAINEL, Icone.PAINEL, new DashboardPanel(servicos, () -> mostrar(LOCACOES)));
        adicionar(LOCACOES, Icone.TROCA, new LocacoesPanel(servicos));
        adicionar(TITULOS, Icone.DISCO, titulos);
        adicionar(CLIENTES, Icone.PESSOA, clientes);
        adicionar(RESERVAS, Icone.MARCADOR, new ReservasPanel(servicos));
        if (logado.gerente()) {
            adicionar(AQUISICOES, Icone.CAIXA, new AquisicoesPanel(servicos));
            adicionar(PERDAS, Icone.ALERTA, new PerdasPanel(servicos));
            adicionar(FUNCIONARIOS, Icone.CRACHA, new FuncionariosPanel(servicos));
        }
        barra.adicionarAcao(BotaoTema.criar());
        barra.adicionarAcao(botaoDoMenu(logado));
        Eventos.tecla(getRootPane(), "ctrl K", this::abrirBusca);

        JPanel janela = new JPanel(new BorderLayout());
        janela.add(barra, BorderLayout.WEST);
        janela.add(conteudo, BorderLayout.CENTER);
        setContentPane(janela);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setSize(1360, 840);
        setLocationRelativeTo(null);
        mostrar(PAINEL);
    }

    public void mostrar(String nome) {
        atual = nome;
        Mensagens.executar(this, () -> paineis.get(nome).atualizar());
        cartoes.show(conteudo, nome);
        barra.marcar(nome);
    }

    private void adicionar(String nome, Icone icone, Painel painel) {
        int posicao = paineis.size() + 1;
        paineis.put(nome, painel);
        conteudo.add(painel, nome);
        barra.adicionarItem(nome, icone, "Ctrl+" + posicao, () -> mostrar(nome));
        Eventos.tecla(getRootPane(), "ctrl " + posicao, () -> mostrar(nome));
    }

    private void abrirBusca() {
        new BuscaGlobalDialog(this, servicos.busca(), this::abrirResultado).setVisible(true);
    }

    private void abrirResultado(BuscaService.Resultado resultado) {
        if (resultado.tipo() == BuscaService.Tipo.CLIENTE) {
            mostrar(CLIENTES);
            clientes.abrir(resultado.id());
        } else {
            mostrar(TITULOS);
            titulos.abrir(resultado.id());
        }
    }

    private JButton botaoDoMenu(Funcionario logado) {
        JButton menu = new JButton(Icone.LISTA.normal());
        menu.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        menu.putClientProperty(FlatClientProperties.STYLE, "margin: 6,6,6,6");
        menu.setToolTipText("Conta e banco de dados");
        menu.addActionListener(e -> {
            JPopupMenu opcoes = new JPopupMenu();
            opcoes.add(item("Trocar minha senha", Icone.CHAVE, () -> new SenhaDialog(this, servicos.funcionarios(), null).abrir()));
            opcoes.add(item("Fazer backup do banco", Icone.BANCO, this::fazerBackup));
            if (logado.gerente())
                opcoes.add(item("Restaurar um backup", Icone.EXPORTAR, this::restaurarBackup));
            opcoes.addSeparator();
            opcoes.add(item("Sair", Icone.SAIR, this::sair));
            opcoes.show(menu, 0, -opcoes.getPreferredSize().height);
        });
        return menu;
    }

    private static JMenuItem item(String texto, Icone icone, Runnable acao) {
        JMenuItem item = new JMenuItem(texto, icone.de(16, "Label.foreground"));
        item.addActionListener(e -> acao.run());
        return item;
    }

    private void fazerBackup() {
        JFileChooser escolha = new JFileChooser();
        escolha.setSelectedFile(new File(servicos.arquivos().nomeSugeridoDeBackup()));
        if (escolha.showSaveDialog(this) == JFileChooser.APPROVE_OPTION
                && Mensagens.executar(this, () -> servicos.arquivos().fazerBackup(escolha.getSelectedFile().toPath())))
            Toast.mostrar(this, "Backup salvo em " + escolha.getSelectedFile().getName());
    }

    private void restaurarBackup() {
        JFileChooser escolha = new JFileChooser();
        if (escolha.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        String aviso = "Os dados atuais serão substituídos pelos de " + escolha.getSelectedFile().getName() + ".\nEsta ação não pode ser desfeita.";
        if (Mensagens.confirmar(this, "Restaurar backup", aviso, "Restaurar")
                && Mensagens.executar(this, () -> servicos.arquivos().restaurar(escolha.getSelectedFile().toPath()))) {
            mostrar(atual);
            Toast.mostrar(this, "Backup restaurado");
        }
    }

    private void sair() {
        AuthService.sair();
        dispose();
        new LoginFrame(servicos).setVisible(true);
    }
}
