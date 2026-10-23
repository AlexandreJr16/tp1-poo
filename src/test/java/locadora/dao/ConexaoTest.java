package locadora.dao;

import locadora.model.Cargo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConexaoTest {

    private static final String INSERIR_TITULO = "INSERT INTO titulo (tipo, nome, descricao, ano, prazo_maximo_dias) VALUES (?, ?, '', 1999, 5)";

    @TempDir
    Path dir;

    private Conexao conexao;

    @BeforeEach
    void abrir() {
        conexao = new Conexao(dir.resolve("teste.db"));
    }

    private int numero(String sql, Object... parametros) {
        return conexao.buscar(sql, (rs, c) -> rs.getInt(1), parametros);
    }

    @Test
    void criaSchemaSoQuandoOBancoEstaVazio() {
        assertTrue(conexao.criarSchemaSeVazio());
        assertFalse(conexao.criarSchemaSeVazio());
    }

    @Test
    void inserirDevolveOIdGerado() {
        conexao.criarSchemaSeVazio();

        int primeiro = conexao.inserir(INSERIR_TITULO, "DVD", "Matrix");
        int segundo = conexao.inserir(INSERIR_TITULO, "CD", "Clube da Esquina");

        assertEquals(1, primeiro);
        assertEquals(2, segundo);
        assertEquals("Clube da Esquina", conexao.buscar("SELECT nome FROM titulo WHERE id = ?", (rs, c) -> rs.getString(1), segundo));
    }

    @Test
    void chavesEstrangeirasFicamLigadas() {
        conexao.criarSchemaSeVazio();

        assertThrows(BancoException.class, () -> conexao.executar(
                "INSERT INTO exemplar (codigo, titulo_id, aquisicao_id, custo_centavos, situacao) VALUES (?, ?, ?, ?, ?)",
                "T9-001", 999, 999, 100, "DISPONIVEL"));
    }

    @Test
    void transacaoDesfazTudoQuandoUmComandoFalha() {
        conexao.criarSchemaSeVazio();

        assertThrows(BancoException.class, () -> conexao.transacao(c -> {
            Conexao.inserir(c, INSERIR_TITULO, "DVD", "Matrix");
            Conexao.inserir(c, INSERIR_TITULO, "VHS", "Tipo invalido");
            return null;
        }));

        assertEquals(0, numero("SELECT COUNT(*) FROM titulo"));
    }

    @Test
    void gravaDinheiroEmCentavosEDatasEnumsEBooleanosComoValoresSimples() {
        conexao.criarSchemaSeVazio();

        int cliente = conexao.inserir("INSERT INTO cliente (nome, cpf, ativo) VALUES (?, ?, ?)", "Ana", "52998224725", false);
        int funcionario = conexao.inserir("INSERT INTO funcionario (nome, cpf, login, senha_hash, sal, cargo) VALUES (?, ?, ?, ?, ?, ?)",
                "Bruno", "52998224725", "bruno", "hash", "sal", Cargo.GERENTE);
        int titulo = conexao.inserir(INSERIR_TITULO, "DVD", "Matrix");
        int aquisicao = conexao.inserir("INSERT INTO aquisicao (titulo_id, quantidade, custo_unitario_centavos, fornecedor, data, funcionario_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)", titulo, 2, new BigDecimal("29.90"), "Distribuidora", LocalDate.of(2026, 10, 1), funcionario);

        assertEquals(0, numero("SELECT ativo FROM cliente WHERE id = ?", cliente));
        assertEquals("GERENTE", conexao.buscar("SELECT cargo FROM funcionario WHERE id = ?", (rs, c) -> rs.getString(1), funcionario));
        assertEquals(2990, numero("SELECT custo_unitario_centavos FROM aquisicao WHERE id = ?", aquisicao));
        assertEquals("2026-10-01", conexao.buscar("SELECT data FROM aquisicao WHERE id = ?", (rs, c) -> rs.getString(1), aquisicao));
    }

    @Test
    void converteCentavosEDatasDoBanco() {
        assertEquals(new BigDecimal("12.90"), Conexao.reais(1290));
        assertEquals(LocalDate.of(2026, 10, 1), Conexao.data("2026-10-01"));
        assertNull(Conexao.data(null));
    }

    @Test
    void executarScriptRodaTodosOsComandos() {
        conexao.criarSchemaSeVazio();

        conexao.executarScript("/locadora/dao/script-teste.sql");

        assertEquals(2, numero("SELECT COUNT(*) FROM titulo"));
        assertEquals(0, numero("SELECT ativo FROM cliente WHERE cpf = ?", "52998224725"));
    }
}
