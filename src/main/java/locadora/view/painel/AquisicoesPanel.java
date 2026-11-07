package locadora.view.painel;

import locadora.Main;
import locadora.model.Aquisicao;
import locadora.view.Icone;
import locadora.view.componente.Botoes;
import locadora.view.componente.Cabecalho;
import locadora.view.componente.EstadoVazio;
import locadora.view.componente.TabelaBusca;
import locadora.view.componente.Toast;
import locadora.view.dialogo.AquisicaoDialog;

import java.awt.BorderLayout;
import java.util.List;

import static locadora.view.componente.ModeloTabela.data;
import static locadora.view.componente.ModeloTabela.dinheiro;
import static locadora.view.componente.ModeloTabela.numero;
import static locadora.view.componente.ModeloTabela.texto;

/**
 * Tela de aquisicoes (so gerente): historico de compras e registro de uma compra nova.
 */
public class AquisicoesPanel extends Painel {

    private final Main.Servicos servicos;
    private final TabelaBusca<Aquisicao> tabela;

    public AquisicoesPanel(Main.Servicos servicos) {
        this.servicos = servicos;
        tabela = new TabelaBusca<>(List.of(
                data("Data", Aquisicao::getData),
                texto("Título", (Aquisicao a) -> a.getTitulo().getNome()),
                texto("Tipo", (Aquisicao a) -> a.getTitulo().tipo()),
                numero("Quantidade", Aquisicao::getQuantidade),
                dinheiro("Custo unitário", Aquisicao::getCustoUnitario),
                dinheiro("Total", Aquisicao::custoTotal),
                texto("Fornecedor", Aquisicao::getFornecedor),
                texto("Registrada por", (Aquisicao a) -> a.getFuncionario().getNome())),
                "Buscar por título ou fornecedor",
                new EstadoVazio(Icone.CAIXA, "Nenhuma aquisição registrada", "Cada compra gera os exemplares novos do título.",
                        Botoes.primario("Registrar aquisição", Icone.MAIS, this::registrar)));
        tabela.permitirExportar(servicos.arquivos(), "aquisicoes");

        add(new Cabecalho("Aquisições", "Compras de títulos e entrada de exemplares no acervo",
                Botoes.primario("Registrar aquisição", Icone.MAIS, this::registrar)), BorderLayout.NORTH);
        add(tabela, BorderLayout.CENTER);
    }

    /**
     * Recarrega o historico.
     */
    @Override
    public void atualizar() {
        tabela.setDados(servicos.aquisicoes().listar());
    }

    private void registrar() {
        if (new AquisicaoDialog(this, servicos).abrir()) {
            atualizar();
            Toast.mostrar(this, "Aquisição registrada e exemplares criados");
        }
    }
}
