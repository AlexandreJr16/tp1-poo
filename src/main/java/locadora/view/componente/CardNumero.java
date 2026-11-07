package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Icone;
import locadora.view.Tema;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Card do painel com um numero em destaque.
 */
public class CardNumero extends JPanel {

    private final JLabel valor = new JLabel("—");

    public CardNumero(Icone icone, String rotulo, String chaveDaCor) {
        super(new BorderLayout(0, Tema.ESPACO_2));
        putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: $Locadora.cartao");
        setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_4, Tema.ESPACO_4 + 2, Tema.ESPACO_4, Tema.ESPACO_4 + 2));

        JLabel titulo = new JLabel(rotulo, icone.de(18, chaveDaCor), JLabel.LEADING);
        titulo.setIconTextGap(Tema.ESPACO_2);
        titulo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        valor.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");

        add(titulo, BorderLayout.NORTH);
        add(valor, BorderLayout.CENTER);
    }

    /**
     * Troca o numero mostrado.
     */
    public void setValor(String texto) {
        valor.setText(texto);
    }

    /**
     * Pinta o numero com a cor de perigo quando ele pede atencao.
     */
    public void setAlerta(boolean alerta) {
        valor.putClientProperty(FlatClientProperties.STYLE, alerta ? "foreground: $Locadora.perigo" : null);
    }
}
