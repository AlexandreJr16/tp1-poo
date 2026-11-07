package locadora.view.painel;

import locadora.Main;
import locadora.model.Funcionario;
import locadora.view.Icone;
import locadora.view.componente.Botoes;
import locadora.view.componente.Cabecalho;
import locadora.view.componente.EstadoVazio;
import locadora.view.componente.TabelaBusca;
import locadora.view.componente.Toast;
import locadora.view.dialogo.FuncionarioDialog;
import locadora.view.dialogo.SenhaDialog;

import javax.swing.JButton;
import java.awt.BorderLayout;
import java.util.List;

import static locadora.view.componente.ModeloTabela.texto;

/**
 * Tela de funcionarios (so gerente): equipe, cargos e redefinicao de senha.
 */
public class FuncionariosPanel extends Painel {

    private final Main.Servicos servicos;
    private final TabelaBusca<Funcionario> tabela;
    private final JButton editar = Botoes.secundario("Editar", Icone.LAPIS, this::editar);
    private final JButton senha = Botoes.secundario("Redefinir senha", Icone.CHAVE, this::redefinirSenha);

    public FuncionariosPanel(Main.Servicos servicos) {
        this.servicos = servicos;
        tabela = new TabelaBusca<>(List.of(
                texto("Nome", Funcionario::getNome),
                texto("Login", Funcionario::getLogin),
                texto("Cargo", Funcionario::getCargo),
                texto("CPF", Funcionario::cpfFormatado),
                texto("Telefone", Funcionario::getTelefone),
                texto("E-mail", Funcionario::getEmail)),
                "Buscar por nome, login ou cargo",
                new EstadoVazio(Icone.CRACHA, "Nenhum funcionário", "Cadastre a equipe da locadora.", null));
        tabela.aoAbrir(f -> editar());
        tabela.aoSelecionar(f -> {
            editar.setEnabled(f != null);
            senha.setEnabled(f != null);
        });
        editar.setEnabled(false);
        senha.setEnabled(false);

        add(new Cabecalho("Funcionários", "Equipe, cargos e acesso ao sistema", senha, editar,
                Botoes.primario("Novo funcionário", Icone.MAIS, this::novo)), BorderLayout.NORTH);
        add(tabela, BorderLayout.CENTER);
    }

    /**
     * Recarrega os funcionarios.
     */
    @Override
    public void atualizar() {
        tabela.setDados(servicos.funcionarios().listar());
    }

    private void novo() {
        if (new FuncionarioDialog(this, servicos.funcionarios(), null).abrir()) {
            atualizar();
            Toast.mostrar(this, "Funcionário cadastrado");
        }
    }

    private void editar() {
        if (new FuncionarioDialog(this, servicos.funcionarios(), tabela.selecionado()).abrir()) {
            atualizar();
            Toast.mostrar(this, "Funcionário atualizado");
        }
    }

    private void redefinirSenha() {
        if (new SenhaDialog(this, servicos.funcionarios(), tabela.selecionado()).abrir())
            Toast.mostrar(this, "Senha redefinida");
    }
}
