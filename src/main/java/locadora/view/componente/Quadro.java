package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Icone;
import locadora.view.Tema;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/* Card do painel com titulo, contador e uma tabela curta, ou um aviso quando a lista esta vazia.
   setDados: troca as linhas e o contador
   destacar: linhas em vermelho
   aoAbrir: acao do duplo clique numa linha */
public class Quadro<T> extends JPanel {

    private final ModeloTabela<T> modelo;
    private final JTable tabela;
    private final CelulaTabela<T> celula;
    private final JLabel contador = new JLabel();
    private final CardLayout cartoes = new CardLayout();
    private final JPanel corpo = new JPanel(cartoes);

    public Quadro(String titulo, Icone icone, String chaveDaCor, List<ModeloTabela.Coluna<T>> colunas, String textoVazio) {
        super(new BorderLayout(0, Tema.ESPACO_3));
        putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: $Locadora.cartao");
        setBorder(Tema.borda(Tema.ESPACO_4));
        modelo = new ModeloTabela<>(colunas);
        tabela = new JTable(modelo);
        celula = CelulaTabela.aplicar(tabela, modelo);

        JLabel nome = new JLabel(titulo, icone.de(18, chaveDaCor), SwingConstants.LEADING);
        nome.setIconTextGap(Tema.ESPACO_2);
        nome.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
        contador.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave; font: bold");
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(nome, BorderLayout.WEST);
        topo.add(contador, BorderLayout.EAST);

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        JLabel vazio = new JLabel(textoVazio, Icone.CONFIRMAR.de(18, "Locadora.sucesso"), SwingConstants.CENTER);
        vazio.setIconTextGap(Tema.ESPACO_2);
        vazio.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        corpo.setOpaque(false);
        corpo.add(rolagem, "tabela");
        corpo.add(vazio, "vazio");

        add(topo, BorderLayout.NORTH);
        add(corpo, BorderLayout.CENTER);
    }

    public void setDados(List<T> dados) {
        modelo.setLinhas(dados);
        contador.setText(String.valueOf(dados.size()));
        CelulaTabela.ajustarLarguras(tabela);
        cartoes.show(corpo, dados.isEmpty() ? "vazio" : "tabela");
    }

    public void destacar(Predicate<T> criterio) {
        celula.destacar(criterio);
    }

    public void aoAbrir(Consumer<T> acao) {
        Eventos.duploClique(tabela, () -> {
            if (tabela.getSelectedRow() >= 0)
                acao.accept(modelo.linha(tabela.convertRowIndexToModel(tabela.getSelectedRow())));
        });
    }
}
