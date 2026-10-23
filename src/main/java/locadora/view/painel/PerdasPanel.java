package locadora.view.painel;

import locadora.Main;
import locadora.model.Perda;
import locadora.view.Icone;
import locadora.view.componente.Botoes;
import locadora.view.componente.Cabecalho;
import locadora.view.componente.EstadoVazio;
import locadora.view.componente.TabelaBusca;
import locadora.view.componente.Toast;
import locadora.view.dialogo.PerdaDialog;

import java.awt.BorderLayout;
import java.util.List;

import static locadora.view.componente.ModeloTabela.data;
import static locadora.view.componente.ModeloTabela.dinheiro;
import static locadora.view.componente.ModeloTabela.texto;

/* Tela de perdas (so gerente): historico de exemplares perdidos e registro de uma perda nova.
   atualizar: recarrega o historico */
public class PerdasPanel extends Painel {

    private final Main.Servicos servicos;
    private final TabelaBusca<Perda> tabela;

    public PerdasPanel(Main.Servicos servicos) {
        this.servicos = servicos;
        tabela = new TabelaBusca<>(List.of(
                data("Data", Perda::getData),
                texto("Exemplar", (Perda p) -> p.getExemplar().getCodigo()),
                texto("Título", (Perda p) -> p.getExemplar().getTitulo().getNome()),
                texto("Motivo", Perda::getMotivo),
                texto("Cliente cobrado", (Perda p) -> p.getLocacao() == null ? "" : p.getLocacao().getCliente().getNome()),
                dinheiro("Valor cobrado", (Perda p) -> p.getLocacao() == null ? null : p.getLocacao().getMulta()),
                texto("Registrada por", (Perda p) -> p.getFuncionario().getNome())),
                "Buscar por exemplar, título ou motivo",
                new EstadoVazio(Icone.ALERTA, "Nenhuma perda registrada", "Ótimo sinal: o acervo está inteiro.", null));
        tabela.permitirExportar(servicos.arquivos(), "perdas");

        add(new Cabecalho("Perdas", "Exemplares perdidos ou danificados, que saem do acervo para sempre",
                Botoes.primario("Registrar perda", Icone.ALERTA, this::registrar)), BorderLayout.NORTH);
        add(tabela, BorderLayout.CENTER);
    }

    @Override
    public void atualizar() {
        tabela.setDados(servicos.perdas().listar());
    }

    private void registrar() {
        if (new PerdaDialog(this, servicos).abrir()) {
            atualizar();
            Toast.mostrar(this, "Perda registrada");
        }
    }
}
