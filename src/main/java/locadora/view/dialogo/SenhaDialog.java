package locadora.view.dialogo;

import locadora.model.Funcionario;
import locadora.service.FuncionarioService;
import locadora.service.RegraException;

import javax.swing.JPasswordField;
import java.awt.Component;
import java.util.Arrays;

// troca da propria senha (pede a atual) ou redefinicao da senha de outro funcionario pelo gerente
public class SenhaDialog extends Formulario {

    private final FuncionarioService servico;
    private final Funcionario alvo;
    private final JPasswordField atual = new JPasswordField(14);
    private final JPasswordField nova = new JPasswordField(14);
    private final JPasswordField confirmacao = new JPasswordField(14);

    public SenhaDialog(Component origem, FuncionarioService servico, Funcionario alvo) {
        super(origem, alvo == null ? "Trocar minha senha" : "Redefinir senha de " + alvo.getNome(), "Salvar senha");
        this.servico = servico;
        this.alvo = alvo;
        if (alvo == null)
            campo("Senha atual", atual);
        campos("Nova senha", nova, "Repita a nova senha", confirmacao);
        nota("A senha precisa ter pelo menos 6 caracteres e é guardada apenas como hash.");
    }

    @Override
    protected void salvar() {
        RegraException.se(!Arrays.equals(nova.getPassword(), confirmacao.getPassword()), "As duas senhas novas não são iguais.");
        String senha = new String(nova.getPassword());
        if (alvo == null)
            servico.trocarSenha(new String(atual.getPassword()), senha);
        else
            servico.redefinirSenha(alvo.getId(), senha);
    }
}
