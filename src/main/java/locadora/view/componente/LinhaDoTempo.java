package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.model.Evento;
import locadora.service.Formato;
import locadora.view.Tema;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Lista vertical dos eventos de um exemplar, com um trilho e uma bolinha por evento. A descricao quebra linha numa largura fixa, entao a lista nunca precisa de rolagem lateral.
 */
public class LinhaDoTempo extends JList<Evento> {

    public static final int LARGURA = 328;
    private static final int RECUO = 28;
    private static final int LARGURA_DO_TEXTO = LARGURA - RECUO - Tema.ESPACO_2 - Tema.ESPACO_3;

    private final DefaultListModel<Evento> eventos = new DefaultListModel<>();

    public LinhaDoTempo() {
        setModel(eventos);
        setCellRenderer(new Passo());
        setFocusable(false);
        putClientProperty(FlatClientProperties.STYLE, "background: $Locadora.cartao");
    }

    /**
     * Troca os eventos mostrados.
     */
    public void setEventos(List<Evento> novos) {
        eventos.clear();
        novos.forEach(eventos::addElement);
    }

    /**
     * Um passo da linha do tempo: data pequena, descricao e quem registrou.
     */
    private static class Passo extends JPanel implements ListCellRenderer<Evento> {

        private final JLabel data = new JLabel();
        private final JLabel descricao = new JLabel();
        private final JLabel autor = new JLabel();
        private boolean primeiro;
        private boolean ultimo;

        Passo() {
            super(new BorderLayout(0, 2));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_2, RECUO, Tema.ESPACO_2, Tema.ESPACO_2));
            data.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave; font: -1");
            descricao.putClientProperty(FlatClientProperties.STYLE_CLASS, "semibold");
            autor.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave; font: -1");
            add(data, BorderLayout.NORTH);
            add(descricao, BorderLayout.CENTER);
            add(autor, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Evento> lista, Evento evento, int indice, boolean selecionado, boolean foco) {
            data.setText(Formato.data(evento.data()));
            descricao.setText("<html><div style='width: " + LARGURA_DO_TEXTO + "pt'>" + evento.descricao() + "</div></html>");
            autor.setText("por " + evento.funcionario());
            primeiro = indice == 0;
            ultimo = indice == lista.getModel().getSize() - 1;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int centro = RECUO / 2 - 2;
            int bolinha = 10;
            int alturaDaBolinha = Tema.ESPACO_2 + 4;
            g2.setColor(Tema.cor("Component.borderColor"));
            g2.fillRect(centro - 1, primeiro ? alturaDaBolinha : 0, 2, ultimo ? alturaDaBolinha : getHeight());
            g2.setColor(ultimo ? Tema.destaque() : Tema.suave());
            g2.fillOval(centro - bolinha / 2, alturaDaBolinha - bolinha / 2 + 2, bolinha, bolinha);
            g2.dispose();
        }
    }
}
