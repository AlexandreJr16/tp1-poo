package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Cliente;
import locadora.model.Locacao;
import locadora.model.Reserva;
import locadora.service.Formato;
import locadora.view.Icone;
import locadora.view.Tema;
import locadora.view.componente.Botoes;
import locadora.view.componente.CardNumero;
import locadora.view.componente.CelulaTabela;
import locadora.view.componente.Descricoes;
import locadora.view.componente.Eventos;
import locadora.view.componente.Fileira;
import locadora.view.componente.Mensagens;
import locadora.view.componente.ModeloTabela;
import locadora.view.componente.Toast;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static locadora.view.componente.ModeloTabela.data;
import static locadora.view.componente.ModeloTabela.dinheiro;
import static locadora.view.componente.ModeloTabela.texto;

// ficha do cliente: contato, numeros, historico de locacoes, reservas e pagamento das multas pendentes
public class ClienteDetalheDialog extends JDialog {

    private final Main.Servicos servicos;
    private final Cliente cliente;
    private final CardNumero abertas = new CardNumero(Icone.TROCA, "Locações em aberto", "Component.accentColor");
    private final CardNumero multas = new CardNumero(Icone.DINHEIRO, "Multas pendentes", "Locadora.aviso");
    private final CardNumero reservas = new CardNumero(Icone.MARCADOR, "Reservas ativas", "Component.accentColor");
    private final ModeloTabela<Locacao> historico;
    private final ModeloTabela<Reserva> filas;
    private final JTable tabelaDeLocacoes;
    private final JTable tabelaDeReservas;
    private final JButton pagar = Botoes.secundario("Registrar pagamento das multas", Icone.DINHEIRO, this::pagarMultas);

    public ClienteDetalheDialog(Component origem, Main.Servicos servicos, Cliente cliente) {
        super(SwingUtilities.getWindowAncestor(origem), cliente.getNome(), ModalityType.APPLICATION_MODAL);
        this.servicos = servicos;
        this.cliente = cliente;
        LocalDate hoje = servicos.locacoes().hoje();
        historico = new ModeloTabela<>(List.of(
                texto("Título", (Locacao l) -> l.getExemplar().getTitulo().getNome()),
                texto("Exemplar", (Locacao l) -> l.getExemplar().getCodigo()),
                data("Locada em", Locacao::getDataLocacao),
                data("Prevista", Locacao::getDataPrevista),
                data("Devolvida", Locacao::getDataDevolucao),
                dinheiro("Multa", Locacao::getMulta),
                texto("Situação", (Locacao l) -> Descricoes.situacao(l, hoje))));
        filas = new ModeloTabela<>(List.of(
                texto("Título", (Reserva r) -> r.getTitulo().getNome()),
                data("Reservada em", Reserva::getData),
                texto("Situação", Reserva::getSituacao),
                texto("Posição na fila", (Reserva r) -> servicos.reservas().posicao(r) == 0 ? "—" : servicos.reservas().posicao(r) + "º")));
        tabelaDeLocacoes = new JTable(historico);
        tabelaDeReservas = new JTable(filas);

        JPanel conteudo = new JPanel(new BorderLayout(0, Tema.ESPACO_4));
        conteudo.setBorder(Tema.borda(Tema.ESPACO_5));
        conteudo.add(topo(), BorderLayout.NORTH);
        conteudo.add(abas(hoje), BorderLayout.CENTER);
        conteudo.add(rodape(), BorderLayout.SOUTH);
        setContentPane(conteudo);
        Eventos.tecla(getRootPane(), "ESCAPE", this::dispose);
        setSize(980, 640);
        setLocationRelativeTo(getOwner());
        carregar();
    }

    private void carregar() {
        List<Locacao> locacoes = servicos.clientes().historico(cliente.getId());
        List<Reserva> ativas = servicos.clientes().reservas(cliente.getId()).stream().filter(Reserva::ativa).toList();
        BigDecimal devendo = servicos.clientes().multasPendentes(cliente.getId());
        historico.setLinhas(locacoes);
        filas.setLinhas(servicos.clientes().reservas(cliente.getId()));
        CelulaTabela.ajustarLarguras(tabelaDeLocacoes);
        CelulaTabela.ajustarLarguras(tabelaDeReservas);
        abertas.setValor(String.valueOf(locacoes.stream().filter(Locacao::ativa).count()));
        multas.setValor(Formato.dinheiro(devendo));
        multas.setAlerta(devendo.signum() > 0);
        reservas.setValor(String.valueOf(ativas.size()));
        pagar.setEnabled(devendo.signum() > 0);
    }

    private JComponent topo() {
        JLabel nome = new JLabel(cliente.getNome());
        nome.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        String contato = "CPF " + cliente.cpfFormatado() + (cliente.getTelefone().isBlank() ? "" : " · " + cliente.getTelefone())
                + (cliente.getEmail().isBlank() ? "" : " · " + cliente.getEmail());
        JLabel dados = new JLabel(contato + (cliente.isAtivo() ? "" : " · INATIVO"));
        dados.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        JLabel endereco = new JLabel(cliente.getEndereco());
        endereco.putClientProperty(FlatClientProperties.STYLE, "foreground: $Locadora.suave");
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(nome);
        textos.add(dados);
        textos.add(endereco);
        JPanel cards = new JPanel(new GridLayout(1, 3, Tema.ESPACO_4, 0));
        cards.add(abertas);
        cards.add(multas);
        cards.add(reservas);
        JPanel topo = new JPanel(new BorderLayout(0, Tema.ESPACO_4));
        topo.add(textos, BorderLayout.NORTH);
        topo.add(cards, BorderLayout.CENTER);
        return topo;
    }

    private JComponent abas(LocalDate hoje) {
        CelulaTabela.aplicar(tabelaDeLocacoes, historico).destacar(l -> l.atrasada(hoje) || l.multaPendente());
        CelulaTabela.aplicar(tabelaDeReservas, filas);
        JTabbedPane abas = new JTabbedPane();
        abas.putClientProperty(FlatClientProperties.STYLE, "tabType: card");
        abas.addTab("Histórico de locações", Icone.TROCA.de(16, "Label.foreground"), new JScrollPane(tabelaDeLocacoes));
        abas.addTab("Reservas", Icone.MARCADOR.de(16, "Label.foreground"), new JScrollPane(tabelaDeReservas));
        return abas;
    }

    private JComponent rodape() {
        Fileira botoes = new Fileira(Tema.ESPACO_2, pagar);
        if (cliente.isAtivo())
            botoes.adicionar(Botoes.secundario("Nova locação", Icone.MAIS, this::novaLocacao));
        JButton fechar = Botoes.primario("Fechar", null, this::dispose);
        getRootPane().setDefaultButton(fechar);
        botoes.adicionar(fechar);
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(botoes, BorderLayout.EAST);
        return rodape;
    }

    private void pagarMultas() {
        BigDecimal devendo = servicos.clientes().multasPendentes(cliente.getId());
        String pergunta = "Registrar o pagamento de " + Formato.dinheiro(devendo) + " em multas de " + cliente.getNome() + "?";
        if (!Mensagens.confirmar(this, "Pagamento de multas", pergunta, "Registrar pagamento"))
            return;
        boolean pago = Mensagens.executar(this, () -> servicos.clientes().historico(cliente.getId()).stream()
                .filter(Locacao::multaPendente)
                .forEach(l -> servicos.locacoes().pagarMulta(l.getId())));
        carregar();
        if (pago)
            Toast.mostrar(this, "Pagamento registrado");
    }

    private void novaLocacao() {
        if (new NovaLocacaoDialog(this, servicos, cliente).abrir()) {
            carregar();
            Toast.mostrar(this, "Locação registrada");
        }
    }
}
