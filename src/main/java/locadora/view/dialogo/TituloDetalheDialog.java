package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Estoque;
import locadora.model.Exemplar;
import locadora.model.Locacao;
import locadora.model.Titulo;
import locadora.service.Formato;
import locadora.view.Icone;
import locadora.view.Tema;
import locadora.view.componente.Botoes;
import locadora.view.componente.Capa;
import locadora.view.componente.Eventos;
import locadora.view.componente.Fileira;
import locadora.view.componente.LinhaDoTempo;
import locadora.view.componente.Quadro;
import locadora.view.componente.Toast;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static locadora.view.componente.ModeloTabela.data;
import static locadora.view.componente.ModeloTabela.dinheiro;
import static locadora.view.componente.ModeloTabela.texto;

// detalhe do titulo: capa, valores do tipo, contagem por situacao, exemplares e a linha do tempo do exemplar escolhido
public class TituloDetalheDialog extends JDialog {

    private static final int LARGURA_DA_CAPA = 120;
    private static final int ALTURA_DA_CAPA = 176;
    // no HTML do Swing, pt equivale a 1 pixel
    private static final int LARGURA_DA_DESCRICAO = 640;

    private final Main.Servicos servicos;
    private final Titulo titulo;
    private final Quadro<Exemplar> exemplares;
    private final LinhaDoTempo linhaDoTempo = new LinhaDoTempo();
    private final JLabel codigo = new JLabel();
    private Map<Integer, Locacao> ativas = Map.of();

    public TituloDetalheDialog(Component origem, Main.Servicos servicos, Titulo titulo) {
        super(SwingUtilities.getWindowAncestor(origem), titulo.getNome(), ModalityType.APPLICATION_MODAL);
        this.servicos = servicos;
        this.titulo = titulo;
        exemplares = new Quadro<>("Exemplares", Icone.DISCO, "Component.accentColor", List.of(
                texto("Código", Exemplar::getCodigo),
                texto("Situação", Exemplar::getSituacao),
                texto("Com o cliente", (Exemplar e) -> locacaoDe(e) == null ? "" : locacaoDe(e).getCliente().getNome()),
                data("Devolver até", (Exemplar e) -> locacaoDe(e) == null ? null : locacaoDe(e).getDataPrevista()),
                dinheiro("Custo", Exemplar::getCusto)), "Nenhum exemplar no acervo. Registre uma aquisição.");
        exemplares.destacar(e -> locacaoDe(e) != null && locacaoDe(e).atrasada(servicos.locacoes().hoje()));
        exemplares.aoSelecionar(this::mostrarHistoria);

        JPanel conteudo = new JPanel(new BorderLayout(0, Tema.ESPACO_5));
        conteudo.setBorder(Tema.borda(Tema.ESPACO_5));
        conteudo.add(topo(), BorderLayout.NORTH);
        conteudo.add(centro(), BorderLayout.CENTER);
        conteudo.add(rodape(), BorderLayout.SOUTH);
        setContentPane(conteudo);
        Eventos.tecla(getRootPane(), "ESCAPE", this::dispose);
        setSize(1000, 720);
        setLocationRelativeTo(getOwner());
        carregar();
    }

    private Locacao locacaoDe(Exemplar exemplar) {
        return ativas.get(exemplar.getId());
    }

    private void carregar() {
        ativas = servicos.locacoes().ativas().stream().collect(Collectors.toMap(l -> l.getExemplar().getId(), Function.identity()));
        exemplares.setDados(servicos.titulos().exemplares(titulo.getId()));
        exemplares.selecionarPrimeira();
    }

    private void mostrarHistoria(Exemplar exemplar) {
        codigo.setText(exemplar == null ? "" : exemplar.getCodigo());
        linhaDoTempo.setEventos(exemplar == null ? List.of() : servicos.titulos().linhaDoTempo(exemplar.getId()));
    }

    private JComponent topo() {
        JLabel nome = new JLabel(titulo.getNome());
        nome.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        JLabel ficha = new JLabel(titulo.tipo() + " · " + titulo.getAno() + " · prazo de " + titulo.getPrazoMaximoDias() + " dias · locação "
                + Formato.dinheiro(titulo.valorLocacao()) + " · multa " + Formato.dinheiro(titulo.valorMultaDiaria()) + " por dia de atraso");
        ficha.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        JLabel descricao = new JLabel("<html><div style='width: " + LARGURA_DA_DESCRICAO + "pt'>" + titulo.getDescricao() + "</div></html>");
        descricao.setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_2, 0, 0, 0));

        Estoque estoque = servicos.titulos().estoqueDe(titulo.getId());
        Fileira contagem = new Fileira(Tema.ESPACO_2,
                etiqueta(estoque.disponiveis(), "disponível", "disponíveis", "Locadora.sucesso"),
                etiqueta(estoque.alugados(), "alugado", "alugados", "Component.accentColor"),
                etiqueta(estoque.reservados(), "reservado", "reservados", "Locadora.aviso"),
                etiqueta(estoque.perdidos(), "perdido", "perdidos", "Locadora.perigo"));
        contagem.setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_4, 0, 0, 0));
        contagem.setMaximumSize(contagem.getPreferredSize());

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        for (JComponent parte : List.of(nome, ficha, descricao, contagem)) {
            parte.setAlignmentX(Component.LEFT_ALIGNMENT);
            textos.add(parte);
        }
        JPanel topo = new JPanel(new BorderLayout(Tema.ESPACO_5, 0));
        topo.add(new JLabel(new Capa(servicos.capas().capa(titulo.getId()), LARGURA_DA_CAPA, ALTURA_DA_CAPA)), BorderLayout.WEST);
        topo.add(textos, BorderLayout.CENTER);
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
        JLabel cabecalho = new JLabel("Linha do tempo", Icone.RELOGIO.de(18, "Component.accentColor"), SwingConstants.LEADING);
        cabecalho.setIconTextGap(Tema.ESPACO_2);
        cabecalho.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
        codigo.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave; font: bold");
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(cabecalho, BorderLayout.WEST);
        topo.add(codigo, BorderLayout.EAST);
        JScrollPane historia = new JScrollPane(linhaDoTempo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        historia.setBorder(BorderFactory.createEmptyBorder());
        JPanel cartao = new JPanel(new BorderLayout(0, Tema.ESPACO_3));
        cartao.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: $Locadora.cartao");
        cartao.setBorder(Tema.borda(Tema.ESPACO_4));
        cartao.setPreferredSize(new Dimension(LinhaDoTempo.LARGURA + 2 * Tema.ESPACO_4, 0));
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(historia, BorderLayout.CENTER);
        JPanel centro = new JPanel(new BorderLayout(Tema.ESPACO_4, 0));
        centro.add(exemplares, BorderLayout.CENTER);
        centro.add(cartao, BorderLayout.EAST);
        return centro;
    }

    private JComponent rodape() {
        JButton fechar = Botoes.primario("Fechar", null, this::dispose);
        getRootPane().setDefaultButton(fechar);
        Fileira botoes = new Fileira(Tema.ESPACO_2);
        if (servicos.titulos().estoqueDe(titulo.getId()).disponiveis() == 0)
            botoes.adicionar(Botoes.secundario("Reservar este título", Icone.MARCADOR, this::reservar));
        botoes.adicionar(fechar);
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(botoes, BorderLayout.EAST);
        return rodape;
    }

    private void reservar() {
        if (new ReservaDialog(this, servicos, titulo).abrir())
            Toast.mostrar(this, "Reserva registrada");
    }
}
