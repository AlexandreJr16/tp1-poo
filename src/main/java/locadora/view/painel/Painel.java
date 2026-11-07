package locadora.view.painel;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Tema;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Base das telas da janela principal: margem padrao, fundo do conteudo e atualizar chamado sempre que a tela aparece.
 */
public abstract class Painel extends JPanel {

    protected Painel() {
        super(new BorderLayout(0, Tema.ESPACO_4));
        setBorder(Tema.borda(Tema.ESPACO_5));
        putClientProperty(FlatClientProperties.STYLE, "background: $Locadora.fundo");
    }

    /**
     * Recarrega os dados da tela; chamado sempre que ela aparece.
     */
    public abstract void atualizar();
}
