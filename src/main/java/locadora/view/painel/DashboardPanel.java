package locadora.view.painel;

import locadora.Main;
import locadora.model.Locacao;
import locadora.model.Reserva;
import locadora.service.AuthService;
import locadora.service.DashboardService;
import locadora.service.Formato;
import locadora.view.Icone;
import locadora.view.Tema;
import locadora.view.componente.Botoes;
import locadora.view.componente.Cabecalho;
import locadora.view.componente.CardNumero;
import locadora.view.componente.Quadro;
import locadora.view.dialogo.NovaLocacaoDialog;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import static locadora.view.componente.ModeloTabela.data;
import static locadora.view.componente.ModeloTabela.dinheiro;
import static locadora.view.componente.ModeloTabela.numero;
import static locadora.view.componente.ModeloTabela.texto;

/* Tela inicial: saudacao, seis cards de numeros e tres quadros: proximas devolucoes em duas colunas da grade,
   atrasadas e reservas prontas empilhadas na terceira.
   atualizar: recarrega os numeros e as listas */
public class DashboardPanel extends Painel {

    private final Main.Servicos servicos;
    private final CardNumero ativas = new CardNumero(Icone.TROCA, "Locações em aberto", "Component.accentColor");
    private final CardNumero atrasadas = new CardNumero(Icone.ALERTA, "Atrasadas", "Locadora.perigo");
    private final CardNumero multas = new CardNumero(Icone.DINHEIRO, "Multas em aberto", "Locadora.aviso");
    private final CardNumero faturamento = new CardNumero(Icone.DINHEIRO, "Faturamento do mês", "Locadora.sucesso");
    private final CardNumero titulos = new CardNumero(Icone.DISCO, "Títulos no acervo", "Component.accentColor");
    private final CardNumero reservas = new CardNumero(Icone.MARCADOR, "Reservas para retirada", "Component.accentColor");
    private final Quadro<Locacao> proximas;
    private final Quadro<Locacao> vencidas;
    private final Quadro<Reserva> prontas;

    public DashboardPanel(Main.Servicos servicos, Runnable verLocacoes) {
        this.servicos = servicos;
        proximas = new Quadro<>("Próximas devoluções", Icone.RELOGIO, "Component.accentColor", List.of(
                texto("Cliente", (Locacao l) -> l.getCliente().getNome()),
                texto("Título", (Locacao l) -> l.getExemplar().getTitulo().getNome()),
                texto("Exemplar", (Locacao l) -> l.getExemplar().getCodigo()),
                data("Prevista", Locacao::getDataPrevista)), "Nenhuma devolução prevista.");
        vencidas = new Quadro<>("Atrasadas", Icone.ALERTA, "Locadora.perigo", List.of(
                texto("Cliente", (Locacao l) -> l.getCliente().getNome()),
                numero("Dias", (Locacao l) -> (int) l.diasAtraso(servicos.locacoes().hoje())),
                dinheiro("Multa hoje", (Locacao l) -> l.multaAte(servicos.locacoes().hoje()))), "Nenhuma locação atrasada.");
        prontas = new Quadro<>("Reservas para retirada", Icone.MARCADOR, "Component.accentColor", List.of(
                texto("Cliente", (Reserva r) -> r.getCliente().getNome()),
                texto("Título", (Reserva r) -> r.getTitulo().getNome()),
                texto("Exemplar", (Reserva r) -> r.getExemplar().getCodigo())), "Nenhuma reserva esperando retirada.");
        vencidas.destacar(locacao -> true);

        String nome = AuthService.logado().getNome().split(" ")[0];
        String hoje = Formato.dataPorExtenso(servicos.locacoes().hoje());
        add(new Cabecalho("Olá, " + nome, Character.toUpperCase(hoje.charAt(0)) + hoje.substring(1),
                Botoes.secundario("Ver locações", Icone.LISTA, verLocacoes),
                Botoes.primario("Nova locação", Icone.MAIS, this::novaLocacao)), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, Tema.ESPACO_4, Tema.ESPACO_4));
        cards.setOpaque(false);
        for (CardNumero card : List.of(ativas, atrasadas, multas, faturamento, titulos, reservas))
            cards.add(card);
        JPanel quadros = new JPanel(new BorderLayout(Tema.ESPACO_4, 0));
        quadros.setOpaque(false);
        quadros.add(proximas, BorderLayout.CENTER);
        quadros.add(colunaDaDireita(vencidas, prontas), BorderLayout.EAST);
        JPanel corpo = new JPanel(new BorderLayout(0, Tema.ESPACO_4));
        corpo.setOpaque(false);
        corpo.add(cards, BorderLayout.NORTH);
        corpo.add(quadros, BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);
    }

    @Override
    public void atualizar() {
        DashboardService.Resumo resumo = servicos.painel().resumo();
        ativas.setValor(String.valueOf(resumo.locacoesAtivas()));
        atrasadas.setValor(String.valueOf(resumo.atrasadas()));
        atrasadas.setAlerta(resumo.atrasadas() > 0);
        multas.setValor(Formato.dinheiro(resumo.multasEmAberto()));
        faturamento.setValor(Formato.dinheiro(resumo.faturamentoMes()));
        titulos.setValor(String.valueOf(resumo.titulos()));
        reservas.setValor(String.valueOf(resumo.reservasProntas()));
        proximas.setDados(servicos.painel().proximasDevolucoes());
        vencidas.setDados(servicos.locacoes().atrasadas());
        prontas.setDados(servicos.reservas().prontas());
    }

    // coluna com a largura de um card, para os quadros seguirem a grade de tres colunas dos cards
    private static JPanel colunaDaDireita(JComponent... quadros) {
        JPanel coluna = new JPanel(new GridLayout(quadros.length, 1, 0, Tema.ESPACO_4)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(Math.max(0, (getParent().getWidth() - 2 * Tema.ESPACO_4) / 3), 0);
            }
        };
        coluna.setOpaque(false);
        for (JComponent quadro : quadros)
            coluna.add(quadro);
        return coluna;
    }

    private void novaLocacao() {
        if (new NovaLocacaoDialog(this, servicos, null).abrir())
            atualizar();
    }
}
