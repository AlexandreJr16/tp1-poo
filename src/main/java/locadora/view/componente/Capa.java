package locadora.view.componente;

import locadora.view.Icone;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Capa de um titulo num tamanho fixo, como icone (serve em JLabel e em celula de tabela).
   A imagem e recortada no centro para preencher o espaco, com cantos arredondados.
   Sem imagem (ou imagem invalida), mostra um quadro suave com o icone de disco. */
public class Capa implements Icon {

    private static final Logger LOG = Logger.getLogger(Capa.class.getName());
    // guarda a imagem no dobro do tamanho para continuar nitida em telas de alta densidade
    private static final int NITIDEZ = 2;

    private final int largura;
    private final int altura;
    private final BufferedImage imagem;

    public Capa(byte[] bytes, int largura, int altura) {
        this.largura = largura;
        this.altura = altura;
        this.imagem = recortar(ler(bytes), largura * NITIDEZ, altura * NITIDEZ);
    }

    public boolean temImagem() {
        return imagem != null;
    }

    @Override
    public void paintIcon(Component componente, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            RoundRectangle2D.Float moldura = new RoundRectangle2D.Float(x, y, largura, altura, arco(), arco());
            if (imagem == null) {
                g2.setColor(UIManager.getColor("Locadora.fundoDaCapa"));
                g2.fill(moldura);
                int lado = Math.min(largura, altura) / 2;
                Icone.DISCO.de(lado, "Locadora.suave").paintIcon(componente, g2, x + (largura - lado) / 2, y + (altura - lado) / 2);
            } else {
                g2.clip(moldura);
                g2.drawImage(imagem, x, y, largura, altura, null);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return largura;
    }

    @Override
    public int getIconHeight() {
        return altura;
    }

    private int arco() {
        return Math.max(6, Math.min(largura, altura) / 8);
    }

    private static BufferedImage ler(byte[] bytes) {
        if (bytes == null)
            return null;
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            LOG.log(Level.FINE, "Capa com imagem invalida", e);
            return null;
        }
    }

    // escala para cobrir o espaco todo e corta o que sobra nas bordas
    private static BufferedImage recortar(BufferedImage original, int largura, int altura) {
        if (original == null)
            return null;
        double escala = Math.max((double) largura / original.getWidth(), (double) altura / original.getHeight());
        int larguraEscalada = (int) Math.ceil(original.getWidth() * escala);
        int alturaEscalada = (int) Math.ceil(original.getHeight() * escala);
        Image escalada = original.getScaledInstance(larguraEscalada, alturaEscalada, Image.SCALE_SMOOTH);
        BufferedImage recorte = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = recorte.createGraphics();
        g.drawImage(escalada, (largura - larguraEscalada) / 2, (altura - alturaEscalada) / 2, null);
        g.dispose();
        return recorte;
    }
}
