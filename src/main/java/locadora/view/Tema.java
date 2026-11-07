package locadora.view;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;

/**
 * Tema visual (claro e escuro), cores lidas do UIManager na hora para acompanhar a troca de tema e espacamentos padrao das telas.
 */
public final class Tema {

    public static final int ESPACO_1 = 4;
    public static final int ESPACO_2 = 8;
    public static final int ESPACO_3 = 12;
    public static final int ESPACO_4 = 16;
    public static final int ESPACO_5 = 24;
    public static final int LARGURA_BARRA_LATERAL = 232;

    private Tema() {
    }

    /**
     * Registra os tokens da locadora (resources/locadora/tema) e liga o tema claro.
     */
    public static void instalar() {
        FlatLaf.registerCustomDefaultsSource("locadora.tema");
        FlatMacLightLaf.setup();
    }

    /**
     * Troca entre claro e escuro em todas as janelas abertas.
     */
    public static void alternar() {
        if (escuro())
            FlatMacLightLaf.setup();
        else
            FlatMacDarkLaf.setup();
        FlatLaf.updateUI();
    }

    /**
     * Diz se o tema atual e o escuro.
     */
    public static boolean escuro() {
        return FlatLaf.isLafDark();
    }

    /**
     * Cor de uma chave do tema.
     */
    public static Color cor(String chave) {
        return UIManager.getColor(chave);
    }

    /**
     * Cor "perigo" do tema atual.
     */
    public static Color perigo() {
        return cor("Locadora.perigo");
    }

    /**
     * Cor "destaque" do tema atual.
     */
    public static Color destaque() {
        return cor("Component.accentColor");
    }

    /**
     * Cor "suave" do tema atual.
     */
    public static Color suave() {
        return cor("Locadora.suave");
    }

    /**
     * Margem vazia igual nos quatro lados.
     */
    public static Border borda(int espaco) {
        return BorderFactory.createEmptyBorder(espaco, espaco, espaco, espaco);
    }
}
