package locadora.dao;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Conexao {

    @FunctionalInterface
    interface Leitor<T> {
        T ler(ResultSet rs, Connection c) throws SQLException;
    }

    @FunctionalInterface
    interface Trabalho<T> {
        T executar(Connection c) throws SQLException;
    }

    public Conexao(Path arquivo) {
    }

    public Connection abrir() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean criarSchemaSeVazio() {
        throw new UnsupportedOperationException();
    }

    public void executarScript(String recurso) {
        throw new UnsupportedOperationException();
    }

    <T> List<T> listar(String sql, Leitor<T> leitor, Object... parametros) {
        throw new UnsupportedOperationException();
    }

    <T> T buscar(String sql, Leitor<T> leitor, Object... parametros) {
        throw new UnsupportedOperationException();
    }

    int inserir(String sql, Object... parametros) {
        throw new UnsupportedOperationException();
    }

    void executar(String sql, Object... parametros) {
        throw new UnsupportedOperationException();
    }

    <T> T transacao(Trabalho<T> trabalho) {
        throw new UnsupportedOperationException();
    }

    static <T> List<T> listar(Connection c, String sql, Leitor<T> leitor, Object... parametros) throws SQLException {
        throw new UnsupportedOperationException();
    }

    static <T> T buscar(Connection c, String sql, Leitor<T> leitor, Object... parametros) throws SQLException {
        throw new UnsupportedOperationException();
    }

    static int inserir(Connection c, String sql, Object... parametros) throws SQLException {
        throw new UnsupportedOperationException();
    }

    static void executar(Connection c, String sql, Object... parametros) throws SQLException {
        throw new UnsupportedOperationException();
    }

    static BigDecimal reais(long centavos) {
        throw new UnsupportedOperationException();
    }

    static LocalDate data(String texto) {
        throw new UnsupportedOperationException();
    }
}
