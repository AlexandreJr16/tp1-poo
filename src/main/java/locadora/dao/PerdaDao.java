package locadora.dao;

import locadora.model.Locacao;
import locadora.model.Perda;
import locadora.model.Reserva;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/* - Registro:
     registrar: grava a perda, o exemplar perdido, a locacao encerrada e a reserva devolvida a fila, tudo junto
   - Consulta:
     listar: historico de perdas, da mais recente para a mais antiga
     doExemplar: perda de um exemplar, ou nulo */
public class PerdaDao {

    private static final String SELECIONAR = "SELECT id, exemplar_id, motivo, data, funcionario_id, locacao_id FROM perda";

    private final Conexao conexao;
    private final ExemplarDao exemplares;
    private final FuncionarioDao funcionarios;
    private final LocacaoDao locacoes;
    private final ReservaDao reservas;

    public PerdaDao(Conexao conexao, ExemplarDao exemplares, FuncionarioDao funcionarios, LocacaoDao locacoes, ReservaDao reservas) {
        this.conexao = conexao;
        this.exemplares = exemplares;
        this.funcionarios = funcionarios;
        this.locacoes = locacoes;
        this.reservas = reservas;
    }

    public void registrar(Perda perda, Reserva devolvidaAFila) {
        Locacao locacao = perda.getLocacao();
        conexao.transacao(c -> {
            perda.setId(Conexao.inserir(c, "INSERT INTO perda (exemplar_id, motivo, data, funcionario_id, locacao_id) VALUES (?, ?, ?, ?, ?)",
                    perda.getExemplar().getId(), perda.getMotivo(), perda.getData(), perda.getFuncionario().getId(),
                    locacao == null ? null : locacao.getId()));
            exemplares.atualizarSituacao(c, perda.getExemplar());
            if (locacao != null)
                locacoes.atualizar(c, locacao);
            if (devolvidaAFila != null)
                reservas.atualizar(c, devolvidaAFila);
            return null;
        });
    }

    public List<Perda> listar() {
        return conexao.listar(SELECIONAR + " ORDER BY data DESC, id DESC", this::ler);
    }

    public Perda doExemplar(int exemplarId) {
        return conexao.buscar(SELECIONAR + " WHERE exemplar_id = ?", this::ler, exemplarId);
    }

    private Perda ler(ResultSet rs, Connection c) throws SQLException {
        int locacaoId = rs.getInt("locacao_id");
        Locacao locacao = rs.wasNull() ? null : locacoes.buscar(c, locacaoId);
        return new Perda(rs.getInt("id"), exemplares.buscar(c, rs.getInt("exemplar_id")), rs.getString("motivo"),
                Conexao.data(rs.getString("data")), funcionarios.buscar(c, rs.getInt("funcionario_id")), locacao);
    }
}
