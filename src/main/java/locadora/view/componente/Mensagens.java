package locadora.view.componente;

import locadora.service.RegraException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

/* - Dialogos:
     erro: aviso amigavel de que algo nao pode ser feito
     informar: aviso neutro que precisa de atencao (mais forte que um toast)
     confirmar: pergunta antes de uma acao irreversivel; true se a pessoa confirmou
     inesperado: registra o erro no log e mostra uma mensagem generica, nunca o stack trace
   - Execucao:
     executar: roda a acao e transforma qualquer erro em dialogo; true se deu certo */
public final class Mensagens {

    private static final Logger LOG = Logger.getLogger(Mensagens.class.getName());

    private Mensagens() {
    }

    public static void erro(Component origem, String mensagem) {
        JOptionPane.showMessageDialog(janela(origem), mensagem, "Não foi possível concluir", JOptionPane.WARNING_MESSAGE);
    }

    public static void informar(Component origem, String titulo, String mensagem) {
        JOptionPane.showMessageDialog(janela(origem), mensagem, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component origem, String titulo, String mensagem, String acao) {
        Object[] opcoes = {acao, "Cancelar"};
        int escolha = JOptionPane.showOptionDialog(janela(origem), mensagem, titulo, JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[1]);
        return escolha == 0;
    }

    public static void inesperado(Component origem, Throwable erro) {
        LOG.log(Level.SEVERE, "Erro inesperado na interface", erro);
        erro(origem, "Ocorreu um erro inesperado. Tente de novo e, se continuar, reinicie o sistema.");
    }

    public static boolean executar(Component origem, Runnable acao) {
        try {
            acao.run();
            return true;
        } catch (RegraException e) {
            erro(origem, e.getMessage());
        } catch (RuntimeException e) {
            inesperado(origem, e);
        }
        return false;
    }

    private static Component janela(Component origem) {
        return origem == null ? null : SwingUtilities.getWindowAncestor(origem) == null ? origem : SwingUtilities.getWindowAncestor(origem);
    }
}
