package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Icone;
import locadora.view.Tema;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

// aviso de sucesso que aparece no canto inferior direito da janela e some sozinho
public final class Toast {

    private static final int DURACAO_MS = 2600;

    private Toast() {
    }

    public static void mostrar(Component origem, String mensagem) {
        JRootPane raiz = SwingUtilities.getRootPane(origem);
        if (raiz == null)
            return;
        JLabel texto = new JLabel(mensagem, Icone.CONFIRMAR.de(16, "Locadora.sucesso"), JLabel.LEADING);
        texto.setIconTextGap(Tema.ESPACO_2);
        texto.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.toastTexto; font: bold");
        JPanel aviso = new JPanel(new BorderLayout());
        aviso.putClientProperty(FlatClientProperties.STYLE, "arc: 14; background: $Locadora.toast");
        aviso.setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_3, Tema.ESPACO_4, Tema.ESPACO_3, Tema.ESPACO_5));
        aviso.add(texto);

        JLayeredPane camada = raiz.getLayeredPane();
        Dimension tamanho = aviso.getPreferredSize();
        aviso.setSize(tamanho);
        aviso.setLocation(camada.getWidth() - tamanho.width - Tema.ESPACO_5, camada.getHeight() - tamanho.height - Tema.ESPACO_5);
        camada.add(aviso, JLayeredPane.POPUP_LAYER);
        camada.repaint();

        Timer sumir = new Timer(DURACAO_MS, e -> {
            camada.remove(aviso);
            camada.repaint();
        });
        sumir.setRepeats(false);
        sumir.start();
    }
}
