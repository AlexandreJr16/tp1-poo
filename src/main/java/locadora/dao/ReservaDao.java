package locadora.dao;

import locadora.model.Exemplar;
import locadora.model.Reserva;
import locadora.model.SituacaoReserva;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/* - Cadastro:
     inserir: grava uma reserva nova e preenche o id
     atualizar: grava a situacao e o exemplar separado
     registrarCancelamento: cancela, libera ou repassa o exemplar e prepara o proximo da fila, tudo junto
   - Consulta:
     buscar: reserva pelo id
     fila: reservas aguardando de um titulo, em ordem de chegada
     ativas: reservas aguardando ou prontas, em ordem de chegada
     doCliente: reservas de um cliente, da mais recente para a mais antiga
     prontaDoExemplar: reserva pronta que segura um exemplar, ou nulo */
public class ReservaDao {

    private static final String SELECIONAR = "SELECT id, titulo_id, cliente_id, funcionario_id, data, situacao, exemplar_id FROM reserva";

    private final Conexao conexao;
    private final TituloDao titulos;
    private final ClienteDao clientes;
    private final FuncionarioDao funcionarios;
    private final ExemplarDao exemplares;

    public ReservaDao(Conexao conexao, TituloDao titulos, ClienteDao clientes, FuncionarioDao funcionarios, ExemplarDao exemplares) {
        this.conexao = conexao;
        this.titulos = titulos;
        this.clientes = clientes;
        this.funcionarios = funcionarios;
        this.exemplares = exemplares;
    }

    public void inserir(Reserva reserva) {
        reserva.setId(conexao.inserir("INSERT INTO reserva (titulo_id, cliente_id, funcionario_id, data, situacao, exemplar_id) VALUES (?, ?, ?, ?, ?, ?)",
                reserva.getTitulo().getId(), reserva.getCliente().getId(), reserva.getFuncionario().getId(), reserva.getData(),
                reserva.getSituacao(), idDoExemplar(reserva)));
    }

    public void atualizar(Reserva reserva) {
        conexao.transacao(c -> {
            atualizar(c, reserva);
            return null;
        });
    }

    public void registrarCancelamento(Reserva cancelada, Reserva proxima) {
        conexao.transacao(c -> {
            atualizar(c, cancelada);
            if (cancelada.getExemplar() != null)
                exemplares.atualizarSituacao(c, cancelada.getExemplar());
            if (proxima != null)
                atualizar(c, proxima);
            return null;
        });
    }

    public Reserva buscar(int id) {
        return conexao.buscar(SELECIONAR + " WHERE id = ?", this::ler, id);
    }

    public List<Reserva> fila(int tituloId) {
        return conexao.listar(SELECIONAR + " WHERE titulo_id = ? AND situacao = ? ORDER BY data, id", this::ler,
                tituloId, SituacaoReserva.AGUARDANDO);
    }

    public List<Reserva> ativas() {
        return conexao.listar(SELECIONAR + " WHERE situacao IN (?, ?) ORDER BY data, id", this::ler,
                SituacaoReserva.AGUARDANDO, SituacaoReserva.PRONTA);
    }

    public List<Reserva> doCliente(int clienteId) {
        return conexao.listar(SELECIONAR + " WHERE cliente_id = ? ORDER BY data DESC, id DESC", this::ler, clienteId);
    }

    public Reserva prontaDoExemplar(int exemplarId) {
        return conexao.buscar(SELECIONAR + " WHERE exemplar_id = ? AND situacao = ?", this::ler, exemplarId, SituacaoReserva.PRONTA);
    }

    void atualizar(Connection c, Reserva reserva) throws SQLException {
        Conexao.executar(c, "UPDATE reserva SET situacao = ?, exemplar_id = ? WHERE id = ?",
                reserva.getSituacao(), idDoExemplar(reserva), reserva.getId());
    }

    private static Integer idDoExemplar(Reserva reserva) {
        return reserva.getExemplar() == null ? null : reserva.getExemplar().getId();
    }

    private Reserva ler(ResultSet rs, Connection c) throws SQLException {
        int exemplarId = rs.getInt("exemplar_id");
        Exemplar exemplar = rs.wasNull() ? null : exemplares.buscar(c, exemplarId);
        return new Reserva(rs.getInt("id"), titulos.buscar(c, rs.getInt("titulo_id")), clientes.buscar(c, rs.getInt("cliente_id")),
                funcionarios.buscar(c, rs.getInt("funcionario_id")), Conexao.data(rs.getString("data")),
                SituacaoReserva.valueOf(rs.getString("situacao")), exemplar);
    }
}
