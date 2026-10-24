package locadora.view.componente;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.model.Recomendacao;
import locadora.model.Titulo;
import locadora.view.Tema;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/* Faixa de titulos recomendados: cada cartao tem a capa e, ao lado, o nome e o motivo
   ("porque alugou Matrix" ou "popular na locadora").
   Sem recomendacoes, mostra um aviso no lugar, sempre com a mesma altura para o dialogo nao pular.
   setRecomendacoes: troca os titulos mostrados (as capas vem pelo id do titulo)
   setAvisoSemItens: frase mostrada quando nao ha recomendacao
   aoEscolher: acao do clique num titulo (sem ela, a faixa so mostra) */
public class Vitrine extends JPanel {

    // cabe nome e motivo com ate duas linhas cada, mesmo quando a capa e menor
    private static final int ALTURA_MINIMA_DO_CARTAO = 68;

    private final int larguraDaCapa;
    private final int alturaDaCapa;
    private final int alturaDoCartao;
    private final int larguraDoCartao;
    private final Fileira cartoes = new Fileira(Tema.ESPACO_3);
    private final JLabel aviso = new JLabel();
    private Consumer<Titulo> aoEscolher;

    public Vitrine(int larguraDaCapa, int larguraDoCartao) {
        super(new BorderLayout());
        this.larguraDaCapa = larguraDaCapa;
        this.alturaDaCapa = larguraDaCapa * 3 / 2;
        this.alturaDoCartao = Math.max(alturaDaCapa, ALTURA_MINIMA_DO_CARTAO);
        this.larguraDoCartao = larguraDoCartao;
        setOpaque(false);
        aviso.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        setPreferredSize(new Dimension(0, alturaDoCartao));
        add(aviso, BorderLayout.CENTER);
    }

    public void setRecomendacoes(List<Recomendacao> recomendacoes, Map<Integer, byte[]> capas) {
        cartoes.removeAll();
        recomendacoes.forEach(recomendacao -> cartoes.adicionar(cartao(recomendacao, capas.get(recomendacao.titulo().getId()))));
        removeAll();
        add(recomendacoes.isEmpty() ? aviso : cartoes, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void setAvisoSemItens(String texto) {
        aviso.setText(texto);
    }

    public void aoEscolher(Consumer<Titulo> acao) {
        aoEscolher = acao;
    }

    private JComponent cartao(Recomendacao recomendacao, byte[] capa) {
        int larguraDoTexto = larguraDoCartao - larguraDaCapa - Tema.ESPACO_2;
        JLabel nome = new JLabel(quebrando(recomendacao.titulo().getNome(), larguraDoTexto));
        nome.putClientProperty(FlatClientProperties.STYLE, "font: -1 bold");
        JLabel motivo = new JLabel(quebrando(recomendacao.porque() == null ? "Popular na locadora"
                : "Porque alugou " + recomendacao.porque().getNome(), larguraDoTexto));
        motivo.putClientProperty(FlatClientProperties.STYLE, "font: -2; foreground: $Locadora.suave");
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.add(nome);
        textos.add(motivo);

        JPanel cartao = new JPanel(new BorderLayout(Tema.ESPACO_2, 0));
        cartao.setOpaque(false);
        JLabel imagem = new JLabel(new Capa(capa, larguraDaCapa, alturaDaCapa));
        imagem.setVerticalAlignment(JLabel.TOP);
        cartao.add(imagem, BorderLayout.WEST);
        cartao.add(textos, BorderLayout.CENTER);
        Dimension tamanho = new Dimension(larguraDoCartao, alturaDoCartao);
        cartao.setPreferredSize(tamanho);
        cartao.setMaximumSize(tamanho);
        if (aoEscolher != null) {
            cartao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cartao.setToolTipText("Escolher " + recomendacao.titulo().getNome());
            cartao.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    aoEscolher.accept(recomendacao.titulo());
                }
            });
        }
        return cartao;
    }

    // no HTML do Swing, pt equivale a 1 pixel
    private static String quebrando(String texto, int largura) {
        return "<html><div style='width: " + largura + "pt'>" + texto + "</div></html>";
    }
}
