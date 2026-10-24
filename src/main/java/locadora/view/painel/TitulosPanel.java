package locadora.view.painel;

import locadora.Main;
import locadora.model.Estoque;
import locadora.model.Titulo;
import locadora.service.TituloService;
import locadora.view.Icone;
import locadora.view.componente.Botoes;
import locadora.view.componente.Cabecalho;
import locadora.view.componente.Capa;
import locadora.view.componente.EstadoVazio;
import locadora.view.componente.TabelaBusca;
import locadora.view.componente.Toast;
import locadora.view.dialogo.TituloDetalheDialog;
import locadora.view.dialogo.TituloDialog;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static locadora.view.componente.ModeloTabela.dinheiro;
import static locadora.view.componente.ModeloTabela.numero;
import static locadora.view.componente.ModeloTabela.texto;

/* Tela de titulos: tabela com capa, busca e filtro por tipo, cadastro, edicao e detalhe com exemplares e linha do tempo.
   atualizar: recarrega titulos e estoque
   abrir: seleciona um titulo e abre o detalhe dele (usado pela busca global) */
public class TitulosPanel extends Painel {

    private static final String TODOS = "Todos os tipos";
    private static final int LARGURA_DA_MINIATURA = 28;
    private static final int ALTURA_DA_MINIATURA = 40;
    private static final int ALTURA_DA_LINHA = 48;

    private final Main.Servicos servicos;
    private final TabelaBusca<Titulo> tabela;
    private final JComboBox<String> tipo = new JComboBox<>(Stream.concat(Stream.of(TODOS), TituloService.TIPOS.stream()).toArray(String[]::new));
    private final JButton editar = Botoes.secundario("Editar", Icone.LAPIS, this::editar);
    private final JButton detalhe = Botoes.secundario("Exemplares", Icone.LISTA, () -> detalhar(tabelaSelecionado()));
    private final Capa semCapa = new Capa(null, LARGURA_DA_MINIATURA, ALTURA_DA_MINIATURA);
    private Map<Integer, Estoque> estoque = Map.of();
    private Map<Integer, Capa> miniaturas = Map.of();

    public TitulosPanel(Main.Servicos servicos) {
        this.servicos = servicos;
        tabela = new TabelaBusca<>(List.of(
                texto("Título", Titulo::getNome),
                texto("Tipo", Titulo::tipo),
                numero("Ano", Titulo::getAno),
                numero("Prazo (dias)", Titulo::getPrazoMaximoDias),
                numero("Disponíveis", (Titulo t) -> estoqueDe(t).disponiveis()),
                numero("No acervo", (Titulo t) -> estoqueDe(t).noAcervo()),
                dinheiro("Locação", Titulo::valorLocacao),
                dinheiro("Multa por dia", Titulo::valorMultaDiaria)),
                "Buscar por título, tipo ou ano",
                new EstadoVazio(Icone.DISCO, "Nenhum título no acervo", "Cadastre o primeiro título para começar.",
                        Botoes.primario("Novo título", Icone.MAIS, this::novo)));
        tipo.addActionListener(e -> tabela.filtrar(t -> tipo.getSelectedIndex() == 0 || t.tipo().equals(tipo.getSelectedItem())));
        tabela.comMiniatura(t -> miniaturas.getOrDefault(t.getId(), semCapa), ALTURA_DA_LINHA);
        tabela.adicionarNaBarra(tipo);
        tabela.permitirExportar(servicos.arquivos(), "titulos");
        tabela.aoAbrir(this::detalhar);
        tabela.aoSelecionar(t -> {
            editar.setEnabled(t != null);
            detalhe.setEnabled(t != null);
        });
        editar.setEnabled(false);
        detalhe.setEnabled(false);

        add(new Cabecalho("Títulos", "Blu-rays, DVDs, CDs e fitas do acervo", detalhe, editar,
                Botoes.primario("Novo título", Icone.MAIS, this::novo)), BorderLayout.NORTH);
        add(tabela, BorderLayout.CENTER);
    }

    @Override
    public void atualizar() {
        estoque = servicos.titulos().estoque();
        miniaturas = servicos.capas().capas().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, capa -> new Capa(capa.getValue(), LARGURA_DA_MINIATURA, ALTURA_DA_MINIATURA)));
        tabela.setDados(servicos.titulos().listar());
    }

    public void abrir(int tituloId) {
        tabela.selecionar(t -> t.getId() == tituloId);
        if (tabela.selecionado() != null)
            detalhar(tabela.selecionado());
    }

    private Estoque estoqueDe(Titulo titulo) {
        return estoque.getOrDefault(titulo.getId(), Estoque.VAZIO);
    }

    private Titulo tabelaSelecionado() {
        return tabela.selecionado();
    }

    private void novo() {
        if (new TituloDialog(this, servicos, null).abrir()) {
            atualizar();
            Toast.mostrar(this, "Título cadastrado");
        }
    }

    private void editar() {
        if (new TituloDialog(this, servicos, tabela.selecionado()).abrir()) {
            atualizar();
            Toast.mostrar(this, "Título atualizado");
        }
    }

    private void detalhar(Titulo titulo) {
        new TituloDetalheDialog(this, servicos, titulo).setVisible(true);
        atualizar();
    }
}
