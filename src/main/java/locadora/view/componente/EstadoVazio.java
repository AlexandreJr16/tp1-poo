package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Icone;
import locadora.view.Tema;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

// aviso no lugar de uma lista vazia: icone grande, frase curta, dica e uma acao opcional
public class EstadoVazio extends JPanel {

    public EstadoVazio(Icone icone, String titulo, String dica, JButton acao) {
        super(new GridBagLayout());
        setOpaque(false);
        GridBagConstraints posicao = new GridBagConstraints();
        posicao.gridx = 0;
        posicao.insets = new Insets(0, 0, Tema.ESPACO_2, 0);

        add(new JLabel(icone.de(48, "Locadora.suave")), posicao);
        JLabel frase = new JLabel(titulo, SwingConstants.CENTER);
        frase.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        add(frase, posicao);
        JLabel apoio = new JLabel(dica, SwingConstants.CENTER);
        apoio.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        add(apoio, posicao);
        if (acao != null) {
            posicao.insets = new Insets(Tema.ESPACO_3, 0, 0, 0);
            add(acao, posicao);
        }
    }
}
