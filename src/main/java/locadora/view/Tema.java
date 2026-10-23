package locadora.view;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;

/* - Tema:
     instalar: registra os tokens da locadora (resources/locadora/tema) e liga o tema claro
     alternar: troca entre claro e escuro em todas as janelas abertas
     escuro: diz se o tema atual e o escuro
   - Cores (lidas do UIManager na hora, para acompanhar a troca de tema):
     cor: cor de uma chave do tema
     perigo, sucesso, aviso, destaque, suave
   - Espacamento:
     borda: margem vazia igual nos quatro lados */
public final class Tema {

    public static final int ESPACO_1 = 4;
    public static final int ESPACO_2 = 8;
    public static final int ESPACO_3 = 12;
    public static final int ESPACO_4 = 16;
    public static final int ESPACO_5 = 24;
    public static final int ESPACO_6 = 32;
    public static final int LARGURA_BARRA_LATERAL = 232;

    private Tema() {
    }

    public static void instalar() {
        FlatLaf.registerCustomDefaultsSource("locadora.tema");
        FlatMacLightLaf.setup();
    }

    public static void alternar() {
        if (escuro())
            FlatMacLightLaf.setup();
        else
            FlatMacDarkLaf.setup();
        FlatLaf.updateUI();
    }

    public static boolean escuro() {
        return FlatLaf.isLafDark();
    }

    public static Color cor(String chave) {
        return UIManager.getColor(chave);
    }

    public static Color perigo() {
        return cor("Locadora.perigo");
    }

    public static Color sucesso() {
        return cor("Locadora.sucesso");
    }

    public static Color aviso() {
        return cor("Locadora.aviso");
    }

    public static Color destaque() {
        return cor("Component.accentColor");
    }

    public static Color suave() {
        return cor("Locadora.suave");
    }

    public static Border borda(int espaco) {
        return BorderFactory.createEmptyBorder(espaco, espaco, espaco, espaco);
    }
}
