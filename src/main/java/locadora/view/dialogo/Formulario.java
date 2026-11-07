package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Tema;
import locadora.view.componente.Botoes;
import locadora.view.componente.Eventos;
import locadora.view.componente.Fileira;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Base dos formularios em dialogo: rotulo acima de cada campo, Cancelar e o botao principal no rodape, Esc fecha e Enter confirma. O dialogo so fecha quando salvar() termina sem quebrar uma regra.
 */
public abstract class Formulario extends JDialog {

    // largura do conteudo sem as margens; as duas colunas nunca ficam mais estreitas que a metade disso
    private static final int LARGURA_DO_CONTEUDO = 432;

    private final JPanel campos = new JPanel(grade());
    private int linha;
    private boolean confirmado;

    protected Formulario(Component origem, String titulo, String textoDoBotao) {
        super(SwingUtilities.getWindowAncestor(origem), titulo, ModalityType.APPLICATION_MODAL);
        JLabel cabecalho = new JLabel(titulo);
        cabecalho.putClientProperty(FlatClientProperties.STYLE_CLASS, "h2");
        cabecalho.setBorder(BorderFactory.createEmptyBorder(0, 0, Tema.ESPACO_4, 0));

        JButton confirmar = Botoes.primario(textoDoBotao, null, this::confirmar);
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_3, 0, 0, 0));
        rodape.add(new Fileira(Tema.ESPACO_2, Botoes.secundario("Cancelar", null, this::dispose), confirmar), BorderLayout.EAST);

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

    /**
     * Valida e grava o formulario. Se lancar RegraException, a mensagem aparece e o dialogo continua aberto.
     */
    protected abstract void salvar();

    /**
     * Chamado antes de salvar; a subclasse pode pedir uma confirmacao e devolver false para desistir.
     */
    protected boolean podeSalvar() {
        return true;
    }

    /**
     * Mostra o dialogo e diz se a pessoa confirmou.
     */
    public boolean abrir() {
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return confirmado;
    }

    /**
     * HTML que quebra linha na largura do formulario.
     */
    // texto em HTML que quebra linha na largura do formulario (no HTML do Swing, pt equivale a 1 pixel)
    protected static String paragrafo(String html) {
        return "<html><div style='width: " + LARGURA_DO_CONTEUDO + "pt'>" + html + "</div></html>";
    }

    /**
     * Refaz o tamanho depois de uma mudanca no conteudo.
     */
    // reajusta a altura quando um texto ou um bloco muda com o dialogo aberto
    protected void reajustar() {
        if (isDisplayable())
            pack();
    }

    /**
     * Uma linha com um campo na largura toda.
     */
    protected void campo(String rotulo, JComponent componente) {
        adicionar(rotulo, componente, 0, 2);
        linha++;
    }

    /**
     * Uma linha com dois campos lado a lado.
     */
    protected void campos(String primeiroRotulo, JComponent primeiro, String segundoRotulo, JComponent segundo) {
        adicionar(primeiroRotulo, primeiro, 0, 1);
        adicionar(segundoRotulo, segundo, 1, 1);
        linha++;
    }

    /**
     * Texto de apoio na largura toda.
     */
    protected void nota(String texto) {
        JLabel apoio = new JLabel(paragrafo(texto));
        apoio.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        adicionar(null, apoio, 0, 2);
        linha++;
    }

    /**
     * Rotulo acima de um campo, para montar grupos de campos dentro do formulario.
     */
    // rotulo acima do campo, no mesmo padrao de todos os formularios
    protected static JPanel bloco(String rotulo, JComponent componente) {
        JPanel bloco = new JPanel(new BorderLayout(0, Tema.ESPACO_1));
        if (rotulo != null) {
            JLabel titulo = new JLabel(rotulo);
            titulo.putClientProperty(FlatClientProperties.STYLE_CLASS, "semibold");
            bloco.add(titulo, BorderLayout.NORTH);
        }
        bloco.add(componente, BorderLayout.CENTER);
        return bloco;
    }

    private void adicionar(String rotulo, JComponent componente, int coluna, int largura) {
        JPanel bloco = bloco(rotulo, componente);
        GridBagConstraints posicao = new GridBagConstraints();
        posicao.gridx = coluna;
        posicao.gridy = linha;
        posicao.gridwidth = largura;
        posicao.fill = GridBagConstraints.HORIZONTAL;
        posicao.weightx = 1;
        posicao.insets = new Insets(0, coluna == 1 ? Tema.ESPACO_3 / 2 : 0, Tema.ESPACO_3, largura == 1 && coluna == 0 ? Tema.ESPACO_3 / 2 : 0);
        campos.add(bloco, posicao);
    }

    private static GridBagLayout grade() {
        GridBagLayout grade = new GridBagLayout();
        grade.columnWidths = new int[]{LARGURA_DO_CONTEUDO / 2, LARGURA_DO_CONTEUDO / 2};
        return grade;
    }

    private void confirmar() {
        if (podeSalvar() && Mensagens.executar(this, this::salvar)) {
            confirmado = true;
            dispose();
        }
    }
}
