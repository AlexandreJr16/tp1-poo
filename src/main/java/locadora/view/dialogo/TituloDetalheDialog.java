package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Estoque;
import locadora.model.Exemplar;
import locadora.model.Locacao;
import locadora.model.Titulo;
import locadora.service.Formato;
import locadora.view.Tema;
import locadora.view.componente.Botoes;
import locadora.view.componente.CelulaTabela;
import locadora.view.componente.Eventos;
import locadora.view.componente.LinhaDoTempo;
import locadora.view.componente.ModeloTabela;
import locadora.view.componente.Toast;
import locadora.view.Icone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static locadora.view.componente.ModeloTabela.data;
import static locadora.view.componente.ModeloTabela.dinheiro;
import static locadora.view.componente.ModeloTabela.texto;

// detalhe do titulo: valores do tipo, contagem por situacao, exemplares e a linha do tempo do exemplar escolhido
public class TituloDetalheDialog extends JDialog {

    private final Main.Servicos servicos;
    private final Titulo titulo;
    private final ModeloTabela<Exemplar> exemplares;
    private final JTable tabela;
    private final LinhaDoTempo linhaDoTempo = new LinhaDoTempo();
    private Map<Integer, Locacao> ativas = Map.of();

    public TituloDetalheDialog(Component origem, Main.Servicos servicos, Titulo titulo) {
        super(SwingUtilities.getWindowAncestor(origem), titulo.getNome(), ModalityType.APPLICATION_MODAL);
        this.servicos = servicos;
        this.titulo = titulo;
        exemplares = new ModeloTabela<>(List.of(
                texto("Código", Exemplar::getCodigo),
                texto("Situação", Exemplar::getSituacao),
                texto("Com o cliente", (Exemplar e) -> locacaoDe(e) == null ? "" : locacaoDe(e).getCliente().getNome()),
                data("Devolver até", (Exemplar e) -> locacaoDe(e) == null ? null : locacaoDe(e).getDataPrevista()),
                dinheiro("Custo", Exemplar::getCusto)));
        tabela = new JTable(exemplares);
        CelulaTabela.aplicar(tabela, exemplares).destacar(e -> locacaoDe(e) != null && locacaoDe(e).atrasada(servicos.locacoes().hoje()));
        tabela.getSelectionModel().addListSelectionListener(e -> mostrarHistoria());

        JPanel conteudo = new JPanel(new BorderLayout(0, Tema.ESPACO_4));
        conteudo.setBorder(Tema.borda(Tema.ESPACO_5));
        conteudo.add(topo(), BorderLayout.NORTH);
        conteudo.add(centro(), BorderLayout.CENTER);
        conteudo.add(rodape(), BorderLayout.SOUTH);
        setContentPane(conteudo);
        Eventos.tecla(getRootPane(), "ESCAPE", this::dispose);
        setSize(1000, 620);
        setLocationRelativeTo(getOwner());
        carregar();
    }

    private Locacao locacaoDe(Exemplar exemplar) {
        return ativas.get(exemplar.getId());
    }

    private void carregar() {
        ativas = servicos.locacoes().ativas().stream().collect(Collectors.toMap(l -> l.getExemplar().getId(), Function.identity()));
        exemplares.setLinhas(servicos.titulos().exemplares(titulo.getId()));
        CelulaTabela.ajustarLarguras(tabela);
        if (exemplares.getRowCount() > 0)
            tabela.setRowSelectionInterval(0, 0);
    }

    private void mostrarHistoria() {
        int linha = tabela.getSelectedRow();
        linhaDoTempo.setEventos(linha < 0 ? List.of() : servicos.titulos().linhaDoTempo(exemplares.linha(tabela.convertRowIndexToModel(linha)).getId()));
    }

    private JComponent topo() {
        JLabel nome = new JLabel(titulo.getNome());
        nome.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        JLabel ficha = new JLabel(titulo.tipo() + " · " + titulo.getAno() + " · prazo de " + titulo.getPrazoMaximoDias() + " dias · locação "
                + Formato.dinheiro(titulo.valorLocacao()) + " · multa " + Formato.dinheiro(titulo.valorMultaDiaria()) + " por dia de atraso");
        ficha.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        JLabel descricao = new JLabel(titulo.getDescricao());
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(nome);
        textos.add(ficha);
        textos.add(descricao);

        Estoque estoque = servicos.titulos().estoqueDe(titulo.getId());
        JPanel contagem = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACO_2, 0));
        contagem.add(etiqueta(estoque.disponiveis(), "disponível", "disponíveis", "Locadora.sucesso"));
        contagem.add(etiqueta(estoque.alugados(), "alugado", "alugados", "Component.accentColor"));
        contagem.add(etiqueta(estoque.reservados(), "reservado", "reservados", "Locadora.aviso"));
        contagem.add(etiqueta(estoque.perdidos(), "perdido", "perdidos", "Locadora.perigo"));
        JPanel topo = new JPanel(new BorderLayout(Tema.ESPACO_4, 0));
        topo.add(textos, BorderLayout.CENTER);
        topo.add(contagem, BorderLayout.EAST);
        return topo;
    }

    private static JComponent etiqueta(int quantidade, String singular, String plural, String chaveDaCor) {
        JLabel etiqueta = new JLabel("<html><b>" + quantidade + "</b> " + (quantidade == 1 ? singular : plural) + "</html>");
        etiqueta.setOpaque(true);
        etiqueta.putClientProperty(FlatClientProperties.STYLE, "arc: 999; background: $Locadora.cartao; foreground: $" + chaveDaCor);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_1, Tema.ESPACO_3, Tema.ESPACO_1, Tema.ESPACO_3));
        return etiqueta;
    }

    private JComponent centro() {
        JScrollPane lista = new JScrollPane(tabela);
        lista.putClientProperty(FlatClientProperties.STYLE, "arc: 14");
        JLabel cabecalho = new JLabel("Linha do tempo do exemplar");
        cabecalho.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
        JScrollPane historia = new JScrollPane(linhaDoTempo);
        historia.setBorder(BorderFactory.createEmptyBorder());
        JPanel cartao = new JPanel(new BorderLayout(0, Tema.ESPACO_2));
        cartao.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: $Locadora.cartao");
        cartao.setBorder(Tema.borda(Tema.ESPACO_4));
        cartao.add(cabecalho, BorderLayout.NORTH);
        cartao.add(historia, BorderLayout.CENTER);
        JPanel centro = new JPanel(new GridLayout(1, 2, Tema.ESPACO_4, 0));
        centro.add(lista);
        centro.add(cartao);
        centro.setPreferredSize(new Dimension(0, 380));
        return centro;
    }

    private JComponent rodape() {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, Tema.ESPACO_2, 0));
        if (servicos.titulos().estoqueDe(titulo.getId()).disponiveis() == 0)
            rodape.add(Botoes.secundario("Reservar este título", Icone.MARCADOR, this::reservar));
        JButton fechar = Botoes.primario("Fechar", null, this::dispose);
        getRootPane().setDefaultButton(fechar);
        rodape.add(fechar);
        return rodape;
    }

    private void reservar() {
        if (new ReservaDialog(this, servicos, titulo).abrir())
            Toast.mostrar(this, "Reserva registrada");
    }
}
