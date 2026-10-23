package locadora.view.componente;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/* Atalhos para ligar acoes a eventos de teclado, mouse e digitacao.
   tecla: atalho que vale em qualquer lugar da janela (ex.: "ctrl K", "ESCAPE")
   teclaNoComponente: atalho que so vale com o componente focado e passa na frente do padrao dele
   duploClique: acao do duplo clique
   aoDigitar: acao a cada mudanca no texto do campo */
public final class Eventos {

    private Eventos() {
    }

    public static void tecla(JComponent alvo, String tecla, Runnable acao) {
        ligar(alvo, JComponent.WHEN_IN_FOCUSED_WINDOW, tecla, acao);
    }

    public static void teclaNoComponente(JComponent alvo, String tecla, Runnable acao) {
        ligar(alvo, JComponent.WHEN_FOCUSED, tecla, acao);
    }

    public static void duploClique(JComponent alvo, Runnable acao) {
        alvo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    acao.run();
            }
        });
    }

    public static void aoDigitar(JTextComponent campo, Runnable acao) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                acao.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                acao.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                acao.run();
            }
        });
    }

    private static void ligar(JComponent alvo, int condicao, String tecla, Runnable acao) {
        String nome = "locadora." + tecla;
        alvo.getInputMap(condicao).put(KeyStroke.getKeyStroke(tecla), nome);
        alvo.getActionMap().put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acao.run();
            }
        });
    }
}
