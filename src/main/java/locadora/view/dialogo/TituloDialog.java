package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Titulo;
import locadora.service.CapaService;
import locadora.service.Formato;
import locadora.service.TituloService;
import locadora.view.Tema;
import locadora.view.componente.Capa;
import locadora.view.componente.Eventos;
import locadora.view.componente.Fileira;
import locadora.view.componente.Mensagens;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.Component;
import java.time.Year;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/* Cadastro e edicao de titulo; os valores de multa e locacao mudam na tela conforme o tipo escolhido.
   Enquanto o nome e digitado, a capa e a sinopse sao buscadas na Wikipedia depois de uma pausa (debounce).
   Sem internet o cadastro funciona igual, so sem capa. Uma capa ja salva nunca e trocada pela busca. */
public class TituloDialog extends Formulario {

    private static final int PAUSA_DA_DIGITACAO_MS = 700;
    private static final int MINIMO_DE_LETRAS = 3;
    private static final int LARGURA_DA_CAPA = 80;
    private static final int ALTURA_DA_CAPA = 116;

    private final Main.Servicos servicos;
    private final Titulo existente;
    private final JComboBox<String> tipo = new JComboBox<>(TituloService.TIPOS.toArray(String[]::new));
    private final JTextField nome = new JTextField(28);
    private final JTextField descricao = new JTextField(28);
    private final JSpinner ano = new JSpinner(new SpinnerNumberModel(2000, TituloService.ANO_MINIMO, Year.now().getValue() + 1, 1));
    private final JSpinner prazo = new JSpinner(new SpinnerNumberModel(5, 1, TituloService.PRAZO_MAXIMO_DIAS, 1));
    private final JLabel valores = new JLabel();
    private final JLabel previa = new JLabel();
    private final JLabel situacaoDaCapa = new JLabel();
    private final Timer espera = new Timer(PAUSA_DA_DIGITACAO_MS, e -> buscarCapa());
    private final boolean capaSalva;
    private byte[] capaAchada;

    public TituloDialog(Component origem, Main.Servicos servicos, Titulo existente) {
        super(origem, existente == null ? "Novo título" : "Editar título", existente == null ? "Cadastrar" : "Salvar");
        this.servicos = servicos;
        this.existente = existente;
        ano.setEditor(new JSpinner.NumberEditor(ano, "#"));
        valores.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        situacaoDaCapa.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        if (existente != null) {
            tipo.setSelectedItem(existente.tipo());
            nome.setText(existente.getNome());
            descricao.setText(existente.getDescricao());
            ano.setValue(existente.getAno());
            prazo.setValue(existente.getPrazoMaximoDias());
        }
        byte[] salva = existente == null ? null : servicos.capas().capa(existente.getId());
        capaSalva = salva != null;
        mostrarCapa(salva, capaSalva ? "Capa salva." : "Digite o nome para buscar a capa e a sinopse na Wikipédia.");

        espera.setRepeats(false);
        Eventos.aoDigitar(nome, espera::restart);
        tipo.addActionListener(e -> {
            mostrarValores();
            espera.restart();
        });
        campos("Tipo de mídia", tipo, "Prazo de empréstimo (dias)", prazo);
        campo("Nome", nome);
        campo("Descrição", descricao);
        campos("Ano", ano, "Valores do tipo", valores);
        campo("Capa", new Fileira(Tema.ESPACO_3, previa, situacaoDaCapa));
        mostrarValores();
        if (existente != null && !capaSalva)
            espera.start();
    }

    @Override
    protected void salvar() {
        Titulo salvo = servicos.titulos().salvar((String) tipo.getSelectedItem(), existente == null ? 0 : existente.getId(), nome.getText(),
                descricao.getText(), (Integer) ano.getValue(), (Integer) prazo.getValue());
        if (capaAchada != null)
            servicos.capas().guardar(salvo.getId(), capaAchada);
    }

    private void mostrarValores() {
        Titulo amostra = servicos.titulos().amostra((String) tipo.getSelectedItem());
        valores.setText("Locação " + Formato.dinheiro(amostra.valorLocacao()) + " · multa " + Formato.dinheiro(amostra.valorMultaDiaria()) + "/dia");
    }

    private void mostrarCapa(byte[] imagem, String situacao) {
        previa.setIcon(new Capa(imagem, LARGURA_DA_CAPA, ALTURA_DA_CAPA));
        situacaoDaCapa.setText(situacao);
    }

    // a busca roda fora da tela; se o nome mudou enquanto ela rodava, a resposta antiga e descartada
    private void buscarCapa() {
        String procurado = nome.getText().trim();
        String tipoProcurado = (String) tipo.getSelectedItem();
        String categoria = servicos.titulos().amostra(tipoProcurado).categoria();
        if (capaSalva || procurado.length() < MINIMO_DE_LETRAS)
            return;
        situacaoDaCapa.setText("Buscando capa…");
        new SwingWorker<Optional<CapaService.Achado>, Void>() {
            @Override
            protected Optional<CapaService.Achado> doInBackground() {
                return servicos.capas().buscar(procurado, categoria);
            }

            @Override
            protected void done() {
                if (!procurado.equals(nome.getText().trim()) || !tipoProcurado.equals(tipo.getSelectedItem()))
                    return;
                try {
                    mostrarAchado(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Mensagens.inesperado(TituloDialog.this, e.getCause());
                }
            }
        }.execute();
    }

    private void mostrarAchado(Optional<CapaService.Achado> achado) {
        capaAchada = achado.map(CapaService.Achado::imagem).orElse(null);
        if (achado.isEmpty()) {
            mostrarCapa(null, "Sem capa: sem internet ou nada encontrado.");
            return;
        }
        if (descricao.getText().isBlank())
            descricao.setText(achado.get().sinopse());
        mostrarCapa(capaAchada, "Capa e sinopse da Wikipédia.");
    }
}
