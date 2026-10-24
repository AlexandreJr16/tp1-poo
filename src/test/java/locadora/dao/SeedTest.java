package locadora.dao;

import locadora.BancoTeste;
import locadora.Main;
import locadora.model.Locacao;
import locadora.model.Pessoa;
import locadora.model.Situacao;
import locadora.model.SituacaoReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedTest {

    @TempDir
    Path dir;

    private BancoTeste banco;

    @BeforeEach
    void carregar() {
        banco = new BancoTeste(dir);
        banco.conexao.executarScript("/seed.sql");
    }

    @Test
    void criaGerenteEAtendentesComSenhasConhecidas() {
        assertTrue(banco.funcionarios.buscarPorLogin("admin").gerente());
        assertTrue(banco.funcionarios.buscarPorLogin("admin").senhaConfere("admin123"));
        assertTrue(banco.funcionarios.buscarPorLogin("joana").senhaConfere("joana123"));
        assertFalse(banco.funcionarios.buscarPorLogin("joana").gerente());
        assertTrue(banco.funcionarios.buscarPorLogin("marcos").senhaConfere("marcos123"));
    }

    @Test
    void todosOsCpfsDoSeedSaoValidos() {
        Stream.concat(banco.clientes.listar().stream(), banco.funcionarios.listar().stream())
                .map(Pessoa::getCpf)
                .forEach(cpf -> assertTrue(Pessoa.cpfValido(cpf), cpf));
    }

    @Test
    void temAcervoDeTodosOsTiposComCodigosCoerentes() {
        assertEquals(4, banco.titulos.listar().stream().map(t -> t.tipo()).distinct().count());
        banco.titulos.listar().forEach(titulo -> banco.exemplares.listarPorTitulo(titulo.getId())
                .forEach(e -> assertTrue(e.getCodigo().startsWith("T" + titulo.getId() + "-"), e.getCodigo())));
    }

    @Test
    void temSituacoesVariadasParaDemonstracao() {
        LocalDate hoje = LocalDate.now();

        assertTrue(banco.locacoes.ativas().stream().anyMatch(l -> l.atrasada(hoje)));
        assertTrue(banco.locacoes.ativas().stream().anyMatch(l -> !l.atrasada(hoje)));
        assertFalse(banco.locacoes.comMultaPendente().isEmpty());
        assertTrue(banco.locacoes.listar().stream().anyMatch(Locacao::multaPaga));
        assertTrue(banco.reservas.ativas().stream().anyMatch(r -> r.getSituacao() == SituacaoReserva.PRONTA));
        assertTrue(banco.reservas.ativas().stream().anyMatch(r -> r.getSituacao() == SituacaoReserva.AGUARDANDO));
        assertFalse(banco.perdas.listar().isEmpty());
        assertTrue(banco.clientes.listar().stream().anyMatch(c -> !c.isAtivo()));
    }

    @Test
    void situacaoDosExemplaresBateComLocacoesReservasEPerdas() {
        banco.locacoes.ativas().forEach(l -> assertEquals(Situacao.ALUGADO, l.getExemplar().getSituacao(), l.getExemplar().getCodigo()));
        banco.reservas.ativas().stream()
                .filter(r -> r.getSituacao() == SituacaoReserva.PRONTA)
                .forEach(r -> assertEquals(Situacao.RESERVADO, r.getExemplar().getSituacao()));
        banco.perdas.listar().forEach(p -> assertEquals(Situacao.PERDIDO, p.getExemplar().getSituacao()));
        long alugados = banco.exemplares.listarPorSituacao(Situacao.ALUGADO).size();
        assertEquals(banco.locacoes.ativas().size(), alugados);
    }

    @Test
    void reservaSoAguardaTituloSemExemplarDisponivel() {
        banco.reservas.ativas().stream()
                .filter(r -> r.getSituacao() == SituacaoReserva.AGUARDANDO)
                .forEach(r -> assertTrue(banco.exemplares.listarPorTitulo(r.getTitulo().getId()).stream().noneMatch(e -> e.disponivel()),
                        r.getTitulo().getNome()));
    }

    @Test
    void bancoNovoJaVemComACapaDeTodosOsTitulosDoSeed() {
        Main.Servicos servicos = Main.montar(dir.resolve("novo.db"), Clock.systemDefaultZone());

        assertEquals(servicos.titulos().listar().size(), servicos.capas().capas().size());
    }
}
