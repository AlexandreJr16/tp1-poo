package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.model.Funcionario;
import locadora.view.Icone;
import locadora.view.Tema;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

/* Barra lateral fixa: marca e atalho de busca no topo, um item por tela e o funcionario logado no rodape.
   adicionarItem: cria o botao de uma tela
   marcar: deixa selecionado o item da tela aberta
   adicionarAcao: botao pequeno no rodape (tema, menu) */
public class BarraLateral extends JPanel {

    private static final int ALTURA_DO_ITEM = 40;

    private final JPanel itens = new JPanel();
    private final JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACO_1, 0));
    private final ButtonGroup grupo = new ButtonGroup();
    private final Map<String, JToggleButton> botoes = new HashMap<>();

    public BarraLateral(Funcionario logado, Runnable abrirBusca) {
        super(new BorderLayout());
        putClientProperty(FlatClientProperties.STYLE, "background: $Locadora.barraLateral");
        setPreferredSize(new Dimension(Tema.LARGURA_BARRA_LATERAL, 0));

        itens.setOpaque(false);
        itens.setLayout(new BoxLayout(itens, BoxLayout.Y_AXIS));
        itens.setBorder(BorderFactory.createEmptyBorder(0, Tema.ESPACO_3, 0, Tema.ESPACO_3));

        JPanel topo = new JPanel(new BorderLayout(0, Tema.ESPACO_4));
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_5, Tema.ESPACO_3, Tema.ESPACO_4, Tema.ESPACO_3));
        topo.add(marca(), BorderLayout.NORTH);
        topo.add(botaoDeBusca(abrirBusca), BorderLayout.SOUTH);

        add(topo, BorderLayout.NORTH);
        add(itens, BorderLayout.CENTER);
        add(rodape(logado), BorderLayout.SOUTH);
    }

    public void adicionarItem(String nome, Icone icone, String atalho, Runnable acao) {
        JToggleButton botao = new JToggleButton(nome, icone.normal());
        botao.setSelectedIcon(icone.de(18, "Component.accentColor"));
        botao.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        botao.putClientProperty(FlatClientProperties.STYLE, "toolbar.selectedBackground: $Locadora.selecao;"
                + " toolbar.selectedForeground: $Component.accentColor; margin: 0,12,0,12; iconTextGap: 12; font: +1");
        botao.setHorizontalAlignment(SwingConstants.LEADING);
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTURA_DO_ITEM));
        botao.setAlignmentX(LEFT_ALIGNMENT);
        botao.setToolTipText(nome + " (" + atalho + ")");
        botao.addActionListener(e -> acao.run());
        grupo.add(botao);
        botoes.put(nome, botao);
        itens.add(botao);
        itens.add(Box.createVerticalStrut(2));
    }

    public void marcar(String nome) {
        botoes.get(nome).setSelected(true);
    }

    public void adicionarAcao(JButton acao) {
        acoes.add(acao);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Tema.cor("Component.borderColor"));
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
    }

    private JComponent marca() {
        JLabel nome = new JLabel("Locadora", Icone.DISCO.de(30, "Component.accentColor"), SwingConstants.LEADING);
        nome.setIconTextGap(Tema.ESPACO_3);
        nome.putClientProperty(FlatClientProperties.STYLE_CLASS, "h2");
        nome.setBorder(BorderFactory.createEmptyBorder(0, Tema.ESPACO_2, 0, 0));
        return nome;
    }

    private JComponent botaoDeBusca(Runnable abrirBusca) {
        JButton busca = new JButton("Buscar...", Icone.LUPA.de(16, "Locadora.suave"));
        busca.setHorizontalAlignment(SwingConstants.LEADING);
        busca.setIconTextGap(Tema.ESPACO_2);
        busca.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave; margin: 7,10,7,10; background: $Locadora.cartao");
        busca.setToolTipText("Buscar título, cliente ou exemplar (Ctrl+K)");
        busca.addActionListener(e -> abrirBusca.run());
        JLabel atalho = new JLabel("Ctrl+K");
        atalho.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave; font: -2");
        busca.setLayout(new BorderLayout());
        busca.add(atalho, BorderLayout.EAST);
        return busca;
    }

    private JComponent rodape(Funcionario logado) {
        JLabel nome = new JLabel(logado.getNome());
        nome.putClientProperty(FlatClientProperties.STYLE_CLASS, "semibold");
        JLabel cargo = new JLabel(logado.gerente() ? "Gerente" : "Atendente");
        cargo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave; font: -1");
        JPanel quem = new JPanel();
        quem.setOpaque(false);
        quem.setLayout(new BoxLayout(quem, BoxLayout.Y_AXIS));
        quem.add(nome);
        quem.add(cargo);

        acoes.setOpaque(false);
        JPanel rodape = new JPanel(new BorderLayout(Tema.ESPACO_2, 0));
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_4, Tema.ESPACO_5, Tema.ESPACO_4, Tema.ESPACO_3));
        rodape.add(new JLabel(Icone.PESSOA.de(22, "Locadora.suave")), BorderLayout.WEST);
        rodape.add(quem, BorderLayout.CENTER);
        rodape.add(acoes, BorderLayout.EAST);
        return rodape;
    }
}
