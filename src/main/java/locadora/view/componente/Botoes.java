package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Icone;

import javax.swing.JButton;

/* Botoes padrao das telas.
   primario: acao principal, na cor de destaque
   secundario: acao comum, com borda
   discreto: acao leve, sem borda (barras e rodapes) */
public final class Botoes {

    private Botoes() {
    }

    public static JButton primario(String texto, Icone icone, Runnable acao) {
        JButton botao = criar(texto, icone, "Button.default.foreground", acao);
        botao.putClientProperty(FlatClientProperties.STYLE,
                "background: $Button.default.background; foreground: $Button.default.foreground; borderColor: $Button.default.borderColor;"
                        + " focusedBorderColor: $Button.default.focusedBorderColor; hoverBackground: $Button.default.hoverBackground;"
                        + " pressedBackground: $Button.default.pressedBackground; font: bold; margin: 6,14,6,14");
        return botao;
    }

    public static JButton secundario(String texto, Icone icone, Runnable acao) {
        JButton botao = criar(texto, icone, "Label.foreground", acao);
        botao.putClientProperty(FlatClientProperties.STYLE, "margin: 6,14,6,14");
        return botao;
    }

    public static JButton discreto(String texto, Icone icone, Runnable acao) {
        JButton botao = criar(texto, icone, "Label.foreground", acao);
        botao.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        botao.putClientProperty(FlatClientProperties.STYLE, "margin: 6,10,6,10");
        return botao;
    }

    private static JButton criar(String texto, Icone icone, String corDoIcone, Runnable acao) {
        JButton botao = new JButton(texto);
        if (icone != null)
            botao.setIcon(icone.de(16, corDoIcone));
        botao.setIconTextGap(8);
        botao.addActionListener(e -> acao.run());
        return botao;
    }
}
