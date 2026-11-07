package locadora.view;

import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Icones desenhados com Java2D numa grade de 18x18 (o projeto nao usa biblioteca de icones).
 */
public enum Icone {

    PAINEL(g -> {
        g.draw(new RoundRectangle2D.Float(2, 2, 6, 6, 2, 2));
        g.draw(new RoundRectangle2D.Float(10, 2, 6, 6, 2, 2));
        g.draw(new RoundRectangle2D.Float(2, 10, 6, 6, 2, 2));
        g.draw(new RoundRectangle2D.Float(10, 10, 6, 6, 2, 2));
    }),
    DISCO(g -> {
        g.draw(new Ellipse2D.Float(2, 2, 14, 14));
        g.draw(new Ellipse2D.Float(7, 7, 4, 4));
        g.draw(new Arc2D.Float(4.5f, 4.5f, 9, 9, 100, 70, Arc2D.OPEN));
    }),
    PESSOA(g -> {
        g.draw(new Ellipse2D.Float(6, 2, 6, 6));
        g.draw(new Arc2D.Float(3, 10, 12, 11, 0, 180, Arc2D.OPEN));
    }),
    CRACHA(g -> {
        g.draw(new RoundRectangle2D.Float(2, 4, 14, 11, 3, 3));
        g.draw(new Ellipse2D.Float(4.5f, 7, 4, 4));
        g.draw(new Line2D.Float(11, 8, 13.5f, 8));
        g.draw(new Line2D.Float(11, 11, 13.5f, 11));
        g.draw(new Line2D.Float(7, 2, 11, 2));
    }),
    TROCA(g -> {
        g.draw(new Line2D.Float(3, 6, 15, 6));
        g.draw(caminho(12, 3, 15, 6, 12, 9));
        g.draw(new Line2D.Float(15, 12, 3, 12));
        g.draw(caminho(6, 9, 3, 12, 6, 15));
    }),
    MARCADOR(g -> {
        Path2D.Float marcador = caminho(5, 2, 13, 2, 13, 16, 9, 12.5f, 5, 16);
        marcador.closePath();
        g.draw(marcador);
    }),
    CAIXA(g -> {
        g.draw(new RoundRectangle2D.Float(2.5f, 6, 13, 10, 2, 2));
        g.draw(new RoundRectangle2D.Float(1.5f, 2.5f, 15, 3.5f, 2, 2));
        g.draw(new Line2D.Float(7, 10, 11, 10));
    }),
    ALERTA(g -> {
        Path2D.Float triangulo = caminho(9, 2, 16.5f, 15.5f, 1.5f, 15.5f);
        triangulo.closePath();
        g.draw(triangulo);
        g.draw(new Line2D.Float(9, 7, 9, 10.5f));
        g.fill(new Ellipse2D.Float(8.1f, 12.4f, 1.8f, 1.8f));
    }),
    LUPA(g -> {
        g.draw(new Ellipse2D.Float(2, 2, 10, 10));
        g.draw(new Line2D.Float(10.6f, 10.6f, 16, 16));
    }),
    SOL(g -> {
        g.draw(new Ellipse2D.Float(5.5f, 5.5f, 7, 7));
        for (int i = 0; i < 8; i++) {
            double angulo = Math.PI / 4 * i;
            g.draw(new Line2D.Double(9 + 6 * Math.cos(angulo), 9 + 6 * Math.sin(angulo), 9 + 8 * Math.cos(angulo), 9 + 8 * Math.sin(angulo)));
        }
    }),
    LUA(g -> {
        Area lua = new Area(new Ellipse2D.Float(2, 2, 14, 14));
        lua.subtract(new Area(new Ellipse2D.Float(7, -1, 13, 13)));
        g.draw(lua);
    }),
    SAIR(g -> {
        g.draw(caminho(10, 2.5f, 3, 2.5f, 3, 15.5f, 10, 15.5f));
        g.draw(new Line2D.Float(7, 9, 16, 9));
        g.draw(caminho(13, 6, 16, 9, 13, 12));
    }),
    BANCO(g -> {
        g.draw(new Ellipse2D.Float(3, 2, 12, 4));
        g.draw(new Line2D.Float(3, 4, 3, 14));
        g.draw(new Line2D.Float(15, 4, 15, 14));
        g.draw(new Arc2D.Float(3, 7, 12, 4, 180, 180, Arc2D.OPEN));
        g.draw(new Arc2D.Float(3, 12, 12, 4, 180, 180, Arc2D.OPEN));
    }),
    MAIS(g -> {
        g.draw(new Line2D.Float(9, 3, 9, 15));
        g.draw(new Line2D.Float(3, 9, 15, 9));
    }),
    EXPORTAR(g -> {
        g.draw(new Line2D.Float(9, 2, 9, 11));
        g.draw(caminho(5.5f, 7.5f, 9, 11, 12.5f, 7.5f));
        g.draw(caminho(3, 12, 3, 15.5f, 15, 15.5f, 15, 12));
    }),
    CHAVE(g -> {
        g.draw(new Ellipse2D.Float(2, 6, 6, 6));
        g.draw(new Line2D.Float(8, 9, 16, 9));
        g.draw(new Line2D.Float(13, 9, 13, 12));
        g.draw(new Line2D.Float(15.5f, 9, 15.5f, 11.5f));
    }),
    LAPIS(g -> {
        Path2D.Float lapis = caminho(3, 15, 4, 11, 12, 3, 15, 6, 7, 14);
        lapis.closePath();
        g.draw(lapis);
        g.draw(new Line2D.Float(10.5f, 4.5f, 13.5f, 7.5f));
    }),
    RELOGIO(g -> {
        g.draw(new Ellipse2D.Float(2, 2, 14, 14));
        g.draw(caminho(9, 5, 9, 9, 12, 11));
    }),
    DINHEIRO(g -> {
        g.draw(new RoundRectangle2D.Float(1.5f, 4.5f, 15, 9, 3, 3));
        g.draw(new Ellipse2D.Float(6.5f, 6.5f, 5, 5));
    }),
    CONFIRMAR(g -> g.draw(caminho(3.5f, 9.5f, 7.5f, 13.5f, 14.5f, 4.5f))),
    FECHAR(g -> {
        g.draw(new Line2D.Float(4, 4, 14, 14));
        g.draw(new Line2D.Float(14, 4, 4, 14));
    }),
    LISTA(g -> {
        for (int i = 0; i < 3; i++) {
            g.fill(new Ellipse2D.Float(2.2f, 3.2f + 5 * i, 1.6f, 1.6f));
            g.draw(new Line2D.Float(6, 4 + 5 * i, 16, 4 + 5 * i));
        }
    });

    private static final int TAMANHO_PADRAO = 18;
    private static final float GRADE = 18f;

    private final Consumer<Graphics2D> desenho;

    Icone(Consumer<Graphics2D> desenho) {
        this.desenho = desenho;
    }

    /**
     * Icone de 18 px na cor do texto.
     */
    public Icon normal() {
        return de(TAMANHO_PADRAO, "Label.foreground");
    }

    /**
     * Icone no tamanho e na cor (chave do tema) escolhidos.
     */
    public Icon de(int tamanho, String chaveDaCor) {
        return new Desenho(tamanho, chaveDaCor, desenho);
    }

    private static Path2D.Float caminho(float... pontos) {
        Path2D.Float caminho = new Path2D.Float();
        caminho.moveTo(pontos[0], pontos[1]);
        for (int i = 2; i < pontos.length; i += 2)
            caminho.lineTo(pontos[i], pontos[i + 1]);
        return caminho;
    }

    private record Desenho(int tamanho, String chaveDaCor, Consumer<Graphics2D> desenho) implements Icon {

        @Override
        public void paintIcon(Component componente, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                g2.scale(tamanho / GRADE, tamanho / GRADE);
                boolean apagado = componente != null && !componente.isEnabled();
                g2.setColor(UIManager.getColor(apagado ? "Label.disabledForeground" : chaveDaCor));
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                desenho.accept(g2);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return tamanho;
        }

        @Override
        public int getIconHeight() {
            return tamanho;
        }
    }
}
