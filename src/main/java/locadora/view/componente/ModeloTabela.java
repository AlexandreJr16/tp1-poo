package locadora.view.componente;

import locadora.service.Formato;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Modelo de tabela montado a partir de uma lista de objetos e da descricao das colunas.
 */
public class ModeloTabela<T> extends AbstractTableModel {

    public record Coluna<T>(String titulo, Class<?> tipo, Function<T, ?> valor) {
    }

    private final List<Coluna<T>> colunas;
    private List<T> linhas = List.of();

    public ModeloTabela(List<Coluna<T>> colunas) {
        this.colunas = colunas;
    }

    /**
     * Cria a coluna com o tipo certo (ordenacao e alinhamento corretos).
     */
    public static <T> Coluna<T> texto(String titulo, Function<T, ?> valor) {
        return new Coluna<>(titulo, String.class, valor);
    }

    /**
     * Cria a coluna com o tipo certo (ordenacao e alinhamento corretos).
     */
    public static <T> Coluna<T> numero(String titulo, Function<T, Integer> valor) {
        return new Coluna<>(titulo, Integer.class, valor);
    }

    /**
     * Cria a coluna com o tipo certo (ordenacao e alinhamento corretos).
     */
    public static <T> Coluna<T> data(String titulo, Function<T, LocalDate> valor) {
        return new Coluna<>(titulo, LocalDate.class, valor);
    }

    /**
     * Cria a coluna com o tipo certo (ordenacao e alinhamento corretos).
     */
    public static <T> Coluna<T> dinheiro(String titulo, Function<T, BigDecimal> valor) {
        return new Coluna<>(titulo, BigDecimal.class, valor);
    }

    /**
     * Troca as linhas mostradas.
     */
    public void setLinhas(List<T> linhas) {
        this.linhas = List.copyOf(linhas);
        fireTableDataChanged();
    }

    /**
     * Objeto de uma linha do modelo.
     */
    public T linha(int indice) {
        return linhas.get(indice);
    }

    public List<String> titulos() {
        return colunas.stream().map(Coluna::titulo).toList();
    }

    /**
     * Valor ja formatado para exibir, buscar e exportar.
     */
    public String textoDaCelula(int linha, int coluna) {
        return formatar(getValueAt(linha, coluna));
    }

    public String textoDaLinha(int linha) {
        return IntStream.range(0, getColumnCount()).mapToObj(c -> textoDaCelula(linha, c)).collect(Collectors.joining(" "));
    }

    public static String formatar(Object valor) {
        if (valor == null)
            return "";
        if (valor instanceof LocalDate data)
            return Formato.data(data);
        if (valor instanceof BigDecimal dinheiro)
            return Formato.dinheiro(dinheiro);
        return valor.toString();
    }

    @Override
    public int getRowCount() {
        return linhas.size();
    }

    @Override
    public int getColumnCount() {
        return colunas.size();
    }

    @Override
    public String getColumnName(int coluna) {
        return colunas.get(coluna).titulo();
    }

    @Override
    public Class<?> getColumnClass(int coluna) {
        return colunas.get(coluna).tipo();
    }

    @Override
    public Object getValueAt(int linha, int coluna) {
        return colunas.get(coluna).valor().apply(linhas.get(linha));
    }
}
