package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.model.Cliente;
import locadora.service.ClienteService;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import java.awt.Component;

/**
 * Cadastro e edicao de cliente; na edicao aparece a opcao de desativar (so gerente consegue salvar isso).
 */
public class ClienteDialog extends Formulario {

    private final ClienteService servico;
    private final Cliente existente;
    private final JTextField nome = new JTextField(30);
    private final JTextField cpf = new JTextField(14);
    private final JTextField telefone = new JTextField(14);
    private final JTextField email = new JTextField(30);
    private final JTextField endereco = new JTextField(30);
    private final JCheckBox ativo = new JCheckBox("Cliente ativo (pode alugar e reservar)", true);

    public ClienteDialog(Component origem, ClienteService servico, Cliente existente) {
        super(origem, existente == null ? "Novo cliente" : "Editar cliente", existente == null ? "Cadastrar" : "Salvar");
        this.servico = servico;
        this.existente = existente;
        cpf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "000.000.000-00");
        telefone.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "(92) 99999-0000");
        email.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "opcional");
        if (existente != null) {
            nome.setText(existente.getNome());
            cpf.setText(existente.cpfFormatado());
            telefone.setText(existente.getTelefone());
            email.setText(existente.getEmail());
            endereco.setText(existente.getEndereco());
            ativo.setSelected(existente.isAtivo());
        }
        campo("Nome completo", nome);
        campos("CPF", cpf, "Telefone", telefone);
        campo("E-mail", email);
        campo("Endereço", endereco);
        if (existente != null)
            campo("Situação", ativo);
    }

    @Override
    protected void salvar() {
        servico.salvar(new Cliente(existente == null ? 0 : existente.getId(), nome.getText(), cpf.getText(), telefone.getText(),
                email.getText(), endereco.getText(), ativo.isSelected()));
    }
}
