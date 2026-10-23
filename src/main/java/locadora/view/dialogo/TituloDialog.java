package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.model.Titulo;
import locadora.service.Formato;
import locadora.service.TituloService;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Component;

// cadastro e edicao de titulo; os valores de multa e locacao mudam na tela conforme o tipo escolhido
public class TituloDialog extends Formulario {

    private final TituloService servico;
    private final Titulo existente;
    private final JComboBox<String> tipo = new JComboBox<>(TituloService.TIPOS.toArray(String[]::new));
    private final JTextField nome = new JTextField(28);
    private final JTextField descricao = new JTextField(28);
    private final JSpinner ano = new JSpinner(new SpinnerNumberModel(2000, 1800, 2200, 1));
    private final JSpinner prazo = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
    private final JLabel valores = new JLabel();

    public TituloDialog(Component origem, TituloService servico, Titulo existente) {
        super(origem, existente == null ? "Novo título" : "Editar título", existente == null ? "Cadastrar" : "Salvar");
        this.servico = servico;
        this.existente = existente;
        ano.setEditor(new JSpinner.NumberEditor(ano, "#"));
        valores.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        tipo.addActionListener(e -> mostrarValores());
        if (existente != null) {
            tipo.setSelectedItem(existente.tipo());
            nome.setText(existente.getNome());
            descricao.setText(existente.getDescricao());
            ano.setValue(existente.getAno());
            prazo.setValue(existente.getPrazoMaximoDias());
        }
        campos("Tipo de mídia", tipo, "Prazo de empréstimo (dias)", prazo);
        campo("Nome", nome);
        campo("Descrição", descricao);
        campos("Ano", ano, "Valores do tipo", valores);
        mostrarValores();
    }

    @Override
    protected void salvar() {
        servico.salvar((String) tipo.getSelectedItem(), existente == null ? 0 : existente.getId(), nome.getText(), descricao.getText(),
                (Integer) ano.getValue(), (Integer) prazo.getValue());
    }

    private void mostrarValores() {
        Titulo amostra = servico.amostra((String) tipo.getSelectedItem());
        valores.setText("Locação " + Formato.dinheiro(amostra.valorLocacao()) + " · multa " + Formato.dinheiro(amostra.valorMultaDiaria()) + "/dia");
    }
}
