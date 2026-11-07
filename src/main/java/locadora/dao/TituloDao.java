package locadora.dao;

import locadora.model.BluRay;
import locadora.model.Cd;
import locadora.model.Dvd;
import locadora.model.FitaVideo;
import locadora.model.Titulo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Acesso a tabela de titulos. E o unico lugar que escolhe a subclasse de Titulo pela coluna tipo.
 */
public class TituloDao {

    private static final String SELECIONAR = "SELECT id, tipo, nome, descricao, ano, prazo_maximo_dias FROM titulo";

    private final Conexao conexao;

    public TituloDao(Conexao conexao) {
        this.conexao = conexao;
    }

    /**
     * Escolhe a subclasse pela coluna tipo.
     */
    // unico ponto do projeto em que o tipo de midia vira switch
    public static Titulo instanciar(String tipo, int id, String nome, String descricao, int ano, int prazoMaximoDias) {
        return switch (tipo) {
            case BluRay.TIPO -> new BluRay(id, nome, descricao, ano, prazoMaximoDias);
            case Dvd.TIPO -> new Dvd(id, nome, descricao, ano, prazoMaximoDias);
            case Cd.TIPO -> new Cd(id, nome, descricao, ano, prazoMaximoDias);
            case FitaVideo.TIPO -> new FitaVideo(id, nome, descricao, ano, prazoMaximoDias);
            default -> throw new IllegalArgumentException("Tipo de midia desconhecido: " + tipo);
        };
    }

    /**
     * Grava um titulo novo e preenche o id.
     */
    public void inserir(Titulo titulo) {
        titulo.setId(conexao.inserir("INSERT INTO titulo (tipo, nome, descricao, ano, prazo_maximo_dias) VALUES (?, ?, ?, ?, ?)",
                titulo.tipo(), titulo.getNome(), titulo.getDescricao(), titulo.getAno(), titulo.getPrazoMaximoDias()));
    }

    /**
     * Grava tipo, nome, descricao, ano e prazo.
     */
    public void atualizar(Titulo titulo) {
        conexao.executar("UPDATE titulo SET tipo = ?, nome = ?, descricao = ?, ano = ?, prazo_maximo_dias = ? WHERE id = ?",
                titulo.tipo(), titulo.getNome(), titulo.getDescricao(), titulo.getAno(), titulo.getPrazoMaximoDias(), titulo.getId());
    }

    /**
     * Titulo pelo id, ja na subclasse certa.
     */
    public Titulo buscar(int id) {
        return conexao.transacao(c -> buscar(c, id));
    }

    /**
     * Todos os titulos em ordem de nome.
     */
    public List<Titulo> listar() {
        return conexao.listar(SELECIONAR + " ORDER BY nome COLLATE NOCASE", TituloDao::ler);
    }

    /**
     * Grava a imagem da capa do titulo.
     */
    public void salvarCapa(int tituloId, byte[] imagem) {
        conexao.executar("UPDATE titulo SET capa = ? WHERE id = ?", imagem, tituloId);
    }

    /**
     * Imagem da capa, ou nulo se o titulo nao tem.
     */
    public byte[] capa(int tituloId) {
        return conexao.buscar("SELECT capa FROM titulo WHERE id = ?", (rs, c) -> rs.getBytes("capa"), tituloId);
    }

    /**
     * Imagens de todos os titulos que tem capa, pelo id.
     */
    public Map<Integer, byte[]> capas() {
        return conexao.listar("SELECT id, capa FROM titulo WHERE capa IS NOT NULL", (rs, c) -> Map.entry(rs.getInt("id"), rs.getBytes("capa")))
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Titulo pelo id, ja na subclasse certa.
     */
    Titulo buscar(Connection c, int id) throws SQLException {
        return Conexao.buscar(c, SELECIONAR + " WHERE id = ?", TituloDao::ler, id);
    }

    private static Titulo ler(ResultSet rs, Connection c) throws SQLException {
        return instanciar(rs.getString("tipo"), rs.getInt("id"), rs.getString("nome"), rs.getString("descricao"),
                rs.getInt("ano"), rs.getInt("prazo_maximo_dias"));
    }
}
