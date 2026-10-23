package locadora.dao;

import locadora.model.Funcionario;
import locadora.model.Locacao;
import locadora.model.Reserva;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/* - Registro (cada um numa transacao so):
     registrar: grava a locacao, o exemplar alugado e a reserva atendida
     registrarDevolucao: grava a devolucao, a situacao do exemplar e a reserva que ficou pronta
     atualizar: grava prazo, devolucao, multa, pagamento e renovacao
   - Consulta:
     buscar: locacao pelo id
     listar: todas, da mais recente para a mais antiga
     ativas: nao devolvidas, pela data prevista
     doCliente: historico de um cliente, da mais recente para a mais antiga
     doExemplar: historico de um exemplar em ordem cronologica
     ativaDoExemplar: locacao em aberto de um exemplar, ou nulo
     comMultaPendente: devolvidas com multa ainda nao paga
     faturamentoEntre: locacoes feitas mais multas pagas no periodo */
public class LocacaoDao {

    private static final String SELECIONAR = """
            SELECT id, exemplar_id, cliente_id, funcionario_locacao_id, funcionario_devolucao_id, data_locacao, data_prevista,
                   data_devolucao, valor_centavos, multa_centavos, data_pagamento_multa, renovada
            FROM locacao""";

    private final Conexao conexao;
    private final ExemplarDao exemplares;
    private final ClienteDao clientes;
    private final FuncionarioDao funcionarios;
    private final ReservaDao reservas;

    public LocacaoDao(Conexao conexao, ExemplarDao exemplares, ClienteDao clientes, FuncionarioDao funcionarios, ReservaDao reservas) {
        this.conexao = conexao;
        this.exemplares = exemplares;
        this.clientes = clientes;
        this.funcionarios = funcionarios;
        this.reservas = reservas;
    }

    public void registrar(Locacao locacao, Reserva atendida) {
        conexao.transacao(c -> {
            locacao.setId(Conexao.inserir(c, """
                            INSERT INTO locacao (exemplar_id, cliente_id, funcionario_locacao_id, data_locacao, data_prevista, valor_centavos)
                            VALUES (?, ?, ?, ?, ?, ?)""",
                    locacao.getExemplar().getId(), locacao.getCliente().getId(), locacao.getFuncionarioLocacao().getId(),
                    locacao.getDataLocacao(), locacao.getDataPrevista(), locacao.getValorLocacao()));
            exemplares.atualizarSituacao(c, locacao.getExemplar());
            if (atendida != null)
                reservas.atualizar(c, atendida);
            return null;
        });
    }

    public void registrarDevolucao(Locacao locacao, Reserva preparada) {
        conexao.transacao(c -> {
            atualizar(c, locacao);
            exemplares.atualizarSituacao(c, locacao.getExemplar());
            if (preparada != null)
                reservas.atualizar(c, preparada);
            return null;
        });
    }

    public void atualizar(Locacao locacao) {
        conexao.transacao(c -> {
            atualizar(c, locacao);
            return null;
        });
    }

    public Locacao buscar(int id) {
        return conexao.transacao(c -> buscar(c, id));
    }

    public List<Locacao> listar() {
        return conexao.listar(SELECIONAR + " ORDER BY data_locacao DESC, id DESC", this::ler);
    }

    public List<Locacao> ativas() {
        return conexao.listar(SELECIONAR + " WHERE data_devolucao IS NULL ORDER BY data_prevista, id", this::ler);
    }

    public List<Locacao> doCliente(int clienteId) {
        return conexao.listar(SELECIONAR + " WHERE cliente_id = ? ORDER BY data_locacao DESC, id DESC", this::ler, clienteId);
    }

    public List<Locacao> doExemplar(int exemplarId) {
        return conexao.listar(SELECIONAR + " WHERE exemplar_id = ? ORDER BY data_locacao, id", this::ler, exemplarId);
    }

    public Locacao ativaDoExemplar(int exemplarId) {
        return conexao.buscar(SELECIONAR + " WHERE exemplar_id = ? AND data_devolucao IS NULL", this::ler, exemplarId);
    }

    public List<Locacao> comMultaPendente() {
        return conexao.listar(SELECIONAR + " WHERE multa_centavos > 0 AND data_pagamento_multa IS NULL ORDER BY data_devolucao, id", this::ler);
    }

    public BigDecimal faturamentoEntre(LocalDate inicio, LocalDate fim) {
        String sql = """
                SELECT (SELECT COALESCE(SUM(valor_centavos), 0) FROM locacao WHERE data_locacao BETWEEN ? AND ?)
                     + (SELECT COALESCE(SUM(multa_centavos), 0) FROM locacao WHERE data_pagamento_multa BETWEEN ? AND ?)""";
        return conexao.buscar(sql, (rs, c) -> Conexao.reais(rs.getLong(1)), inicio, fim, inicio, fim);
    }

    Locacao buscar(Connection c, int id) throws SQLException {
        return Conexao.buscar(c, SELECIONAR + " WHERE id = ?", this::ler, id);
    }

    void atualizar(Connection c, Locacao locacao) throws SQLException {
        Funcionario devolucao = locacao.getFuncionarioDevolucao();
        Conexao.executar(c, """
                        UPDATE locacao
                        SET data_prevista = ?, data_devolucao = ?, funcionario_devolucao_id = ?, multa_centavos = ?,
                            data_pagamento_multa = ?, renovada = ?
                        WHERE id = ?""",
                locacao.getDataPrevista(), locacao.getDataDevolucao(), devolucao == null ? null : devolucao.getId(), locacao.getMulta(),
                locacao.getDataPagamentoMulta(), locacao.isRenovada(), locacao.getId());
    }

    private Locacao ler(ResultSet rs, Connection c) throws SQLException {
        int devolucaoId = rs.getInt("funcionario_devolucao_id");
        Funcionario devolucao = rs.wasNull() ? null : funcionarios.buscar(c, devolucaoId);
        return new Locacao(rs.getInt("id"), exemplares.buscar(c, rs.getInt("exemplar_id")), clientes.buscar(c, rs.getInt("cliente_id")),
                funcionarios.buscar(c, rs.getInt("funcionario_locacao_id")), devolucao, Conexao.data(rs.getString("data_locacao")),
                Conexao.data(rs.getString("data_prevista")), Conexao.data(rs.getString("data_devolucao")),
                Conexao.reais(rs.getLong("valor_centavos")), Conexao.reais(rs.getLong("multa_centavos")),
                Conexao.data(rs.getString("data_pagamento_multa")), rs.getBoolean("renovada"));
    }
}
