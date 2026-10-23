package locadora.dao;

import java.sql.SQLException;

public class BancoException extends RuntimeException {

    public BancoException(SQLException causa) {
        super("Erro ao acessar o banco de dados.", causa);
    }
}
