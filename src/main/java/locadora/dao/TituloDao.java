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

/* - Cadastro:
     inserir: grava um titulo novo e preenche o id
     atualizar: grava tipo, nome, descricao, ano e prazo
   - Consulta:
     buscar: titulo pelo id, ja na subclasse certa
     listar: todos os titulos em ordem de nome
   - Heranca:
     instanciar: escolhe a subclasse pela coluna tipo */
public class TituloDao {

    private static final String SELECIONAR = "SELECT id, tipo, nome, descricao, ano, prazo_maximo_dias FROM titulo";

    private final Conexao conexao;

    public TituloDao(Conexao conexao) {
        this.conexao = conexao;
    }

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

    public void inserir(Titulo titulo) {
        titulo.setId(conexao.inserir("INSERT INTO titulo (tipo, nome, descricao, ano, prazo_maximo_dias) VALUES (?, ?, ?, ?, ?)",
                titulo.tipo(), titulo.getNome(), titulo.getDescricao(), titulo.getAno(), titulo.getPrazoMaximoDias()));
    }

    public void atualizar(Titulo titulo) {
        conexao.executar("UPDATE titulo SET tipo = ?, nome = ?, descricao = ?, ano = ?, prazo_maximo_dias = ? WHERE id = ?",
                titulo.tipo(), titulo.getNome(), titulo.getDescricao(), titulo.getAno(), titulo.getPrazoMaximoDias(), titulo.getId());
    }

    public Titulo buscar(int id) {
        return conexao.transacao(c -> buscar(c, id));
    }

    public List<Titulo> listar() {
        return conexao.listar(SELECIONAR + " ORDER BY nome COLLATE NOCASE", TituloDao::ler);
    }

    public void salvarCapa(int tituloId, byte[] imagem) {
    }

    public byte[] capa(int tituloId) {
        return new byte[0];
    }

    public Map<Integer, byte[]> capas() {
        return Map.of();
    }

    Titulo buscar(Connection c, int id) throws SQLException {
        return Conexao.buscar(c, SELECIONAR + " WHERE id = ?", TituloDao::ler, id);
    }

    private static Titulo ler(ResultSet rs, Connection c) throws SQLException {
        return instanciar(rs.getString("tipo"), rs.getInt("id"), rs.getString("nome"), rs.getString("descricao"),
                rs.getInt("ano"), rs.getInt("prazo_maximo_dias"));
    }
}
