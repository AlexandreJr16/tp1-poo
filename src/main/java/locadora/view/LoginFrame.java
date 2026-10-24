package locadora.view;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.view.componente.Botoes;
import locadora.view.componente.Mensagens;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

// tela de entrada: arte com a marca a esquerda e o formulario de login a direita
public class LoginFrame extends JFrame {

    private final Main.Servicos servicos;
    private final JTextField login = new JTextField(22);
    private final JPasswordField senha = new JPasswordField(22);

    public LoginFrame(Main.Servicos servicos) {
        super("Locadora — Entrar");
        this.servicos = servicos;
        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.add(new Arte(), BorderLayout.WEST);
        conteudo.add(formulario(), BorderLayout.CENTER);
        setContentPane(conteudo);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JComponent formulario() {
        login.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "seu login");
        login.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icone.PESSOA.de(16, "Locadora.suave"));
        senha.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "sua senha");
        senha.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icone.CHAVE.de(16, "Locadora.suave"));
        senha.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        JButton entrar = Botoes.primario("Entrar", null, this::entrar);
        getRootPane().setDefaultButton(entrar);

        JLabel titulo = new JLabel("Bem-vindo de volta");
        titulo.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        JLabel apoio = suave("Entre com o seu login de funcionário.");
        JLabel dica = suave("Demonstração: admin / admin123 (gerente) ou joana / joana123 (atendente)");
        dica.putClientProperty(FlatClientProperties.STYLE_CLASS, "small");

        JPanel caixa = new JPanel();
        caixa.setLayout(new BoxLayout(caixa, BoxLayout.Y_AXIS));
        for (JComponent parte : new JComponent[]{titulo, apoio, espaco(Tema.ESPACO_5), rotulo("Login"), login, espaco(Tema.ESPACO_3),
                rotulo("Senha"), senha, espaco(Tema.ESPACO_5), entrar, espaco(Tema.ESPACO_4), dica}) {
            parte.setAlignmentX(Component.LEFT_ALIGNMENT);
            caixa.add(parte);
        }
        entrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, entrar.getPreferredSize().height + 6));

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(BotaoTema.criar(), BorderLayout.EAST);
        JPanel centro = new JPanel(new GridBagLayout());
        centro.add(caixa);
        JPanel lado = new JPanel(new BorderLayout());
        lado.setBorder(Tema.borda(Tema.ESPACO_4));
        lado.add(topo, BorderLayout.NORTH);
        lado.add(centro, BorderLayout.CENTER);
        return lado;
    }

    private void entrar() {
        boolean entrou = Mensagens.executar(this, () -> servicos.auth().entrar(login.getText(), new String(senha.getPassword())));
        if (entrou) {
            new MainFrame(servicos).setVisible(true);
            dispose();
        } else {
            senha.setText("");
            senha.requestFocusInWindow();
        }
    }

    private static JLabel rotulo(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.putClientProperty(FlatClientProperties.STYLE_CLASS, "semibold");
        rotulo.setBorder(BorderFactory.createEmptyBorder(0, 0, Tema.ESPACO_1, 0));
        return rotulo;
    }

    private static JLabel suave(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        return rotulo;
    }

    private static JComponent espaco(int altura) {
        return (JComponent) Box.createVerticalStrut(altura);
    }

    // painel colorido com a marca: degrade na cor de destaque e circulos translucidos de enfeite
    private static class Arte extends JPanel {

        Arte() {
            super(new GridBagLayout());
            setPreferredSize(new Dimension(380, 0));
            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            JLabel marca = new JLabel(Icone.DISCO.de(64, "Button.default.foreground"));
            JLabel nome = claro("Locadora", "h00");
            JLabel frase = claro("Títulos, locações e acervo", "h3.regular");
            JLabel midias = claro("Blu-ray · DVD · CD · Fita", "medium");
            for (JComponent parte : new JComponent[]{marca, espaco(Tema.ESPACO_4), nome, frase, espaco(Tema.ESPACO_5), midias}) {
                parte.setAlignmentX(Component.LEFT_ALIGNMENT);
                textos.add(parte);
            }
            add(textos);
        }

        private static JLabel claro(String texto, String estilo) {
            JLabel rotulo = new JLabel(texto);
            rotulo.putClientProperty(FlatClientProperties.STYLE_CLASS, estilo);
            rotulo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Button.default.foreground");
            return rotulo;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, Tema.destaque(), getWidth(), getHeight(), Tema.destaque().darker().darker()));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
            g2.setColor(Tema.cor("Button.default.foreground"));
            g2.fill(new Ellipse2D.Float(getWidth() - 220, -120, 360, 360));
            g2.fill(new Ellipse2D.Float(-140, getHeight() - 200, 320, 320));
            g2.dispose();
        }
    }
}
