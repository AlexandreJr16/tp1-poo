package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Tema;
import locadora.view.componente.Botoes;
import locadora.view.componente.Eventos;
import locadora.view.componente.Mensagens;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/* Base dos formularios em dialogo: rotulo acima de cada campo, Cancelar e o botao principal no rodape,
   Esc fecha e Enter confirma. O dialogo so fecha quando salvar() termina sem quebrar uma regra.
   - Montagem (pelas subclasses):
     campo: uma linha com um campo na largura toda
     campos: uma linha com dois campos lado a lado
     nota: texto de apoio na largura toda
   - Uso:
     abrir: mostra o dialogo e diz se a pessoa confirmou */
public abstract class Formulario extends JDialog {

    private static final int LARGURA_MINIMA = 480;

    private final JPanel campos = new JPanel(new GridBagLayout());
    private int linha;
    private boolean confirmado;

    protected Formulario(Component origem, String titulo, String textoDoBotao) {
        super(SwingUtilities.getWindowAncestor(origem), titulo, ModalityType.APPLICATION_MODAL);
        JLabel cabecalho = new JLabel(titulo);
        cabecalho.putClientProperty(FlatClientProperties.STYLE_CLASS, "h2");
        cabecalho.setBorder(BorderFactory.createEmptyBorder(0, 0, Tema.ESPACO_4, 0));

        JButton confirmar = Botoes.primario(textoDoBotao, null, this::confirmar);
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACO_2, 0));
        rodape.setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_3, 0, 0, 0));
        rodape.add(Botoes.secundario("Cancelar", null, this::dispose));
        rodape.add(confirmar);

        JPanel corpo = new JPanel(new BorderLayout());
        corpo.setBorder(Tema.borda(Tema.ESPACO_5));
        corpo.add(cabecalho, BorderLayout.NORTH);
        corpo.add(campos, BorderLayout.CENTER);
        corpo.add(rodape, BorderLayout.SOUTH);
        setContentPane(corpo);

        getRootPane().setDefaultButton(confirmar);
        Eventos.tecla(getRootPane(), "ESCAPE", this::dispose);
        setResizable(false);
    }

    protected abstract void salvar();

    // chamado antes de salvar; a subclasse pode pedir uma confirmacao e devolver false para desistir
    protected boolean podeSalvar() {
        return true;
    }

    public boolean abrir() {
        pack();
        setSize(new Dimension(Math.max(getWidth(), LARGURA_MINIMA), getHeight()));
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return confirmado;
    }

    protected void campo(String rotulo, JComponent componente) {
        adicionar(rotulo, componente, 0, 2);
        linha++;
    }

    protected void campos(String primeiroRotulo, JComponent primeiro, String segundoRotulo, JComponent segundo) {
        adicionar(primeiroRotulo, primeiro, 0, 1);
        adicionar(segundoRotulo, segundo, 1, 1);
        linha++;
    }

    protected void nota(String texto) {
        JLabel apoio = new JLabel("<html><div style='width: 400px'>" + texto + "</div></html>");
        apoio.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        adicionar(null, apoio, 0, 2);
        linha++;
    }

    private void adicionar(String rotulo, JComponent componente, int coluna, int largura) {
        JPanel bloco = new JPanel(new BorderLayout(0, Tema.ESPACO_1));
        if (rotulo != null) {
            JLabel titulo = new JLabel(rotulo);
            titulo.putClientProperty(FlatClientProperties.STYLE_CLASS, "semibold");
            bloco.add(titulo, BorderLayout.NORTH);
        }
        bloco.add(componente, BorderLayout.CENTER);
        GridBagConstraints posicao = new GridBagConstraints();
        posicao.gridx = coluna;
        posicao.gridy = linha;
        posicao.gridwidth = largura;
        posicao.fill = GridBagConstraints.HORIZONTAL;
        posicao.weightx = 1;
        posicao.insets = new Insets(0, coluna == 1 ? Tema.ESPACO_3 : 0, Tema.ESPACO_3, 0);
        campos.add(bloco, posicao);
    }

    private void confirmar() {
        if (podeSalvar() && Mensagens.executar(this, this::salvar)) {
            confirmado = true;
            dispose();
        }
    }
}
