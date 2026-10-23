package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Titulo;
import locadora.service.Formato;
import locadora.service.RegraException;
import locadora.service.TituloService;
import locadora.view.Tema;
import locadora.view.componente.Eventos;
import locadora.view.componente.Seletor;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;

/* Registro de aquisicao: titulo ja cadastrado ou um titulo novo, quantidade, custo unitario e fornecedor.
   Os exemplares novos recebem codigos sequenciais e atendem primeiro a fila de reservas. */
public class AquisicaoDialog extends Formulario {

    private final Main.Servicos servicos;
    private final JRadioButton existente = new JRadioButton("Título já cadastrado", true);
    private final JRadioButton novo = new JRadioButton("Título novo");
    private final CardLayout cartoes = new CardLayout();
    private final JPanel escolha = new JPanel(cartoes);
    private final Seletor<Titulo> titulo = new Seletor<>("Buscar título", Titulo::getNome, (Titulo t) -> t.tipo() + " · " + t.getAno());
    private final JComboBox<String> tipo = new JComboBox<>(TituloService.TIPOS.toArray(String[]::new));
    private final JTextField nome = new JTextField(10);
    private final JSpinner ano = new JSpinner(new SpinnerNumberModel(2024, 1800, 2200, 1));
    private final JSpinner prazo = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
    private final JSpinner quantidade = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
    private final JTextField custo = new JTextField(10);
    private final JTextField fornecedor = new JTextField(24);
    private final JLabel total = new JLabel("Total: —");
    private Titulo criado;

    public AquisicaoDialog(Component origem, Main.Servicos servicos) {
        super(origem, "Registrar aquisição", "Registrar");
        this.servicos = servicos;
        titulo.setItens(servicos.titulos().listar());
        ano.setEditor(new JSpinner.NumberEditor(ano, "#"));
        custo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "29,90");
        total.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
        Eventos.aoDigitar(custo, this::mostrarTotal);
        quantidade.addChangeListener(e -> mostrarTotal());

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(existente);
        grupo.add(novo);
        existente.addActionListener(e -> cartoes.show(escolha, "existente"));
        novo.addActionListener(e -> cartoes.show(escolha, "novo"));
        JPanel opcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, Tema.ESPACO_4, 0));
        opcoes.add(existente);
        opcoes.add(novo);
        escolha.add(titulo, "existente");
        escolha.add(formularioDeTituloNovo(), "novo");
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

    // guarda o titulo criado para uma nova tentativa nao cadastrar o mesmo titulo duas vezes
    private Titulo tituloNovo() {
        if (criado == null)
            criado = servicos.titulos().salvar((String) tipo.getSelectedItem(), 0, nome.getText(), "", (Integer) ano.getValue(), (Integer) prazo.getValue());
        return criado;
    }

    private JPanel formularioDeTituloNovo() {
        JPanel campos = new JPanel(new GridLayout(2, 4, Tema.ESPACO_3, Tema.ESPACO_1));
        for (String rotulo : new String[]{"Tipo", "Nome", "Ano", "Prazo (dias)"})
            campos.add(new JLabel(rotulo));
        campos.add(tipo);
        campos.add(nome);
        campos.add(ano);
        campos.add(prazo);
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
