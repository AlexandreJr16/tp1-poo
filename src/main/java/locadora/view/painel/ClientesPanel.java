package locadora.view.painel;

import locadora.Main;
import locadora.model.Cliente;
import locadora.view.Icone;
import locadora.view.componente.Botoes;
import locadora.view.componente.Cabecalho;
import locadora.view.componente.EstadoVazio;
import locadora.view.componente.TabelaBusca;
import locadora.view.componente.Toast;
import locadora.view.dialogo.ClienteDetalheDialog;
import locadora.view.dialogo.ClienteDialog;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static locadora.view.componente.ModeloTabela.dinheiro;
import static locadora.view.componente.ModeloTabela.texto;

/* Tela de clientes: tabela com busca por nome ou CPF, filtro por situacao, cadastro, edicao e ficha com historico.
   Clientes com multa pendente aparecem em vermelho.
   atualizar: recarrega clientes e multas
   abrir: seleciona um cliente e abre a ficha (usado pela busca global) */
public class ClientesPanel extends Painel {

    private static final String[] FILTROS = {"Todos os clientes", "Ativos", "Inativos", "Com multa pendente"};

    private final Main.Servicos servicos;
    private final TabelaBusca<Cliente> tabela;
    private final JComboBox<String> filtro = new JComboBox<>(FILTROS);
    private final JButton editar = Botoes.secundario("Editar", Icone.LAPIS, this::editar);
    private final JButton ficha = Botoes.secundario("Ficha", Icone.LISTA, this::abrirSelecionado);
    private Map<Integer, BigDecimal> multas = Map.of();

    public ClientesPanel(Main.Servicos servicos) {
        this.servicos = servicos;
        tabela = new TabelaBusca<>(List.of(
                texto("Nome", Cliente::getNome),
                texto("CPF", Cliente::cpfFormatado),
                texto("Telefone", Cliente::getTelefone),
                texto("E-mail", Cliente::getEmail),
                texto("Situação", (Cliente c) -> c.isAtivo() ? "Ativo" : "Inativo"),
                dinheiro("Multas pendentes", this::multaDe)),
                "Buscar por nome, CPF, telefone ou e-mail",
                new EstadoVazio(Icone.PESSOA, "Nenhum cliente cadastrado", "Só clientes cadastrados podem alugar.",
                        Botoes.primario("Novo cliente", Icone.MAIS, this::novo)));
        filtro.addActionListener(e -> tabela.filtrar(this::passaNoFiltro));
        tabela.adicionarNaBarra(filtro);
        tabela.destacar(c -> multaDe(c).signum() > 0);
        tabela.permitirExportar(servicos.arquivos(), "clientes");
        tabela.aoAbrir(this::abrirFicha);
        tabela.aoSelecionar(c -> {
            editar.setEnabled(c != null);
            ficha.setEnabled(c != null);
        });
        editar.setEnabled(false);
        ficha.setEnabled(false);

        add(new Cabecalho("Clientes", "Quem pode alugar, histórico e multas", ficha, editar,
                Botoes.primario("Novo cliente", Icone.MAIS, this::novo)), BorderLayout.NORTH);
        add(tabela, BorderLayout.CENTER);
    }

    @Override
    public void atualizar() {
        List<Cliente> clientes = servicos.clientes().listar();
        multas = clientes.stream().collect(Collectors.toMap(Cliente::getId, c -> servicos.clientes().multasPendentes(c.getId())));
        tabela.setDados(clientes);
    }

    public void abrir(int clienteId) {
        tabela.selecionar(c -> c.getId() == clienteId);
        abrirSelecionado();
    }

    private BigDecimal multaDe(Cliente cliente) {
        return multas.getOrDefault(cliente.getId(), BigDecimal.ZERO);
    }

    private boolean passaNoFiltro(Cliente cliente) {
        return switch (filtro.getSelectedIndex()) {
            case 1 -> cliente.isAtivo();
            case 2 -> !cliente.isAtivo();
            case 3 -> multaDe(cliente).signum() > 0;
            default -> true;
        };
    }

    private void novo() {
        abrirFormulario(null, "Cliente cadastrado");
    }

    private void editar() {
        abrirFormulario(tabela.selecionado(), "Cliente atualizado");
    }

    private void abrirFormulario(Cliente cliente, String aviso) {
        if (new ClienteDialog(this, servicos.clientes(), cliente).abrir()) {
            atualizar();
            Toast.mostrar(this, aviso);
        }
    }

    private void abrirSelecionado() {
        if (tabela.selecionado() != null)
            abrirFicha(tabela.selecionado());
    }

    private void abrirFicha(Cliente cliente) {
        new ClienteDetalheDialog(this, servicos, cliente).setVisible(true);
        atualizar();
    }
}
