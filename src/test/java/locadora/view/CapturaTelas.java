package locadora.view;

import locadora.Main;
import locadora.model.Cliente;
import locadora.model.Titulo;
import locadora.view.dialogo.AquisicaoDialog;
import locadora.view.dialogo.BuscaGlobalDialog;
import locadora.view.dialogo.ClienteDetalheDialog;
import locadora.view.dialogo.ClienteDialog;
import locadora.view.dialogo.NovaLocacaoDialog;
import locadora.view.dialogo.PerdaDialog;
import locadora.view.dialogo.TituloDetalheDialog;
import locadora.view.dialogo.TituloDialog;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Clock;
import java.util.List;
import java.util.Locale;

/* Gera imagens das telas e dos dialogos principais nos temas claro e escuro em target/telas,
   usando um banco novo com o seed e o login do admin. Nao abre nenhuma janela na tela.
   Uso: java -cp target/classes:target/test-classes:<dependencias> locadora.view.CapturaTelas */
public class CapturaTelas {

    private static final List<String> TELAS = List.of(MainFrame.PAINEL, MainFrame.LOCACOES, MainFrame.TITULOS, MainFrame.CLIENTES,
            MainFrame.RESERVAS, MainFrame.AQUISICOES, MainFrame.PERDAS, MainFrame.FUNCIONARIOS);

    public static void main(String[] args) throws Exception {
        Path pasta = Files.createDirectories(Path.of("target", "telas"));
        Path banco = Files.createTempFile("locadora-telas", ".db");
        Files.delete(banco);
        Tema.instalar();
        Main.Servicos servicos = Main.montar(banco, Clock.systemDefaultZone());
        servicos.auth().entrar("admin", "admin123");
        SwingUtilities.invokeAndWait(() -> {
            capturarTema(servicos, pasta, "claro");
            Tema.alternar();
            capturarTema(servicos, pasta, "escuro");
        });
        Files.deleteIfExists(banco);
        System.exit(0);
    }

    private static void capturarTema(Main.Servicos servicos, Path pasta, String tema) {
        capturar(new LoginFrame(servicos), pasta, "login", tema);
        MainFrame principal = new MainFrame(servicos);
        principal.addNotify();
        for (String tela : TELAS) {
            principal.mostrar(tela);
            salvar(principal, pasta.resolve(nomeDoArquivo(tela) + "-" + tema + ".png"));
        }
        Titulo matrix = servicos.titulos().buscar(7);
        Cliente ana = servicos.clientes().buscar(1);
        capturar(new TituloDetalheDialog(principal, servicos, matrix), pasta, "titulo-detalhe", tema);
        capturar(new ClienteDetalheDialog(principal, servicos, ana), pasta, "cliente-ficha", tema);
        capturar(new TituloDialog(principal, servicos.titulos(), matrix), pasta, "titulo-formulario", tema);
        capturar(new ClienteDialog(principal, servicos.clientes(), null), pasta, "cliente-formulario", tema);
        capturar(new NovaLocacaoDialog(principal, servicos, null), pasta, "nova-locacao", tema);
        capturar(new AquisicaoDialog(principal, servicos), pasta, "aquisicao", tema);
        capturar(new PerdaDialog(principal, servicos), pasta, "perda", tema);
        BuscaGlobalDialog busca = new BuscaGlobalDialog(principal, servicos.busca(), resultado -> { });
        primeiroCampo(busca.getContentPane()).setText("ma");
        capturar(busca, pasta, "busca-global", tema);
        principal.dispose();
    }

    private static void capturar(Window janela, Path pasta, String nome, String tema) {
        if (janela.getWidth() == 0)
            janela.pack();
        janela.addNotify();
        salvar(janela, pasta.resolve(nome + "-" + tema + ".png"));
        janela.dispose();
    }

    private static void salvar(Window janela, Path destino) {
        JComponent raiz = ((RootPaneContainer) janela).getRootPane();
        janela.validate();
        BufferedImage imagem = new BufferedImage(raiz.getWidth(), raiz.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagem.createGraphics();
        raiz.printAll(g);
        g.dispose();
        try {
            ImageIO.write(imagem, "png", destino.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JTextField primeiroCampo(Container recipiente) {
        for (Component filho : recipiente.getComponents()) {
            if (filho instanceof JTextField campo)
                return campo;
            if (filho instanceof Container interno && primeiroCampo(interno) != null)
                return primeiroCampo(interno);
        }
        return null;
    }

    private static String nomeDoArquivo(String tela) {
        String semAcento = Normalizer.normalize(tela, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }
}
