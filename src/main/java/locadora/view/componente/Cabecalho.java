package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Tema;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

// topo de cada tela: titulo grande e subtitulo a esquerda, botoes de acao a direita
public class Cabecalho extends JPanel {

    public Cabecalho(String titulo, String subtitulo, JComponent... acoes) {
        super(new BorderLayout(Tema.ESPACO_4, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Tema.ESPACO_2, 0));

        JLabel rotulo = new JLabel(titulo);
        rotulo.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        JLabel apoio = new JLabel(subtitulo);
        apoio.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(rotulo);
        textos.add(apoio);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACO_2, 0));
        botoes.setOpaque(false);
        for (JComponent acao : acoes)
            botoes.add(acao);

        add(textos, BorderLayout.WEST);
        add(botoes, BorderLayout.EAST);
    }
}
