package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Cliente;
import locadora.model.Exemplar;
import locadora.model.Situacao;
import locadora.service.Formato;
import locadora.service.RegraException;
import locadora.view.componente.Seletor;

import javax.swing.JLabel;
import java.awt.Component;
import java.util.List;

/* Nova locacao: escolhe o cliente, depois um exemplar disponivel (ou separado para ele) e confirma.
   O resumo mostra a data de devolucao e o valor antes de confirmar. */
public class NovaLocacaoDialog extends Formulario {

    private final Main.Servicos servicos;
    private final Cliente fixo;
    private final Seletor<Cliente> cliente = new Seletor<>("Buscar cliente por nome ou CPF", Cliente::getNome, Cliente::cpfFormatado);
    private final Seletor<Exemplar> exemplar = new Seletor<>("Buscar título ou código do exemplar",
            (Exemplar e) -> e.getTitulo().getNome(), NovaLocacaoDialog::detalhe);
    private final JLabel resumo = new JLabel("Escolha o cliente e o exemplar.");

    public NovaLocacaoDialog(Component origem, Main.Servicos servicos, Cliente fixo) {
        super(origem, "Nova locação", "Confirmar locação");
        this.servicos = servicos;
        this.fixo = fixo;
        resumo.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
        exemplar.aoSelecionar(e -> mostrarResumo());
        if (fixo == null) {
            cliente.setItens(servicos.clientes().listar().stream().filter(Cliente::isAtivo).toList());
            cliente.aoSelecionar(c -> carregarExemplares());
            carregarExemplares();
            campo("Cliente", cliente);
        } else {
            JLabel escolhido = new JLabel(fixo.getNome() + " · CPF " + fixo.cpfFormatado());
            escolhido.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
            campo("Cliente", escolhido);
            carregarExemplares();
        }
        campo("Exemplar", exemplar);
        campo("Resumo", resumo);
        nota("O funcionário logado fica registrado como responsável pela locação.");
    }

    @Override
    protected void salvar() {
        RegraException.se(clienteEscolhido() == null, "Escolha o cliente.");
        RegraException.se(exemplar.selecionado() == null, "Escolha o exemplar.");
        servicos.locacoes().locar(clienteEscolhido().getId(), exemplar.selecionado().getId());
    }

    private Cliente clienteEscolhido() {
        return fixo != null ? fixo : cliente.selecionado();
    }

    private void carregarExemplares() {
        Cliente escolhido = clienteEscolhido();
        exemplar.setAvisoSemItens(escolhido == null ? "Escolha o cliente para ver os exemplares que ele pode levar."
                : "Nenhum exemplar disponível para " + escolhido.getNome() + ".");
        exemplar.setItens(escolhido == null ? List.of() : servicos.locacoes().alugaveisPara(escolhido.getId()));
        mostrarResumo();
    }

    private void mostrarResumo() {
        Exemplar escolhido = exemplar.selecionado();
        resumo.setText(escolhido == null ? "Escolha o cliente e o exemplar."
                : "Devolver até " + Formato.data(servicos.locacoes().hoje().plusDays(escolhido.getTitulo().getPrazoMaximoDias()))
                + " · " + Formato.dinheiro(escolhido.getTitulo().valorLocacao()));
    }

    private static String detalhe(Exemplar exemplar) {
        String situacao = exemplar.getSituacao() == Situacao.RESERVADO ? "separado para este cliente" : "disponível";
        return exemplar.getCodigo() + " · " + exemplar.getTitulo().tipo() + " · " + situacao;
    }
}
