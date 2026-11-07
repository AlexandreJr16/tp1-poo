package locadora.dao;

import java.sql.SQLException;

/**
 * Erro de acesso ao banco, sem checagem obrigatoria, embrulhando a SQLException original.
 */
public class BancoException extends RuntimeException {

    public BancoException(SQLException causa) {
        super("Erro ao acessar o banco de dados.", causa);
    }
}
