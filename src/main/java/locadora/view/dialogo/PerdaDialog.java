package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Exemplar;
import locadora.model.Situacao;
import locadora.service.Formato;
import locadora.service.RegraException;
import locadora.view.componente.Descricoes;
import locadora.view.componente.Mensagens;
import locadora.view.componente.Seletor;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.math.BigDecimal;

/**
 * Registro de perda: escolhe o exemplar, informa o motivo e ve antes o que acontece (se estava alugado, o cliente e cobrado do custo mais a multa ate hoje). Pede confirmacao.
 */
public class PerdaDialog extends Formulario {

    private final Main.Servicos servicos;
    private final Seletor<Exemplar> exemplar = new Seletor<>("Buscar título ou código do exemplar", Descricoes::exemplar,
            (Exemplar e) -> e.getTitulo().tipo() + " · " + e.getSituacao());
    private final JTextArea motivo = new JTextArea(3, 30);
    private final JLabel consequencia = new JLabel("Escolha o exemplar.");

    public PerdaDialog(Component origem, Main.Servicos servicos) {
        super(origem, "Registrar perda", "Registrar perda");
        this.servicos = servicos;
        exemplar.setItens(servicos.titulos().exemplaresNoAcervo());
        exemplar.aoSelecionar(e -> mostrarConsequencia());
        mostrarConsequencia();
        motivo.setLineWrap(true);
        motivo.setWrapStyleWord(true);
        campo("Exemplar", exemplar);
        campo("Motivo", new JScrollPane(motivo));
        campo("O que acontece", consequencia);
    }

    @Override
    protected boolean podeSalvar() {
        Exemplar escolhido = exemplar.selecionado();
        return escolhido == null || motivo.getText().isBlank() || Mensagens.confirmar(this, "Registrar perda",
                "Dar " + escolhido.getCodigo() + " como perdido? Um exemplar perdido nunca volta a ser alugado.", "Registrar perda");
    }

    @Override
    protected void salvar() {
        RegraException.se(exemplar.selecionado() == null, "Escolha o exemplar.");
        servicos.perdas().registrar(exemplar.selecionado().getId(), motivo.getText());
    }

    private void mostrarConsequencia() {
        Exemplar escolhido = exemplar.selecionado();
        boolean grave = escolhido != null && escolhido.getSituacao() != Situacao.DISPONIVEL;
        consequencia.putClientProperty(FlatClientProperties.STYLE, grave ? "foreground: $Locadora.perigo" : "foreground: $Locadora.suave");
        if (escolhido == null) {
            consequencia.setText("Escolha o exemplar.");
        } else if (escolhido.getSituacao() == Situacao.ALUGADO) {
            BigDecimal cobranca = servicos.perdas().previaCobranca(escolhido.getId());
            consequencia.setText(paragrafo("Está alugado: a locação será encerrada e o cliente cobrado em <b>" + Formato.dinheiro(cobranca)
                    + "</b> (custo do exemplar mais a multa até hoje)."));
        } else if (escolhido.getSituacao() == Situacao.RESERVADO) {
            consequencia.setText(paragrafo("Está separado para uma reserva: ela volta para a fila, na mesma posição."));
        } else {
            consequencia.setText("O exemplar sai do acervo para sempre.");
        }
        reajustar();
    }
}
