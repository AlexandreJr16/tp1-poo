package locadora.service;

import locadora.dao.Conexao;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

public class ArquivoService {

    public ArquivoService(Conexao conexao, Clock relogio) {
    }

    public static String paraCsv(List<String> cabecalho, List<List<String>> linhas) {
        return "";
    }

    public void exportarCsv(Path destino, List<String> cabecalho, List<List<String>> linhas) {
        throw new UnsupportedOperationException();
    }

    public void fazerBackup(Path destino) {
        throw new UnsupportedOperationException();
    }

    public void restaurar(Path origem) {
        throw new UnsupportedOperationException();
    }

    public String nomeSugeridoDeBackup() {
        return "";
    }
}
