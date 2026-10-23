package locadora.view.dialogo;

import com.formdev.flatlaf.FlatClientProperties;
import locadora.Main;
import locadora.model.Cliente;
import locadora.model.Estoque;
import locadora.model.Titulo;
import locadora.service.RegraException;
import locadora.view.componente.Seletor;

import javax.swing.JLabel;
import java.awt.Component;
import java.util.Map;

// nova reserva: escolhe o cliente e o titulo (ou recebe o titulo ja escolhido no detalhe)
public class ReservaDialog extends Formulario {

    private final Main.Servicos servicos;
    private final Titulo fixo;
    private final Seletor<Cliente> cliente = new Seletor<>("Buscar cliente por nome ou CPF", Cliente::getNome, Cliente::cpfFormatado);
    private final Seletor<Titulo> titulo;

    public ReservaDialog(Component origem, Main.Servicos servicos, Titulo fixo) {
        super(origem, "Nova reserva", "Reservar");
        this.servicos = servicos;
        this.fixo = fixo;
        Map<Integer, Estoque> estoque = servicos.titulos().estoque();
        titulo = new Seletor<>("Buscar título", Titulo::getNome,
                (Titulo t) -> t.tipo() + " · " + estoque.getOrDefault(t.getId(), Estoque.VAZIO).disponiveis() + " disponíveis");
        cliente.setItens(servicos.clientes().listar().stream().filter(Cliente::isAtivo).toList());
        campo("Cliente", cliente);
        if (fixo == null) {
            titulo.setItens(servicos.titulos().listar());
            campo("Título", titulo);
        } else {
            JLabel escolhido = new JLabel(fixo.getNome() + " · " + fixo.tipo());
            escolhido.putClientProperty(FlatClientProperties.STYLE_CLASS, "h4");
            campo("Título", escolhido);
        }
        nota("A reserva entra na fila do título. Quando um exemplar voltar ou for comprado, ele fica separado para o primeiro da fila.");
    }

    @Override
    protected void salvar() {
        Titulo escolhido = fixo != null ? fixo : titulo.selecionado();
        RegraException.se(cliente.selecionado() == null, "Escolha o cliente.");
        RegraException.se(escolhido == null, "Escolha o título.");
        servicos.reservas().reservar(cliente.selecionado().getId(), escolhido.getId());
    }
}
