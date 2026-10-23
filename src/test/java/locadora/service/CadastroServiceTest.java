package locadora.service;

import locadora.BancoTeste;
import locadora.model.BluRay;
import locadora.model.Cargo;
import locadora.model.Cd;
import locadora.model.Cliente;
import locadora.model.Estoque;
import locadora.model.Funcionario;
import locadora.model.Locacao;
import locadora.model.Titulo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static locadora.BancoTeste.assertRegra;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CadastroServiceTest {

    @TempDir
    Path dir;

    private BancoTeste banco;

    @BeforeEach
    void abrir() {
        banco = new BancoTeste(dir);
    }

    @Test
    void salvarTituloNovoCriaASubclasseDoTipoEditarTrocaOTipo() {
        banco.logarAtendente();

        Titulo duna = banco.tituloService.salvar("Blu-ray", 0, "  Duna  ", "Ficcao", 2021, 3);
        banco.tituloService.salvar("CD", duna.getId(), "Duna - Trilha Sonora", "Hans Zimmer", 2021, 7);

        assertInstanceOf(BluRay.class, duna);
        assertEquals("Duna", duna.getNome());
        Titulo editado = banco.tituloService.buscar(duna.getId());
        assertInstanceOf(Cd.class, editado);
        assertEquals("Duna - Trilha Sonora", editado.getNome());
        assertEquals(1, banco.tituloService.listar().size());
    }

    @Test
    void tituloInvalidoGeraMensagemAmigavel() {
        banco.logarAtendente();

        assertRegra("Informe o nome do título.", () -> banco.tituloService.salvar("DVD", 0, "  ", "", 2000, 5));
        assertRegra("Escolha o tipo de mídia.", () -> banco.tituloService.salvar("VHS", 0, "Matrix", "", 1999, 5));
        assertRegra("Informe um ano entre 1900 e 2027.", () -> banco.tituloService.salvar("DVD", 0, "Matrix", "", 1899, 5));
        assertRegra("Informe um ano entre 1900 e 2027.", () -> banco.tituloService.salvar("DVD", 0, "Matrix", "", 2028, 5));
        assertRegra("O prazo deve ser de 1 a 30 dias.", () -> banco.tituloService.salvar("DVD", 0, "Matrix", "", 1999, 0));
        assertRegra("O prazo deve ser de 1 a 30 dias.", () -> banco.tituloService.salvar("DVD", 0, "Matrix", "", 1999, 31));
        assertTrue(banco.tituloService.listar().isEmpty());
    }

    @Test
    void cadastrarTituloExigeLogin() {
        assertRegra("Faça login para continuar.", () -> banco.tituloService.salvar("DVD", 0, "Matrix", "", 1999, 5));
    }

    @Test
    void tiposListaAsQuatroMidias() {
        assertEquals(List.of("Blu-ray", "DVD", "CD", "Fita"), TituloService.TIPOS);
    }

    @Test
    void estoqueEExemplaresDoTitulo() {
        Funcionario gerente = banco.logarGerente();
        Titulo matrix = banco.dvd("Matrix");
        Titulo semExemplares = banco.dvd("Duna");
        banco.adquirir(matrix, 2, gerente);

        assertEquals(new Estoque(2, 0, 0, 0), banco.tituloService.estoque().get(matrix.getId()));
        assertEquals(new Estoque(2, 0, 0, 0), banco.tituloService.estoqueDe(matrix.getId()));
        assertEquals(Estoque.VAZIO, banco.tituloService.estoqueDe(semExemplares.getId()));
        assertEquals(2, banco.tituloService.exemplares(matrix.getId()).size());
    }

    @Test
    void salvarClienteNovoGuardaCpfSoComDigitos() {
        banco.logarAtendente();

        Cliente ana = banco.clienteService.salvar(new Cliente(0, " Ana Souza ", "529.982.247-25", "92 99999-0000", "ana@email.com", "Rua A", true));

        Cliente lido = banco.clienteService.buscar(ana.getId());
        assertEquals("52998224725", lido.getCpf());
        assertEquals("Ana Souza", lido.getNome());
        assertEquals(1, banco.clienteService.listar().size());
    }

    @Test
    void clienteInvalidoGeraMensagemAmigavel() {
        banco.logarAtendente();
        banco.clienteService.salvar(new Cliente(0, "Ana", "529.982.247-25", "", "", "", true));

        assertRegra("Informe o nome do cliente.", () -> banco.clienteService.salvar(new Cliente(0, "", banco.cpf(), "", "", "", true)));
        assertRegra("CPF inválido.", () -> banco.clienteService.salvar(new Cliente(0, "Bruno", "123.456.789-00", "", "", "", true)));
        assertRegra("Já existe um cliente com este CPF.", () -> banco.clienteService.salvar(new Cliente(0, "Bruno", "52998224725", "", "", "", true)));
        assertRegra("E-mail inválido.", () -> banco.clienteService.salvar(new Cliente(0, "Bruno", banco.cpf(), "", "bruno@", "", true)));
    }

    @Test
    void editarClienteMantendoOCpfNaoAcusaDuplicidade() {
        banco.logarAtendente();
        Cliente ana = banco.clienteService.salvar(new Cliente(0, "Ana", "529.982.247-25", "", "", "", true));

        banco.clienteService.salvar(new Cliente(ana.getId(), "Ana Lima", "52998224725", "92 3333-0000", "", "", true));

        assertEquals("Ana Lima", banco.clienteService.buscar(ana.getId()).getNome());
    }

    @Test
    void soGerenteDesativaCliente() {
        Cliente ana = banco.cliente("Ana");
        Cliente inativa = new Cliente(ana.getId(), "Ana", ana.getCpf(), "", "", "", false);

        banco.logarAtendente();
        assertRegra("Apenas gerentes podem fazer isso.", () -> banco.clienteService.salvar(inativa));

        banco.logarGerente();
        banco.clienteService.salvar(inativa);
        assertFalse(banco.clienteService.buscar(ana.getId()).isAtivo());
    }

    @Test
    void historicoMultasEReservasDoCliente() {
        Funcionario gerente = banco.logarGerente();
        Cliente ana = banco.cliente("Ana");
        Titulo matrix = banco.dvd("Matrix");
        Locacao locacao = Locacao.abrir(banco.adquirir(matrix, 1, gerente).get(0), ana, gerente, BancoTeste.HOJE);
        banco.locacoes.registrar(locacao, null);
        locacao.devolver(LocalDate.of(2026, 10, 9), gerente);
        banco.locacoes.registrarDevolucao(locacao, null);

        assertEquals(new BigDecimal("10.50"), banco.clienteService.multasPendentes(ana.getId()));
        assertEquals(new BigDecimal("0.00"), banco.clienteService.multasPendentes(banco.cliente("Bruno").getId()));
        assertEquals(1, banco.clienteService.historico(ana.getId()).size());
        assertTrue(banco.clienteService.reservas(ana.getId()).isEmpty());
    }

    @Test
    void cadastrarFuncionarioGuardaHashELoginMinusculo() {
        banco.logarGerente();

        Funcionario carla = banco.funcionarioService.cadastrar("Carla Dias", "529.982.247-25", "", "carla@locadora.com", " Carla ", "segredo1", Cargo.ATENDENTE);

        Funcionario lida = banco.funcionarios.buscar(carla.getId());
        assertEquals("carla", lida.getLogin());
        assertTrue(lida.senhaConfere("segredo1"));
        assertEquals(Cargo.ATENDENTE, lida.getCargo());
    }

    @Test
    void soGerenteCadastraFuncionario() {
        banco.logarAtendente();

        assertRegra("Apenas gerentes podem fazer isso.",
                () -> banco.funcionarioService.cadastrar("Carla", banco.cpf(), "", "", "carla", "segredo1", Cargo.ATENDENTE));
    }

    @Test
    void funcionarioInvalidoGeraMensagemAmigavel() {
        Funcionario gerente = banco.logarGerente();
        String cpf = banco.cpf();

        assertRegra("Informe o nome do funcionário.", () -> banco.funcionarioService.cadastrar(" ", cpf, "", "", "carla", "segredo1", Cargo.ATENDENTE));
        assertRegra("CPF inválido.", () -> banco.funcionarioService.cadastrar("Carla", "111.111.111-11", "", "", "carla", "segredo1", Cargo.ATENDENTE));
        assertRegra("Já existe um funcionário com este CPF.",
                () -> banco.funcionarioService.cadastrar("Carla", gerente.getCpf(), "", "", "carla", "segredo1", Cargo.ATENDENTE));
        assertRegra("E-mail inválido.", () -> banco.funcionarioService.cadastrar("Carla", cpf, "", "carla.com", "carla", "segredo1", Cargo.ATENDENTE));
        assertRegra("Informe o login.", () -> banco.funcionarioService.cadastrar("Carla", cpf, "", "", "  ", "segredo1", Cargo.ATENDENTE));
        assertRegra("Este login já está em uso.", () -> banco.funcionarioService.cadastrar("Carla", cpf, "", "", "GERENTE", "segredo1", Cargo.ATENDENTE));
        assertRegra("A senha precisa ter pelo menos 6 caracteres.", () -> banco.funcionarioService.cadastrar("Carla", cpf, "", "", "carla", "12345", Cargo.ATENDENTE));
        assertRegra("Escolha o cargo.", () -> banco.funcionarioService.cadastrar("Carla", cpf, "", "", "carla", "segredo1", null));
    }

    @Test
    void atualizarFuncionarioMantemASenhaETrocaOCargo() {
        Funcionario bruno = banco.funcionario("bruno", Cargo.ATENDENTE);
        banco.logarGerente();

        banco.funcionarioService.atualizar(bruno.getId(), "Bruno Lima", bruno.getCpf(), "92 98888-0000", "", Cargo.GERENTE);

        Funcionario lido = banco.funcionarios.buscar(bruno.getId());
        assertEquals("Bruno Lima", lido.getNome());
        assertTrue(lido.gerente());
        assertTrue(lido.senhaConfere("123"));
        assertEquals(2, banco.funcionarioService.listar().size());
    }

    @Test
    void naoRebaixaOUltimoGerente() {
        Funcionario gerente = banco.logarGerente();

        assertRegra("A locadora precisa de pelo menos um gerente.",
                () -> banco.funcionarioService.atualizar(gerente.getId(), "Gerente", gerente.getCpf(), "", "", Cargo.ATENDENTE));
    }

    @Test
    void trocarSenhaConfereASenhaAtual() {
        banco.logarAtendente();

        assertRegra("A senha atual está incorreta.", () -> banco.funcionarioService.trocarSenha("errada", "novaSenha"));
        assertRegra("A senha precisa ter pelo menos 6 caracteres.", () -> banco.funcionarioService.trocarSenha("123", "curta"));
        banco.funcionarioService.trocarSenha("123", "novaSenha");

        AuthService.sair();
        assertEquals("atendente", banco.auth.entrar("atendente", "novaSenha").getLogin());
    }

    @Test
    void gerenteRedefineSenhaDeOutroFuncionario() {
        Funcionario bruno = banco.funcionario("bruno", Cargo.ATENDENTE);
        banco.logarAtendente();
        assertRegra("Apenas gerentes podem fazer isso.", () -> banco.funcionarioService.redefinirSenha(bruno.getId(), "provisoria"));

        banco.logarGerente();
        banco.funcionarioService.redefinirSenha(bruno.getId(), "provisoria");

        assertTrue(banco.funcionarios.buscar(bruno.getId()).senhaConfere("provisoria"));
    }
}
