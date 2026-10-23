package locadora.service;

import locadora.dao.ClienteDao;
import locadora.dao.ExemplarDao;
import locadora.dao.LocacaoDao;
import locadora.dao.ReservaDao;
import locadora.dao.TituloDao;
import locadora.model.Cliente;
import locadora.model.Exemplar;
import locadora.model.Funcionario;
import locadora.model.Reserva;
import locadora.model.SituacaoReserva;
import locadora.model.Titulo;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/* - Reserva:
     reservar: coloca o cliente na fila de um titulo que nao tem exemplar disponivel
     cancelar: encerra a reserva; o exemplar que ela segurava vai para o proximo da fila ou volta a ficar disponivel
   - Consulta:
     ativas: reservas aguardando ou prontas, em ordem de chegada
     prontas: reservas com exemplar separado esperando a retirada
     fila: reservas aguardando de um titulo
     posicao: lugar da reserva na fila do titulo (0 se ela nao esta aguardando) */
public class ReservaService {

    private final ReservaDao reservas;
    private final TituloDao titulos;
    private final ClienteDao clientes;
    private final ExemplarDao exemplares;
    private final LocacaoDao locacoes;
    private final Clock relogio;

    public ReservaService(ReservaDao reservas, TituloDao titulos, ClienteDao clientes, ExemplarDao exemplares, LocacaoDao locacoes, Clock relogio) {
        this.reservas = reservas;
        this.titulos = titulos;
        this.clientes = clientes;
        this.exemplares = exemplares;
        this.locacoes = locacoes;
        this.relogio = relogio;
    }

    public Reserva reservar(int clienteId, int tituloId) {
        Funcionario funcionario = AuthService.exigirLogado();
        Cliente cliente = clientes.buscar(clienteId);
        RegraException.se(cliente == null, "Cliente não encontrado.");
        RegraException.se(!cliente.isAtivo(), "Cliente inativo não pode reservar.");
        Titulo titulo = titulos.buscar(tituloId);
        RegraException.se(titulo == null, "Título não encontrado.");
        RegraException.se(exemplares.listarPorTitulo(tituloId).stream().anyMatch(Exemplar::disponivel),
                "Há exemplar disponível deste título. Faça a locação direto.");
        RegraException.se(reservas.doCliente(clienteId).stream().anyMatch(r -> r.ativa() && r.getTitulo().getId() == tituloId),
                "O cliente já tem uma reserva ativa para este título.");
        RegraException.se(locacoes.doCliente(clienteId).stream().anyMatch(l -> l.ativa() && l.getExemplar().getTitulo().getId() == tituloId),
                "O cliente já está com este título alugado.");
        Reserva reserva = Reserva.abrir(titulo, cliente, funcionario, LocalDate.now(relogio));
        reservas.inserir(reserva);
        return reserva;
    }

    public void cancelar(int reservaId) {
        AuthService.exigirLogado();
        Reserva reserva = reservas.buscar(reservaId);
        RegraException.se(reserva == null, "Reserva não encontrada.");
        RegraException.se(!reserva.ativa(), "Esta reserva já foi encerrada.");
        Exemplar separado = reserva.getExemplar();
        reserva.cancelar();
        Reserva proxima = separado == null ? null : fila(reserva.getTitulo().getId()).stream().findFirst().orElse(null);
        if (proxima != null)
            proxima.preparar(separado);
        reservas.registrarCancelamento(reserva, proxima);
    }

    public List<Reserva> ativas() {
        return reservas.ativas();
    }

    public List<Reserva> prontas() {
        return ativas().stream().filter(r -> r.getSituacao() == SituacaoReserva.PRONTA).toList();
    }

    public List<Reserva> fila(int tituloId) {
        return reservas.fila(tituloId);
    }

    public int posicao(Reserva reserva) {
        if (reserva.getSituacao() != SituacaoReserva.AGUARDANDO)
            return 0;
        List<Integer> ids = fila(reserva.getTitulo().getId()).stream().map(Reserva::getId).toList();
        return ids.indexOf(reserva.getId()) + 1;
    }
}
