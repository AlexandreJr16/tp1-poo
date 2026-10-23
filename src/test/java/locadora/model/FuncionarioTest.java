package locadora.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuncionarioTest {

    private Funcionario funcionario(Cargo cargo) {
        return new Funcionario(1, "Bruno Lima", "529.982.247-25", "92 98888-0000", "bruno@locadora.com", "bruno", null, null, cargo);
    }

    @Test
    void senhaConfereSoComASenhaCerta() {
        Funcionario bruno = funcionario(Cargo.ATENDENTE);

        bruno.definirSenha("segredo");

        assertTrue(bruno.senhaConfere("segredo"));
        assertFalse(bruno.senhaConfere("Segredo"));
        assertFalse(bruno.senhaConfere(""));
    }

    @Test
    void senhaNuncaFicaEmTextoPuro() {
        Funcionario bruno = funcionario(Cargo.ATENDENTE);

        bruno.definirSenha("segredo");

        assertNotEquals("segredo", bruno.getSenhaHash());
        assertTrue(bruno.getSenhaHash().matches("[0-9a-f]{64}"));
        assertTrue(bruno.getSal().matches("[0-9a-f]{32}"));
    }

    @Test
    void mesmaSenhaGeraHashDiferenteComSalNovo() {
        Funcionario bruno = funcionario(Cargo.ATENDENTE);

        bruno.definirSenha("segredo");
        String primeiro = bruno.getSenhaHash();
        bruno.definirSenha("segredo");

        assertNotEquals(primeiro, bruno.getSenhaHash());
        assertTrue(bruno.senhaConfere("segredo"));
    }

    @Test
    void hashEhSha256DoSalSeguidoDaSenha() {
        assertEquals("6ca13d52ca70c883e0f0bb101e425a89e8624de51db2d2392593af6a84118090", Funcionario.hash("abc", "123"));
    }

    @Test
    void funcionarioSemSenhaDefinidaNaoEntra() {
        assertFalse(funcionario(Cargo.GERENTE).senhaConfere("qualquer"));
    }

    @Test
    void soCargoGerenteEhGerente() {
        assertTrue(funcionario(Cargo.GERENTE).gerente());
        assertFalse(funcionario(Cargo.ATENDENTE).gerente());
    }
}
