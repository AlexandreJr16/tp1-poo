package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Titulo;
import locadora.service.AquisicaoService;
import locadora.service.Formato;
import locadora.service.RegraException;
import locadora.service.TituloService;
import locadora.view.Tema;
import locadora.view.componente.Eventos;
import locadora.view.componente.Fileira;
import locadora.view.componente.Seletor;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.Year;

/**
 * Registro de aquisicao: titulo ja cadastrado ou um titulo novo, quantidade, custo unitario e fornecedor. Os exemplares novos recebem codigos sequenciais e atendem primeiro a fila de reservas.
 */
public class AquisicaoDialog extends Formulario {

    private final Main.Servicos servicos;
    private final JRadioButton existente = new JRadioButton("Título já cadastrado", true);
    private final JRadioButton novo = new JRadioButton("Título novo");
    private final JPanel escolha = new JPanel(new BorderLayout());
    private final Seletor<Titulo> titulo = new Seletor<>("Buscar título", Titulo::getNome, (Titulo t) -> t.tipo() + " · " + t.getAno());
    private final JComboBox<String> tipo = new JComboBox<>(TituloService.TIPOS.toArray(String[]::new));
    private final JTextField nome = new JTextField(10);
    private final JSpinner ano = new JSpinner(new SpinnerNumberModel(2024, TituloService.ANO_MINIMO, Year.now().getValue() + 1, 1));
    private final JSpinner prazo = new JSpinner(new SpinnerNumberModel(5, 1, TituloService.PRAZO_MAXIMO_DIAS, 1));
    private final JSpinner quantidade = new JSpinner(new SpinnerNumberModel(2, 1, AquisicaoService.QUANTIDADE_MAXIMA, 1));
    private final JTextField custo = new JTextField(10);
    private final JTextField fornecedor = new JTextField(24);
    private final JLabel total = new JLabel("Total: —");
    private Titulo criado;

    public AquisicaoDialog(Component origem, Main.Servicos servicos) {
        super(origem, "Registrar aquisição", "Registrar");
        this.servicos = servicos;
        titulo.setItens(servicos.titulos().listar());
        ano.setEditor(new JSpinner.NumberEditor(ano, "#"));
        custo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "0,00");
        total.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
        Eventos.aoDigitar(custo, this::mostrarTotal);
        quantidade.addChangeListener(e -> mostrarTotal());

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(existente);
        grupo.add(novo);
        JPanel tituloNovo = formularioDeTituloNovo();
        existente.addActionListener(e -> mostrar(titulo));
        novo.addActionListener(e -> mostrar(tituloNovo));
        escolha.add(titulo);
        JPanel opcoes = new JPanel(new BorderLayout());
        opcoes.add(new Fileira(Tema.ESPACO_5, existente, novo), BorderLayout.WEST);
        JPanel blocoDoTitulo = new JPanel(new BorderLayout(0, Tema.ESPACO_2));
        blocoDoTitulo.add(opcoes, BorderLayout.NORTH);
        blocoDoTitulo.add(escolha, BorderLayout.CENTER);

        campo("Título", blocoDoTitulo);
        campos("Quantidade de exemplares", quantidade, "Custo unitário (R$)", custo);
        campo("Fornecedor", fornecedor);
        campo("Resumo", total);
    }

    @Override
    protected void salvar() {
        BigDecimal valor = Formato.lerDinheiro(custo.getText());
        RegraException.se(fornecedor.getText().isBlank(), "Informe o fornecedor.");
        Titulo escolhido = novo.isSelected() ? tituloNovo() : titulo.selecionado();
        RegraException.se(escolhido == null, "Escolha o título da aquisição.");
        servicos.aquisicoes().registrar(escolhido.getId(), (Integer) quantidade.getValue(), valor, fornecedor.getText());
    }

    /**
     * Guarda o titulo criado para uma nova tentativa nao cadastrar o mesmo titulo duas vezes.
     */
    private Titulo tituloNovo() {
        if (criado == null)
            criado = servicos.titulos().salvar((String) tipo.getSelectedItem(), 0, nome.getText(), "", (Integer) ano.getValue(), (Integer) prazo.getValue());
        return criado;
    }

    private void mostrar(JComponent bloco) {
        escolha.removeAll();
        escolha.add(bloco);
        escolha.revalidate();
        escolha.repaint();
        reajustar();
    }

    /**
     * Nome na largura toda; tipo, ano e prazo lado a lado.
     */
    private JPanel formularioDeTituloNovo() {
        JPanel numeros = new JPanel(new GridLayout(1, 3, Tema.ESPACO_3, 0));
        numeros.add(bloco("Tipo de mídia", tipo));
        numeros.add(bloco("Ano", ano));
        numeros.add(bloco("Prazo (dias)", prazo));
        JPanel campos = new JPanel(new BorderLayout(0, Tema.ESPACO_3));
        campos.add(bloco("Nome do título", nome), BorderLayout.NORTH);
        campos.add(numeros, BorderLayout.CENTER);
        return campos;
    }

    private void mostrarTotal() {
        try {
            BigDecimal valor = Formato.lerDinheiro(custo.getText());
            total.setText("Total: " + Formato.dinheiro(valor.multiply(BigDecimal.valueOf((Integer) quantidade.getValue()))));
        } catch (RegraException e) {
            total.setText("Total: —");
        }
    }
}
