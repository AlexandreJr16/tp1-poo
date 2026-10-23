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
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/* - Banco:
     abrir: abre uma conexao com as chaves estrangeiras ligadas
     criarSchemaSeVazio: cria as tabelas do schema.sql quando o arquivo ainda nao tem tabelas
     executarScript: roda um arquivo .sql do classpath inteiro numa transacao
     copiarPara: grava uma copia do banco inteiro em outro arquivo (backup)
     restaurarDe: troca o arquivo do banco por uma copia
     ehBancoDaLocadora: confere se um arquivo e um banco SQLite com as tabelas da locadora
   - Atalhos dos DAOs (abrem e fecham a propria conexao):
     listar, buscar, inserir, executar: rodam um PreparedStatement com os parametros na ordem
     transacao: roda varios comandos na mesma conexao, com commit no fim ou rollback no erro
   - Atalhos dentro de uma transacao (recebem a conexao aberta):
     listar, buscar, inserir, executar
   - Conversao:
     reais: centavos do banco para BigDecimal
     data: texto ISO do banco para LocalDate */
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

    public void restaurarDe(Path origem) {
        try {
            Files.copy(origem, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

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

    public Connection abrir() throws SQLException {
        Connection conexao = DriverManager.getConnection(url);
        try (Statement comando = conexao.createStatement()) {
            comando.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            conexao.close();
            throw e;
        }
        return conexao;
    }

    public boolean criarSchemaSeVazio() {
        boolean vazio = buscar("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table'", (rs, c) -> rs.getInt(1)) == 0;
        if (vazio)
            executarScript("/schema.sql");
        return vazio;
    }

    public void executarScript(String recurso) {
        String script = lerRecurso(recurso).replaceAll("(?m)^\\s*--.*$", "");
        transacao(c -> {
            try (Statement comando = c.createStatement()) {
                for (String sql : script.split(";\\s*(\\r?\\n|$)")) {
                    if (!sql.isBlank())
                        comando.executeUpdate(sql);
                }
            }
            return null;
        });
    }

    <T> List<T> listar(String sql, Leitor<T> leitor, Object... parametros) {
        return transacao(c -> listar(c, sql, leitor, parametros));
    }

    <T> T buscar(String sql, Leitor<T> leitor, Object... parametros) {
        return transacao(c -> buscar(c, sql, leitor, parametros));
    }

    int inserir(String sql, Object... parametros) {
        return transacao(c -> inserir(c, sql, parametros));
    }

    void executar(String sql, Object... parametros) {
        transacao(c -> {
            executar(c, sql, parametros);
            return null;
        });
    }

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

    static <T> List<T> listar(Connection c, String sql, Leitor<T> leitor, Object... parametros) throws SQLException {
        try (PreparedStatement comando = preparar(c, sql, parametros); ResultSet rs = comando.executeQuery()) {
            List<T> lista = new ArrayList<>();
            while (rs.next())
                lista.add(leitor.ler(rs, c));
            return lista;
        }
    }

    static <T> T buscar(Connection c, String sql, Leitor<T> leitor, Object... parametros) throws SQLException {
        List<T> lista = listar(c, sql, leitor, parametros);
        return lista.isEmpty() ? null : lista.get(0);
    }

    static int inserir(Connection c, String sql, Object... parametros) throws SQLException {
        executar(c, sql, parametros);
        try (Statement comando = c.createStatement(); ResultSet chave = comando.executeQuery("SELECT last_insert_rowid()")) {
            return chave.getInt(1);
        }
    }

    static void executar(Connection c, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement comando = preparar(c, sql, parametros)) {
            comando.executeUpdate();
        }
    }

    static BigDecimal reais(long centavos) {
        return BigDecimal.valueOf(centavos, 2);
    }

    static LocalDate data(String texto) {
        return texto == null ? null : LocalDate.parse(texto);
    }

    private static PreparedStatement preparar(Connection c, String sql, Object... parametros) throws SQLException {
        PreparedStatement comando = c.prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++)
            comando.setObject(i + 1, paraBanco(parametros[i]));
        return comando;
    }

    // dinheiro vira centavos, data vira texto ISO, enum vira o nome e booleano vira 0 ou 1
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
