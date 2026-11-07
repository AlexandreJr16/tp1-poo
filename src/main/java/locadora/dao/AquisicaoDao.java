package locadora.dao;

import locadora.model.Aquisicao;
import locadora.model.Exemplar;
import locadora.model.Reserva;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Acesso a tabela de aquisicoes. Gravar uma compra grava junto os exemplares novos e as reservas que eles atenderam.
 */
public class AquisicaoDao {

    private static final String SELECIONAR =
            "SELECT a.id, a.titulo_id, a.quantidade, a.custo_unitario_centavos, a.fornecedor, a.data, a.funcionario_id FROM aquisicao a";

    private final Conexao conexao;
    private final TituloDao titulos;
    private final FuncionarioDao funcionarios;
    private final ExemplarDao exemplares;
    private final ReservaDao reservas;

    public AquisicaoDao(Conexao conexao, TituloDao titulos, FuncionarioDao funcionarios, ExemplarDao exemplares, ReservaDao reservas) {
        this.conexao = conexao;
        this.titulos = titulos;
        this.funcionarios = funcionarios;
        this.exemplares = exemplares;
        this.reservas = reservas;
    }

    /**
     * Grava a aquisicao, os exemplares novos e as reservas que eles atenderam, tudo junto.
     */
    public void registrar(Aquisicao aquisicao, List<Exemplar> novos, List<Reserva> preparadas) {
        conexao.transacao(c -> {
            aquisicao.setId(Conexao.inserir(c,
                    "INSERT INTO aquisicao (titulo_id, quantidade, custo_unitario_centavos, fornecedor, data, funcionario_id) VALUES (?, ?, ?, ?, ?, ?)",
                    aquisicao.getTitulo().getId(), aquisicao.getQuantidade(), aquisicao.getCustoUnitario(), aquisicao.getFornecedor(),
                    aquisicao.getData(), aquisicao.getFuncionario().getId()));
            for (Exemplar exemplar : novos)
                exemplares.inserir(c, exemplar, aquisicao.getId());
            for (Reserva reserva : preparadas)
                reservas.atualizar(c, reserva);
            return null;
        });
    }

    /**
     * Historico de aquisicoes, da mais recente para a mais antiga.
     */
    public List<Aquisicao> listar() {
        return conexao.listar(SELECIONAR + " ORDER BY a.data DESC, a.id DESC", this::ler);
    }

    /**
     * Aquisicao que trouxe um exemplar.
     */
    public Aquisicao buscarDoExemplar(int exemplarId) {
        return conexao.buscar(SELECIONAR + " JOIN exemplar e ON e.aquisicao_id = a.id WHERE e.id = ?", this::ler, exemplarId);
    }

    private Aquisicao ler(ResultSet rs, Connection c) throws SQLException {
        return new Aquisicao(rs.getInt("id"), titulos.buscar(c, rs.getInt("titulo_id")), rs.getInt("quantidade"),
                Conexao.reais(rs.getLong("custo_unitario_centavos")), rs.getString("fornecedor"), Conexao.data(rs.getString("data")),
                funcionarios.buscar(c, rs.getInt("funcionario_id")));
    }
}
