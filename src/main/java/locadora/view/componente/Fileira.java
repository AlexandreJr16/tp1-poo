package locadora.view.componente;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/* Fileira horizontal (botoes, filtros, etiquetas) com espaco fixo so entre os itens,
   sem a folga nas pontas do FlowLayout, para alinhar com as bordas da tela.
   adicionar: poe mais um item no fim da fileira */
public class Fileira extends JPanel {

    private final int espaco;

    public Fileira(int espaco, JComponent... itens) {
        this.espaco = espaco;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        for (JComponent item : itens)
            adicionar(item);
    }

    public void adicionar(JComponent item) {
        if (getComponentCount() > 0)
            add(Box.createHorizontalStrut(espaco));
        add(item);
    }
}
