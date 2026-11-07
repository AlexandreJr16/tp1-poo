package locadora.dao;

import locadora.model.Estoque;
import locadora.model.Exemplar;
import locadora.model.Situacao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Acesso a tabela de exemplares e a contagem de exemplares por situacao.
 */
public class ExemplarDao {

    private static final String SELECIONAR = "SELECT e.id, e.codigo, e.titulo_id, e.custo_centavos, e.situacao FROM exemplar e";

    private final Conexao conexao;
    private final TituloDao titulos;

    public ExemplarDao(Conexao conexao, TituloDao titulos) {
        this.conexao = conexao;
        this.titulos = titulos;
    }

    /**
     * Exemplar pelo id, com o titulo.
     */
    public Exemplar buscar(int id) {
        return conexao.transacao(c -> buscar(c, id));
    }

    /**
     * Exemplares de um titulo na ordem em que foram adquiridos.
     */
    public List<Exemplar> listarPorTitulo(int tituloId) {
        return conexao.listar(SELECIONAR + " WHERE e.titulo_id = ? ORDER BY e.id", this::ler, tituloId);
    }

    /**
     * Exemplares cujo codigo comeca com o texto dado.
     */
    public List<Exemplar> listarPorCodigo(String inicio) {
        return conexao.listar(SELECIONAR + " WHERE e.codigo LIKE ? ORDER BY e.id", this::ler, inicio + "%");
    }

    /**
     * Exemplares numa situacao, em ordem de titulo.
     */
    public List<Exemplar> listarPorSituacao(Situacao situacao) {
        return conexao.listar(SELECIONAR + " JOIN titulo t ON t.id = e.titulo_id WHERE e.situacao = ? ORDER BY t.nome COLLATE NOCASE, e.id",
                this::ler, situacao);
    }

    /**
     * Contagem por situacao de cada titulo que tem exemplares.
     */
    public Map<Integer, Estoque> estoquePorTitulo() {
        String sql = """
                SELECT titulo_id,
                       SUM(situacao = 'DISPONIVEL') AS disponiveis,
                       SUM(situacao = 'ALUGADO')    AS alugados,
                       SUM(situacao = 'RESERVADO')  AS reservados,
                       SUM(situacao = 'PERDIDO')    AS perdidos
                FROM exemplar
                GROUP BY titulo_id""";
        return conexao.listar(sql, (rs, c) -> Map.entry(rs.getInt("titulo_id"),
                        new Estoque(rs.getInt("disponiveis"), rs.getInt("alugados"), rs.getInt("reservados"), rs.getInt("perdidos"))))
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Numero do proximo exemplar do titulo.
     */
    public int proximaSequencia(int tituloId) {
        return conexao.buscar("SELECT COUNT(*) + 1 FROM exemplar WHERE titulo_id = ?", (rs, c) -> rs.getInt(1), tituloId);
    }

    /**
     * Grava a situacao atual do exemplar.
     */
    public void atualizarSituacao(Exemplar exemplar) {
        conexao.transacao(c -> {
            atualizarSituacao(c, exemplar);
            return null;
        });
    }

    /**
     * Exemplar pelo id, com o titulo.
     */
    Exemplar buscar(Connection c, int id) throws SQLException {
        return Conexao.buscar(c, SELECIONAR + " WHERE e.id = ?", this::ler, id);
    }

    void inserir(Connection c, Exemplar exemplar, int aquisicaoId) throws SQLException {
        exemplar.setId(Conexao.inserir(c, "INSERT INTO exemplar (codigo, titulo_id, aquisicao_id, custo_centavos, situacao) VALUES (?, ?, ?, ?, ?)",
                exemplar.getCodigo(), exemplar.getTitulo().getId(), aquisicaoId, exemplar.getCusto(), exemplar.getSituacao()));
    }

    /**
     * Grava a situacao atual do exemplar.
     */
    void atualizarSituacao(Connection c, Exemplar exemplar) throws SQLException {
        Conexao.executar(c, "UPDATE exemplar SET situacao = ? WHERE id = ?", exemplar.getSituacao(), exemplar.getId());
    }

    private Exemplar ler(ResultSet rs, Connection c) throws SQLException {
        return new Exemplar(rs.getInt("id"), rs.getString("codigo"), titulos.buscar(c, rs.getInt("titulo_id")),
                Conexao.reais(rs.getLong("custo_centavos")), Situacao.valueOf(rs.getString("situacao")));
    }
}
