package locadora.view.painel;

import locadora.Main;
import locadora.model.Locacao;
import locadora.service.Formato;
import locadora.view.Icone;
import locadora.view.componente.Botoes;
import locadora.view.componente.Cabecalho;
import locadora.view.componente.Descricoes;
import locadora.view.componente.EstadoVazio;
import locadora.view.componente.Mensagens;
import locadora.view.componente.TabelaBusca;
import locadora.view.componente.Toast;
import locadora.view.dialogo.NovaLocacaoDialog;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static locadora.view.componente.ModeloTabela.data;
import static locadora.view.componente.ModeloTabela.dinheiro;
import static locadora.view.componente.ModeloTabela.texto;

/* Tela de locacoes: nova locacao, devolucao com previa da multa, pagamento de multa e renovacao.
   Atrasadas e multas pendentes aparecem em vermelho.
   atualizar: recarrega as locacoes */
public class LocacoesPanel extends Painel {

    private static final String[] FILTROS = {"Em aberto", "Atrasadas", "Com multa pendente", "Todas"};

    private final Main.Servicos servicos;
    private final TabelaBusca<Locacao> tabela;
    private final JComboBox<String> filtro = new JComboBox<>(FILTROS);
    private final JButton devolver = Botoes.secundario("Devolver", Icone.CONFIRMAR, this::devolver);
    private final JButton renovar = Botoes.secundario("Renovar", Icone.RELOGIO, this::renovar);
    private final JButton pagar = Botoes.secundario("Pagar multa", Icone.DINHEIRO, this::pagar);

    public LocacoesPanel(Main.Servicos servicos) {
        this.servicos = servicos;
        tabela = new TabelaBusca<>(List.of(
                texto("Cliente", (Locacao l) -> l.getCliente().getNome()),
                texto("Título", (Locacao l) -> l.getExemplar().getTitulo().getNome()),
                texto("Exemplar", (Locacao l) -> l.getExemplar().getCodigo()),
                data("Locada em", Locacao::getDataLocacao),
                data("Prevista", Locacao::getDataPrevista),
                data("Devolvida", Locacao::getDataDevolucao),
                dinheiro("Valor", Locacao::getValorLocacao),
                dinheiro("Multa", (Locacao l) -> l.ativa() ? l.multaAte(hoje()) : l.getMulta()),
                texto("Situação", (Locacao l) -> Descricoes.situacao(l, hoje()))),
                "Buscar por cliente, título ou código",
                new EstadoVazio(Icone.TROCA, "Nenhuma locação ainda", "Registre a primeira locação da locadora.",
                        Botoes.primario("Nova locação", Icone.MAIS, this::nova)));
        filtro.addActionListener(e -> tabela.filtrar(this::passaNoFiltro));
        tabela.adicionarNaBarra(filtro);
        tabela.filtrar(this::passaNoFiltro);
        tabela.destacar(l -> l.atrasada(hoje()) || l.multaPendente());
        tabela.permitirExportar(servicos.arquivos(), "locacoes");
        tabela.aoSelecionar(this::ajustarBotoes);
        tabela.aoAbrir(l -> {
            if (l.ativa())
                devolver();
        });
        ajustarBotoes(null);

        add(new Cabecalho("Locações", "Empréstimos, devoluções e multas", pagar, renovar, devolver,
                Botoes.primario("Nova locação", Icone.MAIS, this::nova)), BorderLayout.NORTH);
        add(tabela, BorderLayout.CENTER);
    }

    @Override
    public void atualizar() {
        tabela.setDados(servicos.locacoes().listar());
        ajustarBotoes(tabela.selecionado());
    }

    private LocalDate hoje() {
        return servicos.locacoes().hoje();
    }

    private boolean passaNoFiltro(Locacao locacao) {
        return switch (filtro.getSelectedIndex()) {
            case 0 -> locacao.ativa();
            case 1 -> locacao.atrasada(hoje());
            case 2 -> locacao.multaPendente();
            default -> true;
        };
    }

    private void ajustarBotoes(Locacao locacao) {
        devolver.setEnabled(locacao != null && locacao.ativa());
        renovar.setEnabled(locacao != null && locacao.ativa());
        pagar.setEnabled(locacao != null && locacao.multaPendente());
    }

    private void nova() {
        if (new NovaLocacaoDialog(this, servicos, null).abrir()) {
            atualizar();
            Toast.mostrar(this, "Locação registrada");
        }
    }

    private void devolver() {
        Locacao locacao = tabela.selecionado();
        BigDecimal multa = servicos.locacoes().previaMulta(locacao.getId());
        String detalhe = multa.signum() > 0
                ? "Atraso de " + locacao.diasAtraso(hoje()) + " dia(s): multa de " + Formato.dinheiro(multa) + " (" + locacao.getExemplar().getTitulo().tipo()
                + " cobra " + Formato.dinheiro(locacao.getExemplar().getTitulo().valorMultaDiaria()) + " por dia)."
                : "Dentro do prazo, sem multa.";
        String pergunta = "Registrar a devolução de " + Descricoes.exemplar(locacao.getExemplar()) + " por " + locacao.getCliente().getNome() + "?\n" + detalhe;
        if (!Mensagens.confirmar(this, "Devolução", pergunta, "Registrar devolução"))
            return;
        if (Mensagens.executar(this, () -> servicos.locacoes().devolver(locacao.getId()))) {
            atualizar();
            avisarSeFicouReservado(locacao);
        }
    }

    private void avisarSeFicouReservado(Locacao locacao) {
        servicos.reservas().prontas().stream()
                .filter(r -> r.getExemplar().getId() == locacao.getExemplar().getId())
                .findFirst()
                .ifPresentOrElse(r -> Mensagens.informar(this, "Exemplar reservado", "Separe o exemplar " + locacao.getExemplar().getCodigo()
                                + ": ele ficou reservado para " + r.getCliente().getNome() + ", o primeiro da fila."),
                        () -> Toast.mostrar(this, "Devolução registrada"));
    }

    private void renovar() {
        Locacao locacao = tabela.selecionado();
        LocalDate novaData = locacao.getDataPrevista().plusDays(locacao.getExemplar().getTitulo().getPrazoMaximoDias());
        String pergunta = "Renovar a locação de " + locacao.getCliente().getNome() + " até " + Formato.data(novaData) + "? A renovação só pode ser feita uma vez.";
        if (Mensagens.confirmar(this, "Renovação", pergunta, "Renovar") && Mensagens.executar(this, () -> servicos.locacoes().renovar(locacao.getId()))) {
            atualizar();
            Toast.mostrar(this, "Locação renovada até " + Formato.data(novaData));
        }
    }

    private void pagar() {
        Locacao locacao = tabela.selecionado();
        String pergunta = "Registrar o pagamento de " + Formato.dinheiro(locacao.getMulta()) + " de multa de " + locacao.getCliente().getNome() + "?";
        if (Mensagens.confirmar(this, "Pagamento de multa", pergunta, "Registrar pagamento")
                && Mensagens.executar(this, () -> servicos.locacoes().pagarMulta(locacao.getId()))) {
            atualizar();
            Toast.mostrar(this, "Pagamento registrado");
        }
    }
}
