package locadora.service;

import locadora.BancoTeste;
import locadora.model.Cliente;
import locadora.model.Pessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static locadora.BancoTeste.assertRegra;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArquivoServiceTest {

    @TempDir
    Path dir;

    private BancoTeste banco;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
        banco.logarGerente();
    }

    private List<String> nomesDosClientes() {
        return banco.clienteService.listar().stream().map(Pessoa::getNome).toList();
    }

    @Test
    void csvSeparaPorPontoEVirgulaEEscapaAspas() {
        String csv = ArquivoService.paraCsv(List.of("Nome", "Obs"), List.of(List.of("Ana", "diz \"oi\"; tchau"), List.of("Bruno", "")));

        assertEquals("Nome;Obs\r\nAna;\"diz \"\"oi\"\"; tchau\"\r\nBruno;\r\n", csv);
    }

    @Test
    void exportarCsvGravaComBomParaOExcelAbrirOsAcentos() throws IOException {
        Path arquivo = dir.resolve("titulos.csv");

        banco.arquivoService.exportarCsv(arquivo, List.of("Título"), List.of(List.of("Ação")));

        String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
        assertTrue(conteudo.startsWith("\uFEFFTítulo\r\n"));
        assertTrue(conteudo.contains("Ação"));
    }

    @Test
    void backupGeraUmaCopiaQueARestauracaoTrazDeVolta() {
        banco.clienteService.salvar(new Cliente(0, "Ana", banco.cpf(), "", "", "", true));
        Path backup = dir.resolve("copia.db");

        banco.arquivoService.fazerBackup(backup);
        banco.arquivoService.fazerBackup(backup);
        banco.clienteService.salvar(new Cliente(0, "Bruno", banco.cpf(), "", "", "", true));
        assertEquals(List.of("Ana", "Bruno"), nomesDosClientes());

        banco.arquivoService.restaurar(backup);

        assertEquals(List.of("Ana"), nomesDosClientes());
    }

    @Test
    void restaurarRecusaArquivoQueNaoEhBackupDaLocadora() throws IOException {
        Path texto = Files.writeString(dir.resolve("notas.db"), "isto nao e um banco");
        Path vazio = dir.resolve("vazio.db");

        assertRegra("O arquivo escolhido não é um backup da locadora.", () -> banco.arquivoService.restaurar(texto));
        assertRegra("O arquivo escolhido não é um backup da locadora.", () -> banco.arquivoService.restaurar(dir.resolve("nao-existe.db")));
        assertTrue(Files.notExists(dir.resolve("nao-existe.db")));
        Files.createFile(vazio);
        assertRegra("O arquivo escolhido não é um backup da locadora.", () -> banco.arquivoService.restaurar(vazio));
    }

    @Test
    void soGerenteRestauraEBackupExigeLogin() {
        Path backup = dir.resolve("copia.db");
        banco.arquivoService.fazerBackup(backup);

        banco.logarAtendente();
        assertRegra("Apenas gerentes podem fazer isso.", () -> banco.arquivoService.restaurar(backup));

        AuthService.sair();
        assertRegra("Faça login para continuar.", () -> banco.arquivoService.fazerBackup(dir.resolve("outra.db")));
    }

    @Test
    void nomeSugeridoDoBackupLevaAData() {
        assertEquals("locadora-backup-2026-10-01.db", banco.arquivoService.nomeSugeridoDeBackup());
    }
}
