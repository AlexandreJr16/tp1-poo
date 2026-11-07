package locadora.view;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.JButton;

/**
 * Botao que alterna entre tema claro e escuro; mostra a lua no claro e o sol no escuro.
 */
public final class BotaoTema {

    private BotaoTema() {
    }

    public static JButton criar() {
        JButton botao = new JButton();
        botao.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        botao.putClientProperty(FlatClientProperties.STYLE, "margin: 6,6,6,6");
        atualizarIcone(botao);
        botao.addActionListener(e -> {
            Tema.alternar();
            atualizarIcone(botao);
        });
        return botao;
    }

    private static void atualizarIcone(JButton botao) {
        botao.setIcon(Tema.escuro() ? Icone.SOL.normal() : Icone.LUA.normal());
        botao.setToolTipText(Tema.escuro() ? "Usar tema claro" : "Usar tema escuro");
    }
}
