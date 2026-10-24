package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.model.Cargo;
import locadora.model.Funcionario;
import locadora.service.FuncionarioService;
import locadora.service.RegraException;

import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Component;
import java.util.Arrays;

// cadastro de funcionario (com login e senha) e edicao de dados e cargo (login e senha ficam como estao)
public class FuncionarioDialog extends Formulario {

    private final FuncionarioService servico;
    private final Funcionario existente;
    private final JTextField nome = new JTextField(28);
    private final JTextField cpf = new JTextField(14);
    private final JTextField telefone = new JTextField(14);
    private final JTextField email = new JTextField(14);
    private final JComboBox<Cargo> cargo = new JComboBox<>(Cargo.values());
    private final JTextField login = new JTextField(14);
    private final JPasswordField senha = new JPasswordField(14);
    private final JPasswordField confirmacao = new JPasswordField(14);

    public FuncionarioDialog(Component origem, FuncionarioService servico, Funcionario existente) {
        super(origem, existente == null ? "Novo funcionário" : "Editar funcionário", existente == null ? "Cadastrar" : "Salvar");
        this.servico = servico;
        this.existente = existente;
        cpf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "000.000.000-00");
        cargo.setSelectedItem(Cargo.ATENDENTE);
        if (existente != null) {
            nome.setText(existente.getNome());
            cpf.setText(existente.cpfFormatado());
            telefone.setText(existente.getTelefone());
            email.setText(existente.getEmail());
            cargo.setSelectedItem(existente.getCargo());
        }
        campo("Nome completo", nome);
        campos("CPF", cpf, "Telefone", telefone);
        campos("E-mail", email, "Cargo", cargo);
        if (existente == null) {
            campo("Login", login);
            campos("Senha", senha, "Repita a senha", confirmacao);
            nota("Gerentes registram aquisições, perdas e funcionários. Atendentes cuidam de locações, devoluções e reservas.");
        }
    }

    @Override
    protected void salvar() {
        if (existente != null) {
            servico.atualizar(existente.getId(), nome.getText(), cpf.getText(), telefone.getText(), email.getText(), (Cargo) cargo.getSelectedItem());
            return;
        }
        RegraException.se(!Arrays.equals(senha.getPassword(), confirmacao.getPassword()), "As duas senhas não são iguais.");
        servico.cadastrar(nome.getText(), cpf.getText(), telefone.getText(), email.getText(), login.getText(), new String(senha.getPassword()),
                (Cargo) cargo.getSelectedItem());
    }
}
