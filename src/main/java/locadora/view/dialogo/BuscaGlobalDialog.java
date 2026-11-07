package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.service.BuscaService;
import locadora.view.Icone;
import locadora.view.Tema;
import locadora.view.componente.Eventos;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

/**
 * Busca global (Ctrl+K): campo grande no topo e resultados de titulos, clientes e exemplares abaixo. Setas navegam, Enter abre o resultado escolhido, Esc ou clicar fora fecha.
 */
public class BuscaGlobalDialog extends JDialog {

    private final BuscaService busca;
    private final Consumer<BuscaService.Resultado> aoEscolher;
    private final JTextField campo = new JTextField();
    private final DefaultListModel<BuscaService.Resultado> resultados = new DefaultListModel<>();
    private final JList<BuscaService.Resultado> lista = new JList<>(resultados);
    private final JLabel aviso = new JLabel("Digite pelo menos 2 letras para buscar.");

    public BuscaGlobalDialog(JFrame dono, BuscaService busca, Consumer<BuscaService.Resultado> aoEscolher) {
        super(dono, "Buscar", ModalityType.MODELESS);
        this.busca = busca;
        this.aoEscolher = aoEscolher;
        setUndecorated(true);

        campo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar título, cliente, CPF ou código do exemplar (ex.: T4-001)");
        campo.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, Icone.LUPA.de(20, "Locadora.suave"));
        campo.putClientProperty(FlatClientProperties.STYLE, "font: +4; margin: 8,12,8,12; borderWidth: 0; focusWidth: 0");
        Eventos.aoDigitar(campo, this::buscar);
        Eventos.teclaNoComponente(campo, "DOWN", () -> mover(1));
        Eventos.teclaNoComponente(campo, "UP", () -> mover(-1));
        Eventos.teclaNoComponente(campo, "ENTER", this::escolher);
        Eventos.tecla(getRootPane(), "ESCAPE", this::dispose);

        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer(new Linha());
        Eventos.duploClique(lista, this::escolher);
        aviso.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        aviso.setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_2, Tema.ESPACO_4, Tema.ESPACO_3, Tema.ESPACO_4));

        JScrollPane rolagem = new JScrollPane(lista);
        rolagem.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.cor("Component.borderColor")));
        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setBorder(BorderFactory.createLineBorder(Tema.cor("Component.borderColor")));
        conteudo.add(campo, BorderLayout.NORTH);
        conteudo.add(rolagem, BorderLayout.CENTER);
        conteudo.add(aviso, BorderLayout.SOUTH);
        setContentPane(conteudo);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                dispose();
            }
        });
        setSize(640, 440);
        setLocation(dono.getX() + (dono.getWidth() - getWidth()) / 2, dono.getY() + dono.getHeight() / 6);
    }

    private void buscar() {
        List<BuscaService.Resultado> achados = busca.buscar(campo.getText());
        resultados.clear();
        achados.forEach(resultados::addElement);
        if (!achados.isEmpty())
            lista.setSelectedIndex(0);
        aviso.setText(campo.getText().trim().length() < 2 ? "Digite pelo menos 2 letras para buscar."
                : achados.isEmpty() ? "Nada encontrado." : "↑ ↓ para navegar · Enter para abrir · Esc para fechar");
    }

    private void mover(int passo) {
        if (resultados.isEmpty())
            return;
        int alvo = Math.max(0, Math.min(resultados.size() - 1, lista.getSelectedIndex() + passo));
        lista.setSelectedIndex(alvo);
        lista.ensureIndexIsVisible(alvo);
    }

    private void escolher() {
        BuscaService.Resultado escolhido = lista.getSelectedValue();
        if (escolhido == null)
            return;
        dispose();
        aoEscolher.accept(escolhido);
    }

    /**
     * Linha do resultado: icone do tipo, nome em destaque e detalhe embaixo; a escolhida ganha um fundo arredondado suave.
     */
    private static class Linha extends JPanel implements ListCellRenderer<BuscaService.Resultado> {

        private final JLabel icone = new JLabel();
        private final JLabel rotulo = new JLabel();
        private final JLabel detalhe = new JLabel();
        private final JLabel tipo = new JLabel();
        private boolean escolhida;

        Linha() {
            super(new BorderLayout(Tema.ESPACO_3, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(Tema.ESPACO_2, Tema.ESPACO_4, Tema.ESPACO_2, Tema.ESPACO_4));
            rotulo.putClientProperty(FlatClientProperties.STYLE_CLASS, "semibold");
            JPanel textos = new JPanel(new BorderLayout());
            textos.setOpaque(false);
            textos.add(rotulo, BorderLayout.NORTH);
            textos.add(detalhe, BorderLayout.SOUTH);
            add(icone, BorderLayout.WEST);
            add(textos, BorderLayout.CENTER);
            add(tipo, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends BuscaService.Resultado> lista, BuscaService.Resultado resultado,
                                                      int indice, boolean selecionada, boolean foco) {
            Icone forma = switch (resultado.tipo()) {
                case TITULO -> Icone.DISCO;
                case CLIENTE -> Icone.PESSOA;
                case EXEMPLAR -> Icone.CAIXA;
            };
            icone.setIcon(forma.de(22, "Component.accentColor"));
            rotulo.setText(resultado.rotulo());
            detalhe.setText(resultado.detalhe());
            tipo.setText(resultado.tipo().toString());
            escolhida = selecionada;
            rotulo.setForeground(lista.getForeground());
            detalhe.setForeground(Tema.suave());
            tipo.setForeground(selecionada ? Tema.destaque() : Tema.suave());
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!escolhida)
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Tema.cor("Locadora.selecao"));
            g2.fill(new RoundRectangle2D.Float(Tema.ESPACO_2, 2, getWidth() - 2 * Tema.ESPACO_2, getHeight() - 4, 10, 10));
            g2.dispose();
        }
    }
}
