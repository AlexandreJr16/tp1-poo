package locadora.view.painel;

import locadora.Main;
import locadora.model.Reserva;
import locadora.model.SituacaoReserva;
import locadora.view.Icone;
import locadora.view.componente.Botoes;
import locadora.view.componente.Cabecalho;
import locadora.view.componente.EstadoVazio;
import locadora.view.componente.Mensagens;
import locadora.view.componente.TabelaBusca;
import locadora.view.componente.Toast;
import locadora.view.dialogo.NovaLocacaoDialog;
import locadora.view.dialogo.ReservaDialog;

import javax.swing.JButton;
import java.awt.BorderLayout;
import java.util.List;

import static locadora.view.componente.ModeloTabela.data;
import static locadora.view.componente.ModeloTabela.texto;

/**
 * Tela de reservas: fila de espera por titulos sem exemplar disponivel. Uma reserva pronta tem um exemplar separado; a retirada abre a locacao para o cliente dela.
 */
public class ReservasPanel extends Painel {

    private final Main.Servicos servicos;
    private final TabelaBusca<Reserva> tabela;
    private final JButton cancelar = Botoes.secundario("Cancelar reserva", Icone.FECHAR, this::cancelar);
    private final JButton retirar = Botoes.secundario("Registrar retirada", Icone.CONFIRMAR, this::retirar);

    public ReservasPanel(Main.Servicos servicos) {
        this.servicos = servicos;
        tabela = new TabelaBusca<>(List.of(
                texto("Cliente", (Reserva r) -> r.getCliente().getNome()),
                texto("Título", (Reserva r) -> r.getTitulo().getNome()),
                data("Reservada em", Reserva::getData),
                texto("Situação", Reserva::getSituacao),
                texto("Posição na fila", (Reserva r) -> r.getSituacao() == SituacaoReserva.PRONTA ? "—" : servicos.reservas().posicao(r) + "º"),
                texto("Exemplar separado", (Reserva r) -> r.getExemplar() == null ? "" : r.getExemplar().getCodigo()),
                texto("Registrada por", (Reserva r) -> r.getFuncionario().getNome())),
                "Buscar por cliente ou título",
                new EstadoVazio(Icone.MARCADOR, "Nenhuma reserva ativa", "Reserve um título quando todos os exemplares estiverem fora.",
                        Botoes.primario("Nova reserva", Icone.MAIS, this::nova)));
        tabela.permitirExportar(servicos.arquivos(), "reservas");
        tabela.aoSelecionar(r -> {
            cancelar.setEnabled(r != null);
            retirar.setEnabled(r != null && r.getSituacao() == SituacaoReserva.PRONTA);
        });
        cancelar.setEnabled(false);
        retirar.setEnabled(false);

        add(new Cabecalho("Reservas", "Fila de espera por títulos sem exemplar disponível", cancelar, retirar,
                Botoes.primario("Nova reserva", Icone.MAIS, this::nova)), BorderLayout.NORTH);
        add(tabela, BorderLayout.CENTER);
    }

    /**
     * Recarrega as reservas ativas.
     */
    @Override
    public void atualizar() {
        tabela.setDados(servicos.reservas().ativas());
    }

    private void nova() {
        if (new ReservaDialog(this, servicos, null).abrir()) {
            atualizar();
            Toast.mostrar(this, "Reserva registrada");
        }
    }

    private void retirar() {
        if (new NovaLocacaoDialog(this, servicos, tabela.selecionado().getCliente()).abrir()) {
            atualizar();
            Toast.mostrar(this, "Retirada registrada");
        }
    }

    private void cancelar() {
        Reserva reserva = tabela.selecionado();
        String pergunta = "Cancelar a reserva de " + reserva.getCliente().getNome() + " para " + reserva.getTitulo().getNome() + "?"
                + (reserva.getExemplar() == null ? "" : "\nO exemplar separado vai para o próximo da fila ou volta a ficar disponível.");
        if (Mensagens.confirmar(this, "Cancelar reserva", pergunta, "Cancelar reserva")
                && Mensagens.executar(this, () -> servicos.reservas().cancelar(reserva.getId()))) {
            atualizar();
            Toast.mostrar(this, "Reserva cancelada");
        }
    }
}
