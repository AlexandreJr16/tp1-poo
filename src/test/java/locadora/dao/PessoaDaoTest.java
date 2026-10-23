package locadora.dao;

import locadora.BancoTeste;
import locadora.model.Cargo;
import locadora.model.Cliente;
import locadora.model.Funcionario;
import locadora.model.Pessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PessoaDaoTest {

    @TempDir
    Path dir;

    private BancoTeste banco;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
    }

    @Test
    void clienteInseridoVoltaPorIdEPorCpf() {
        Cliente ana = new Cliente(0, "Ana Souza", banco.cpf(), "92 99999-0000", "ana@email.com", "Rua A, 10", true);

        banco.clientes.inserir(ana);

        Cliente porId = banco.clientes.buscar(ana.getId());
        assertEquals("Ana Souza", porId.getNome());
        assertEquals("92 99999-0000", porId.getTelefone());
        assertEquals("ana@email.com", porId.getEmail());
        assertEquals("Rua A, 10", porId.getEndereco());
        assertTrue(porId.isAtivo());
        assertEquals(ana.getId(), banco.clientes.buscarPorCpf(ana.getCpf()).getId());
        assertNull(banco.clientes.buscarPorCpf("00000000000"));
    }

    @Test
    void atualizarClienteGravaDadosEDesativa() {
        Cliente ana = banco.cliente("Ana Souza");

        banco.clientes.atualizar(new Cliente(ana.getId(), "Ana S. Lima", ana.getCpf(), "92 3333-0000", "", "Rua B, 20", false));

        Cliente lido = banco.clientes.buscar(ana.getId());
        assertEquals("Ana S. Lima", lido.getNome());
        assertEquals("Rua B, 20", lido.getEndereco());
        assertFalse(lido.isAtivo());
    }

    @Test
    void listarClientesOrdenaPorNome() {
        banco.cliente("Carla");
        banco.cliente("ana");
        banco.cliente("Bruno");

        assertEquals(List.of("ana", "Bruno", "Carla"), banco.clientes.listar().stream().map(Pessoa::getNome).toList());
    }

    @Test
    void funcionarioGuardaHashESalEVoltaPeloLogin() {
        Funcionario bruno = banco.funcionario("bruno", Cargo.GERENTE);

        Funcionario lido = banco.funcionarios.buscarPorLogin("bruno");

        assertEquals(bruno.getId(), lido.getId());
        assertEquals(bruno.getSal(), lido.getSal());
        assertEquals(Cargo.GERENTE, lido.getCargo());
        assertTrue(lido.senhaConfere("123"));
        assertNull(banco.funcionarios.buscarPorLogin("ninguem"));
    }

    @Test
    void atualizarFuncionarioTrocaSenhaECargo() {
        Funcionario bruno = banco.funcionario("bruno", Cargo.ATENDENTE);
        Funcionario promovido = new Funcionario(bruno.getId(), "Bruno Lima", bruno.getCpf(), "92 98888-0000", "bruno@locadora.com",
                "bruno", null, null, Cargo.GERENTE);
        promovido.definirSenha("nova");

        banco.funcionarios.atualizar(promovido);

        Funcionario lido = banco.funcionarios.buscar(bruno.getId());
        assertTrue(lido.senhaConfere("nova"));
        assertFalse(lido.senhaConfere("123"));
        assertTrue(lido.gerente());
        assertEquals("Bruno Lima", lido.getNome());
        assertEquals(bruno.getId(), banco.funcionarios.buscarPorCpf(bruno.getCpf()).getId());
    }

    @Test
    void listarFuncionariosOrdenaPorNome() {
        banco.funcionario("zeca", Cargo.ATENDENTE);
        banco.funcionario("admin", Cargo.GERENTE);

        assertEquals(List.of("admin", "zeca"), banco.funcionarios.listar().stream().map(Funcionario::getLogin).toList());
    }
}
