package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.view.Tema;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Celula padrao das tabelas: valores ja formatados, numeros a direita, travessao cinza na celula vazia e linhas em destaque em vermelho.
 */
public class CelulaTabela<T> extends DefaultTableCellRenderer {

    private static final int LINHAS_MEDIDAS = 60;
    private static final String VAZIO = "—";

    private final ModeloTabela<T> modelo;
    private Predicate<T> destaque = item -> false;
    private Function<T, Icon> icone = item -> null;

    private CelulaTabela(ModeloTabela<T> modelo) {
        this.modelo = modelo;
    }

    /**
     * Configura a tabela inteira (celulas e cabecalho) com o visual da locadora e devolve a celula usada.
     */
    public static <T> CelulaTabela<T> aplicar(JTable tabela, ModeloTabela<T> modelo) {
        CelulaTabela<T> celula = new CelulaTabela<>(modelo);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.putClientProperty(FlatClientProperties.STYLE, "selectionArc: 8; selectionInsets: 0,4,0,4");
        for (Class<?> tipo : List.of(Object.class, String.class, Integer.class, LocalDate.class, BigDecimal.class))
            tabela.setDefaultRenderer(tipo, celula);
        alinharCabecalho(tabela);
        return celula;
    }

    /**
     * Cabecalho alinhado como as celulas: numeros a direita, o resto a esquerda.
     */
    private static void alinharCabecalho(JTable tabela) {
        TableCellRenderer padrao = tabela.getTableHeader().getDefaultRenderer();
        tabela.getTableHeader().setDefaultRenderer((grade, valor, selecionada, foco, linha, coluna) -> {
            Component componente = padrao.getTableCellRendererComponent(grade, valor, selecionada, foco, linha, coluna);
            if (componente instanceof JLabel rotulo)
                rotulo.setHorizontalAlignment(Number.class.isAssignableFrom(grade.getColumnClass(coluna)) ? RIGHT : LEADING);
            return componente;
        });
    }

    /**
     * Reparte a largura das colunas conforme o tamanho do conteudo.
     */
    // larguras proporcionais ao conteudo: cada coluna pede o espaco do maior texto entre o cabecalho e as primeiras linhas;
    // datas, numeros e dinheiro nunca encolhem abaixo disso para nao aparecerem cortados
    public static void ajustarLarguras(JTable tabela) {
        for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {
            TableColumn atual = tabela.getColumnModel().getColumn(coluna);
            int largura = tabela.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(tabela, atual.getHeaderValue(), false, false, -1, coluna).getPreferredSize().width;
            for (int linha = 0; linha < Math.min(tabela.getRowCount(), LINHAS_MEDIDAS); linha++)
                largura = Math.max(largura, tabela.prepareRenderer(tabela.getCellRenderer(linha, coluna), linha, coluna).getPreferredSize().width);
            atual.setPreferredWidth(largura);
            if (tabela.getColumnClass(coluna) != String.class)
                atual.setMinWidth(largura);
        }
    }

    /**
     * Escolhe quais linhas ficam em vermelho.
     */
    public void destacar(Predicate<T> criterio) {
        destaque = criterio;
    }

    /**
     * Mostra um icone (ex.: a capa) antes do texto da primeira coluna.
     */
    public void iconeNaPrimeiraColuna(Function<T, Icon> icone) {
        this.icone = icone;
    }

    @Override
    public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada, boolean foco, int linha, int coluna) {
        String texto = ModeloTabela.formatar(valor);
        super.getTableCellRendererComponent(tabela, texto.isEmpty() ? VAZIO : texto, selecionada, false, linha, coluna);
        setHorizontalAlignment(Number.class.isAssignableFrom(tabela.getColumnClass(coluna)) ? RIGHT : LEFT);
        setBorder(BorderFactory.createEmptyBorder(0, Tema.ESPACO_3, 0, Tema.ESPACO_3));
        setIcon(coluna == 0 ? icone.apply(modelo.linha(tabela.convertRowIndexToModel(linha))) : null);
        setIconTextGap(Tema.ESPACO_3);
        if (!selecionada) {
            boolean emDestaque = destaque.test(modelo.linha(tabela.convertRowIndexToModel(linha)));
            setForeground(emDestaque ? Tema.perigo() : texto.isEmpty() ? Tema.suave() : tabela.getForeground());
            setBackground(emDestaque ? Tema.cor("Locadora.perigoFundo") : tabela.getBackground());
        }
        return this;
    }
}
