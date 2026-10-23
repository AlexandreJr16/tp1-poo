package locadora.service;

import locadora.BancoTeste;
import locadora.model.Cargo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    @TempDir
    Path dir;

    private AuthService auth;

    @BeforeEach
    void abrir() {
        BancoTeste banco = new BancoTeste(dir);
        banco.funcionario("admin", Cargo.GERENTE);
        banco.funcionario("bruno", Cargo.ATENDENTE);
        auth = new AuthService(banco.funcionarios);
    }

    @AfterEach
    void sair() {
        AuthService.sair();
    }

    @Test
    void entrarComLoginESenhaCertosAbreASessao() {
        auth.entrar("admin", "123");

        assertEquals("admin", AuthService.logado().getLogin());
        assertEquals("admin", AuthService.exigirLogado().getLogin());
    }

    @Test
    void loginIgnoraEspacosEMaiusculas() {
        auth.entrar("  ADMIN ", "123");

        assertEquals("admin", AuthService.logado().getLogin());
    }

    @Test
    void senhaErradaOuLoginInexistenteDaAMesmaMensagem() {
        RegraException senhaErrada = assertThrows(RegraException.class, () -> auth.entrar("admin", "errada"));
        RegraException loginInexistente = assertThrows(RegraException.class, () -> auth.entrar("ninguem", "123"));

        assertEquals("Login ou senha incorretos.", senhaErrada.getMessage());
        assertEquals("Login ou senha incorretos.", loginInexistente.getMessage());
        assertNull(AuthService.logado());
    }

    @Test
    void sairEncerraASessao() {
        auth.entrar("admin", "123");

        AuthService.sair();

        assertNull(AuthService.logado());
        RegraException e = assertThrows(RegraException.class, AuthService::exigirLogado);
        assertEquals("Faça login para continuar.", e.getMessage());
    }

    @Test
    void soGerentePassaPelaExigenciaDeGerente() {
        auth.entrar("bruno", "123");
        RegraException e = assertThrows(RegraException.class, AuthService::exigirGerente);
        assertEquals("Apenas gerentes podem fazer isso.", e.getMessage());

        auth.entrar("admin", "123");
        assertEquals("admin", AuthService.exigirGerente().getLogin());
    }
}
