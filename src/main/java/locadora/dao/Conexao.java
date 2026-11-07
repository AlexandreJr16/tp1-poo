package locadora.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Conexao com o banco SQLite da locadora: cria o schema, roda scripts, faz backup e restauracao
 * e oferece atalhos de PreparedStatement e transacao para os DAOs.
 */
public class Conexao {

    @FunctionalInterface
    interface Leitor<T> {
        T ler(ResultSet rs, Connection c) throws SQLException;
    }

    @FunctionalInterface
    interface Trabalho<T> {
        T executar(Connection c) throws SQLException;
    }

    private static final List<String> TABELAS = List.of("titulo", "exemplar", "cliente", "funcionario", "aquisicao", "locacao", "reserva", "perda");

    private final Path arquivo;
    private final String url;

    public Conexao(Path arquivo) {
        this.arquivo = arquivo.toAbsolutePath();
        url = "jdbc:sqlite:" + this.arquivo;
    }

    /**
     * Grava uma copia do banco inteiro em outro arquivo (backup).
     */
    // VACUUM nao roda dentro de transacao, por isso usa uma conexao em autocommit
    public void copiarPara(Path destino) {
        try (Connection c = abrir()) {
            Files.deleteIfExists(destino);
            executar(c, "VACUUM INTO ?", destino.toAbsolutePath().toString());
        } catch (SQLException e) {
            throw new BancoException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Troca o arquivo do banco por uma copia.
     */
    public void restaurarDe(Path origem) {
        try {
            Files.copy(origem, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Confere se um arquivo e um banco SQLite com as tabelas da locadora.
     */
    public static boolean ehBancoDaLocadora(Path candidato) {
        if (!Files.isRegularFile(candidato))
            return false;
        try {
            List<String> tabelas = new Conexao(candidato).listar("SELECT name FROM sqlite_master WHERE type = 'table'", (rs, c) -> rs.getString(1));
            return tabelas.containsAll(TABELAS);
        } catch (BancoException e) {
            return false;
        }
    }

    private Connection abrir() throws SQLException {
        Connection conexao = DriverManager.getConnection(url);
        try {
            executar(conexao, "PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            conexao.close();
            throw e;
        }
        return conexao;
    }

    /**
     * Cria as tabelas do schema.sql quando o arquivo ainda nao tem tabelas.
     */
    public boolean criarSchemaSeVazio() {
        boolean vazio = buscar("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table'", (rs, c) -> rs.getInt(1)) == 0;
        if (vazio)
            executarScript("/schema.sql");
        return vazio;
    }

    /**
     * Roda um arquivo .sql do classpath inteiro numa transacao.
     */
    public void executarScript(String recurso) {
        String script = lerRecurso(recurso).replaceAll("(?m)^\\s*--.*$", "");
        transacao(c -> {
            for (String sql : script.split(";\\s*(\\r?\\n|$)")) {
                if (!sql.isBlank())
                    executar(c, sql);
            }
            return null;
        });
    }

    /**
     * Roda um PreparedStatement com os parametros na ordem e le cada linha do resultado.
     * Abre e fecha a propria conexao.
     */
    <T> List<T> listar(String sql, Leitor<T> leitor, Object... parametros) {
        return transacao(c -> listar(c, sql, leitor, parametros));
    }

    /**
     * Roda um PreparedStatement com os parametros na ordem.
     */
    <T> T buscar(String sql, Leitor<T> leitor, Object... parametros) {
        return transacao(c -> buscar(c, sql, leitor, parametros));
    }

    /**
     * Roda um PreparedStatement com os parametros na ordem.
     */
    int inserir(String sql, Object... parametros) {
        return transacao(c -> inserir(c, sql, parametros));
    }

    /**
     * Roda um PreparedStatement com os parametros na ordem.
     */
    void executar(String sql, Object... parametros) {
        transacao(c -> {
            executar(c, sql, parametros);
            return null;
        });
    }

    /**
     * Roda varios comandos na mesma conexao, com commit no fim ou rollback no erro.
     */
    <T> T transacao(Trabalho<T> trabalho) {
        try (Connection c = abrir()) {
            c.setAutoCommit(false);
            try {
                T resultado = trabalho.executar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BancoException(e);
        }
    }

    /**
     * Igual ao metodo de mesmo nome, mas dentro de uma transacao (recebe a conexao aberta).
     */
    static <T> List<T> listar(Connection c, String sql, Leitor<T> leitor, Object... parametros) throws SQLException {
        try (PreparedStatement comando = c.prepareStatement(sql)) {
            preencher(comando, parametros);
            try (ResultSet rs = comando.executeQuery()) {
                List<T> lista = new ArrayList<>();
                while (rs.next())
                    lista.add(leitor.ler(rs, c));
                return lista;
            }
        }
    }

    /**
     * Igual ao metodo de mesmo nome, mas dentro de uma transacao (recebe a conexao aberta).
     */
    static <T> T buscar(Connection c, String sql, Leitor<T> leitor, Object... parametros) throws SQLException {
        List<T> lista = listar(c, sql, leitor, parametros);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Igual ao metodo de mesmo nome, mas dentro de uma transacao (recebe a conexao aberta).
     */
    static int inserir(Connection c, String sql, Object... parametros) throws SQLException {
        executar(c, sql, parametros);
        return buscar(c, "SELECT last_insert_rowid()", (rs, mesma) -> rs.getInt(1));
    }

    /**
     * Igual ao metodo de mesmo nome, mas dentro de uma transacao (recebe a conexao aberta).
     */
    static void executar(Connection c, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement comando = c.prepareStatement(sql)) {
            preencher(comando, parametros);
            comando.executeUpdate();
        }
    }

    /**
     * Centavos do banco para BigDecimal.
     */
    static BigDecimal reais(long centavos) {
        return BigDecimal.valueOf(centavos, 2);
    }

    /**
     * Texto ISO do banco para LocalDate.
     */
    static LocalDate data(String texto) {
        return texto == null ? null : LocalDate.parse(texto);
    }

    private static void preencher(PreparedStatement comando, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++)
            comando.setObject(i + 1, paraBanco(parametros[i]));
    }

    /**
     * Dinheiro vira centavos, data vira texto ISO, enum vira o nome e booleano vira 0 ou 1.
     */
    private static Object paraBanco(Object valor) {
        if (valor instanceof BigDecimal dinheiro)
            return dinheiro.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        if (valor instanceof LocalDate data)
            return data.toString();
        if (valor instanceof Enum<?> constante)
            return constante.name();
        if (valor instanceof Boolean verdade)
            return verdade ? 1 : 0;
        return valor;
    }

    private static String lerRecurso(String recurso) {
        try (InputStream entrada = Conexao.class.getResourceAsStream(recurso)) {
            if (entrada == null)
                throw new IllegalArgumentException("Script nao encontrado: " + recurso);
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
