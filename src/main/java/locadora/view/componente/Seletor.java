package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.service.Formato;
import locadora.view.Icone;
import locadora.view.Tema;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/* Escolha de um item numa lista com busca, usada nos dialogos (cliente, exemplar, titulo).
   A lista nunca fica em branco: sem itens, mostra um aviso no lugar.
   setItens: troca os itens oferecidos
   setAvisoSemItens: frase mostrada quando nao ha nada para escolher
   selecionado: item escolhido, ou nulo
   aoSelecionar: acao a cada troca de escolha */
public class Seletor<T> extends JPanel {

    private final JTextField busca = new JTextField();
    private final DefaultListModel<T> visiveis = new DefaultListModel<>();
    private final JList<T> lista = new JList<>(visiveis);
    private final JScrollPane rolagem = new JScrollPane(lista, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private final JLabel aviso = new JLabel("", SwingConstants.CENTER);
    private final Function<T, String> rotulo;
    private final Function<T, String> detalhe;
    private List<T> itens = List.of();
    private String avisoSemItens = "Nada para escolher.";

    public Seletor(String dicaDaBusca, Function<T, String> rotulo, Function<T, String> detalhe) {
        super(new BorderLayout(0, Tema.ESPACO_1));
        this.rotulo = rotulo;
        this.detalhe = detalhe;
        busca.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, dicaDaBusca);
        busca.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icone.LUPA.de(16, "Locadora.suave"));
        busca.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        Eventos.aoDigitar(busca, this::filtrar);
        Eventos.teclaNoComponente(busca, "DOWN", () -> mover(1));
        Eventos.teclaNoComponente(busca, "UP", () -> mover(-1));

        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setVisibleRowCount(5);
        lista.putClientProperty(FlatClientProperties.STYLE, "selectionArc: 8; selectionInsets: 0,4,0,4;"
                + " selectionBackground: $Locadora.selecao; selectionForeground: $List.foreground;"
                + " selectionInactiveBackground: $Locadora.selecao; selectionInactiveForeground: $List.foreground");
        lista.setCellRenderer(new Linha());
        aviso.setOpaque(true);
        aviso.putClientProperty(FlatClientProperties.STYLE, "background: $List.background; foreground: $Locadora.suave");
        rolagem.setPreferredSize(new Dimension(420, 150));

        add(busca, BorderLayout.NORTH);
        add(rolagem, BorderLayout.CENTER);
        filtrar();
    }

    public void setItens(List<T> itens) {
        this.itens = List.copyOf(itens);
        filtrar();
    }

    public void setAvisoSemItens(String texto) {
        avisoSemItens = texto;
        filtrar();
    }

    public T selecionado() {
        return lista.getSelectedValue();
    }

    public void aoSelecionar(Consumer<T> acao) {
        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                acao.accept(selecionado());
        });
    }

    private void filtrar() {
        T anterior = selecionado();
        String termo = busca.getText();
        visiveis.clear();
        itens.stream()
                .filter(item -> Formato.parecido(termo, rotulo.apply(item) + " " + detalhe.apply(item)))
                .forEach(visiveis::addElement);
        if (anterior != null && visiveis.contains(anterior))
            lista.setSelectedValue(anterior, true);
        else if (visiveis.size() == 1)
            lista.setSelectedIndex(0);
        aviso.setText(itens.isEmpty() ? avisoSemItens : "Nada encontrado para essa busca.");
        rolagem.setViewportView(visiveis.isEmpty() ? aviso : lista);
    }

    private void mover(int passo) {
        int alvo = Math.max(0, Math.min(visiveis.size() - 1, lista.getSelectedIndex() + passo));
        if (!visiveis.isEmpty()) {
            lista.setSelectedIndex(alvo);
            lista.ensureIndexIsVisible(alvo);
        }
    }

    // cada linha mostra o rotulo em negrito e o detalhe em cinza; a escolhida ganha o fundo suave de destaque
    private class Linha extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice, boolean selecionada, boolean foco) {
            super.getListCellRendererComponent(lista, valor, indice, selecionada, false);
            @SuppressWarnings("unchecked")
            T item = (T) valor;
            String cor = String.format("#%06x", Tema.suave().getRGB() & 0xffffff);
            setText("<html><b>" + rotulo.apply(item) + "</b>&nbsp;&nbsp;<span style='color:" + cor + "'>" + detalhe.apply(item) + "</span></html>");
            setBorder(Tema.borda(Tema.ESPACO_2));
            return this;
        }
    }
}
