package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Cliente;
import locadora.model.Exemplar;
import locadora.model.Situacao;
import locadora.service.Formato;
import locadora.service.RegraException;
import locadora.view.componente.Seletor;
import locadora.view.componente.Vitrine;

import javax.swing.JLabel;
import java.awt.Component;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/* Nova locacao: escolhe o cliente, depois um exemplar disponivel (ou separado para ele) e confirma.
   Ao escolher o cliente, aparecem as recomendacoes que tem exemplar disponivel; o clique ja escolhe o exemplar.
   O resumo mostra a data de devolucao e o valor antes de confirmar. */
public class NovaLocacaoDialog extends Formulario {

    private static final int LARGURA_DA_CAPA = 40;
    private static final int LARGURA_DO_CARTAO = 136;
    private static final int MAXIMO_DE_SUGESTOES = 3;

    private final Main.Servicos servicos;
    private final Cliente fixo;
    private final Seletor<Cliente> cliente = new Seletor<>("Buscar cliente por nome ou CPF", Cliente::getNome, Cliente::cpfFormatado);
    private final Seletor<Exemplar> exemplar = new Seletor<>("Buscar título ou código do exemplar",
            (Exemplar e) -> e.getTitulo().getNome(), NovaLocacaoDialog::detalhe);
    private final JLabel resumo = new JLabel("Escolha o cliente e o exemplar.");
    private final Vitrine sugestoes = new Vitrine(LARGURA_DA_CAPA, LARGURA_DO_CARTAO);

    public NovaLocacaoDialog(Component origem, Main.Servicos servicos, Cliente fixo) {
        super(origem, "Nova locação", "Confirmar locação");
        this.servicos = servicos;
        this.fixo = fixo;
        resumo.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
        exemplar.aoSelecionar(e -> mostrarResumo());
        sugestoes.aoEscolher(titulo -> exemplar.selecionar(e -> e.getTitulo().getId() == titulo.getId()));
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
        campo("Sugestões disponíveis para o cliente", sugestoes);
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
        List<Exemplar> alugaveis = escolhido == null ? List.of() : servicos.locacoes().alugaveisPara(escolhido.getId());
        exemplar.setItens(alugaveis);
        mostrarSugestoes(escolhido, alugaveis);
        mostrarResumo();
    }

    // so as recomendacoes que tem exemplar para levar agora; o clique escolhe o exemplar
    private void mostrarSugestoes(Cliente escolhido, List<Exemplar> alugaveis) {
        Set<Integer> titulosAlugaveis = alugaveis.stream().map(e -> e.getTitulo().getId()).collect(Collectors.toSet());
        sugestoes.setAvisoSemItens(escolhido == null ? "Escolha o cliente para ver sugestões." : "Nenhuma sugestão disponível agora.");
        sugestoes.setRecomendacoes(escolhido == null ? List.of() : servicos.locacoes().recomendacoes(escolhido.getId()).stream()
                .filter(r -> titulosAlugaveis.contains(r.titulo().getId()))
                .limit(MAXIMO_DE_SUGESTOES)
                .toList(), servicos.capas().capas());
    }

    private void mostrarResumo() {
        Exemplar escolhido = exemplar.selecionado();
        resumo.setText(escolhido == null ? "Escolha o cliente e o exemplar."
                : "Devolver até " + Formato.data(escolhido.getTitulo().devolucaoPrevista(servicos.locacoes().hoje()))
                + " · " + Formato.dinheiro(escolhido.getTitulo().valorLocacao()));
    }

    private static String detalhe(Exemplar exemplar) {
        String situacao = exemplar.getSituacao() == Situacao.RESERVADO ? "separado para este cliente" : "disponível";
        return exemplar.getCodigo() + " · " + exemplar.getTitulo().tipo() + " · " + situacao;
    }
}
