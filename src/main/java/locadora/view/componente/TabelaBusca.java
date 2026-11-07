package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.service.ArquivoService;
import locadora.service.Formato;
import locadora.view.Icone;
import locadora.view.Tema;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Tabela com busca aproximada (tolera acento e erro de digitacao), filtro extra, linhas em destaque, exportacao CSV e estado vazio.
 */
public class TabelaBusca<T> extends JPanel {

    private static final String TABELA = "tabela";
    private static final String VAZIA = "vazia";
    private static final String SEM_RESULTADO = "semResultado";

    private final ModeloTabela<T> modelo;
    private final JTable tabela;
    private final TableRowSorter<ModeloTabela<T>> ordenador;
    private final JTextField busca = new JTextField(24);
    private final Fileira filtros = new Fileira(Tema.ESPACO_2);
    private final Fileira acoes = new Fileira(Tema.ESPACO_2);
    private final CardLayout cartoes = new CardLayout();
    private final JPanel conteudo = new JPanel(cartoes);
    private Predicate<T> filtroExtra = item -> true;
    private final CelulaTabela<T> celula;

    public TabelaBusca(List<ModeloTabela.Coluna<T>> colunas, String dicaDaBusca, EstadoVazio vazio) {
        super(new BorderLayout(0, Tema.ESPACO_3));
        setOpaque(false);
        modelo = new ModeloTabela<>(colunas);
        tabela = new JTable(modelo);
        ordenador = new TableRowSorter<>(modelo);
        tabela.setRowSorter(ordenador);
        celula = CelulaTabela.aplicar(tabela, modelo);

        busca.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, dicaDaBusca);
        busca.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icone.LUPA.de(16, "Locadora.suave"));
        busca.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        Eventos.aoDigitar(busca, this::aplicarFiltros);
        filtros.adicionar(busca);
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(filtros, BorderLayout.WEST);
        barra.add(acoes, BorderLayout.EAST);

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.putClientProperty(FlatClientProperties.STYLE, "arc: 14");
        conteudo.setOpaque(false);
        conteudo.add(rolagem, TABELA);
        conteudo.add(vazio, VAZIA);
        conteudo.add(new EstadoVazio(Icone.LUPA, "Nada encontrado", "Tente outro termo ou limpe a busca.",
                Botoes.secundario("Limpar busca", Icone.FECHAR, () -> busca.setText(""))), SEM_RESULTADO);

        add(barra, BorderLayout.NORTH);
        add(conteudo, BorderLayout.CENTER);
    }

    /**
     * Troca as linhas e reaplica busca e filtro.
     */
    public void setDados(List<T> dados) {
        modelo.setLinhas(dados);
        aplicarFiltros();
        CelulaTabela.ajustarLarguras(tabela);
    }

    /**
     * Objeto da linha selecionada, ou nulo.
     */
    public T selecionado() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : modelo.linha(tabela.convertRowIndexToModel(linha));
    }

    /**
     * Seleciona e rola ate a primeira linha que atende ao criterio.
     */
    public void selecionar(Predicate<T> criterio) {
        IntStream.range(0, tabela.getRowCount())
                .filter(linha -> criterio.test(modelo.linha(tabela.convertRowIndexToModel(linha))))
                .findFirst()
                .ifPresent(linha -> {
                    tabela.setRowSelectionInterval(linha, linha);
                    tabela.scrollRectToVisible(tabela.getCellRect(linha, 0, true));
                });
    }

    /**
     * Acao do duplo clique ou do Enter numa linha.
     */
    public void aoAbrir(Consumer<T> acao) {
        Runnable abrir = () -> {
            if (selecionado() != null)
                acao.accept(selecionado());
        };
        Eventos.duploClique(tabela, abrir);
        Eventos.teclaNoComponente(tabela, "ENTER", abrir);
    }

    /**
     * Acao a cada troca de selecao (para ligar e desligar botoes).
     */
    public void aoSelecionar(Consumer<T> acao) {
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                acao.accept(selecionado());
        });
    }

    /**
     * Filtro extra combinado com a busca (ex.: tipo de midia).
     */
    public void filtrar(Predicate<T> criterio) {
        filtroExtra = criterio;
        aplicarFiltros();
    }

    /**
     * Linhas mostradas em vermelho (ex.: atrasadas).
     */
    public void destacar(Predicate<T> criterio) {
        celula.destacar(criterio);
        tabela.repaint();
    }

    /**
     * Mostra uma imagem antes do texto da primeira coluna, com linhas mais altas.
     */
    public void comMiniatura(Function<T, Icon> miniatura, int alturaDaLinha) {
        celula.iconeNaPrimeiraColuna(miniatura);
        tabela.setRowHeight(alturaDaLinha);
    }

    /**
     * Poe um componente ao lado da busca.
     */
    public void adicionarNaBarra(JComponent componente) {
        filtros.adicionar(componente);
    }

    /**
     * Mostra o botao de exportar CSV com as linhas visiveis.
     */
    public void permitirExportar(ArquivoService arquivos, String nomeDoArquivo) {
        acoes.adicionar(Botoes.discreto("Exportar CSV", Icone.EXPORTAR, () -> exportar(arquivos, nomeDoArquivo)));
    }

    private void aplicarFiltros() {
        String termo = busca.getText();
        ordenador.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ModeloTabela<T>, ? extends Integer> entrada) {
                int linha = entrada.getIdentifier();
                return filtroExtra.test(modelo.linha(linha))
                        && Formato.parecido(termo, modelo.textoDaLinha(linha));
            }
        });
        cartoes.show(conteudo, modelo.getRowCount() == 0 ? VAZIA : tabela.getRowCount() == 0 ? SEM_RESULTADO : TABELA);
    }

    private void exportar(ArquivoService arquivos, String nomeDoArquivo) {
        JFileChooser escolha = new JFileChooser();
        escolha.setSelectedFile(new File(nomeDoArquivo + ".csv"));
        if (escolha.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        List<List<String>> linhas = IntStream.range(0, tabela.getRowCount())
                .map(tabela::convertRowIndexToModel)
                .mapToObj(linha -> IntStream.range(0, modelo.getColumnCount()).mapToObj(coluna -> modelo.textoDaCelula(linha, coluna)).toList())
                .toList();
        if (Mensagens.executar(this, () -> arquivos.exportarCsv(escolha.getSelectedFile().toPath(), modelo.titulos(), linhas)))
            Toast.mostrar(this, "Planilha exportada com " + linhas.size() + " linhas");
    }

}
