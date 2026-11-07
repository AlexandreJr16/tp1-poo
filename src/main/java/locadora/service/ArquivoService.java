package locadora.service;

import locadora.dao.Conexao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Exportacao de tabelas para CSV e backup e restauracao do arquivo do banco.
 */
public class ArquivoService {

    private static final String SEPARADOR = ";";
    private static final String FIM_DE_LINHA = "\r\n";
    private static final String BOM = "\uFEFF";

    private final Conexao conexao;
    private final Clock relogio;

    public ArquivoService(Conexao conexao, Clock relogio) {
        this.conexao = conexao;
        this.relogio = relogio;
    }

    /**
     * Monta o texto CSV com separador ; e aspas quando preciso, como o Excel em portugues espera.
     */
    public static String paraCsv(List<String> cabecalho, List<List<String>> linhas) {
        StringBuilder csv = new StringBuilder();
        Stream.concat(Stream.of(cabecalho), linhas.stream())
                .forEach(linha -> csv.append(linha.stream().map(ArquivoService::campo).collect(Collectors.joining(SEPARADOR))).append(FIM_DE_LINHA));
        return csv.toString();
    }

    /**
     * Grava o CSV em UTF-8 com BOM para os acentos aparecerem certos.
     */
    public void exportarCsv(Path destino, List<String> cabecalho, List<List<String>> linhas) {
        AuthService.exigirLogado();
        try {
            Files.writeString(destino, BOM + paraCsv(cabecalho, linhas), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RegraException("Não foi possível gravar o arquivo " + destino.getFileName() + ".");
        }
    }

    /**
     * Copia o banco inteiro para o arquivo escolhido.
     */
    public void fazerBackup(Path destino) {
        AuthService.exigirLogado();
        conexao.copiarPara(destino);
    }

    /**
     * Troca o banco atual por um backup, depois de conferir que ele e da locadora (so gerente).
     */
    public void restaurar(Path origem) {
        AuthService.exigirGerente();
        RegraException.se(!Conexao.ehBancoDaLocadora(origem), "O arquivo escolhido não é um backup da locadora.");
        conexao.restaurarDe(origem);
    }

    /**
     * Nome de arquivo com a data de hoje.
     */
    public String nomeSugeridoDeBackup() {
        return "locadora-backup-" + LocalDate.now(relogio) + ".db";
    }

    private static String campo(String valor) {
        String texto = valor == null ? "" : valor;
        boolean precisaDeAspas = texto.contains(SEPARADOR) || texto.contains("\"") || texto.contains("\n");
        return precisaDeAspas ? "\"" + texto.replace("\"", "\"\"") + "\"" : texto;
    }
}
