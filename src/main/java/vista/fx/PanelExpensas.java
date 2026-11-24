package vista.fx;

import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import controlador.GestorBarrioControlador;
import modelo.comun.Fechas;
import modelo.comunidad.Lote;
import modelo.finanzas.*;
import modelo.personas.Persona;

/** Expensas: el titular consulta sus lotes; el administrador consulta todos, emite y registra pagos. */
final class PanelExpensas extends PanelBase {
    private final ComboBox<Lote> lote = new ComboBox<>();
    private final TextField fecha = new TextField();
    private final TextArea resultado = new TextArea();

    PanelExpensas(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        fecha.setPrefColumnCount(10);
        resultado.setEditable(false);
        HBox consulta = new HBox(8, new Label("Lote:"), lote, new Label("Fecha:"), fecha,
                Componentes.boton("Consultar cuenta", this::consultar));
        HBox barra = Componentes.barra();
        if (controlador.esAdministrador(actor)) {
            barra.getChildren().addAll(Componentes.boton("Emitir expensas", this::emitir),
                    Componentes.boton("Registrar pago", this::registrarPago));
        }
        barra.getChildren().add(0, consulta);
        setTop(barra);
        setCenter(resultado);
        refrescar();
    }

    @Override String titulo() { return "Expensas"; }

    @Override void refrescar() {
        Lote anterior = lote.getValue();
        lote.setItems(FXCollections.observableArrayList(controlador.listarLotesConsultables(actor)));
        if (anterior != null && lote.getItems().contains(anterior)) lote.setValue(anterior);
        else if (!lote.getItems().isEmpty()) lote.getSelectionModel().selectFirst();
        if (fecha.getText().isBlank()) fecha.setText(Fechas.hoy());
        if (lote.getValue() != null) consultar();
    }

    private void consultar() {
        int numero = Formulario.elegido(lote, "un lote").getNumero();
        String dia = fecha.getText().trim();
        EstadoCuenta cuenta = controlador.consultarCuenta(actor, numero, dia);
        String texto = "ESTADO DE CUENTA — lote " + numero + " al " + dia + "\n" + cuenta + "\n\nPAGOS\n";
        for (Pago pago : controlador.consultarPagos(actor, numero, dia)) texto += pago + "\n";
        texto += "\nAJUSTES\n";
        for (AsientoAjuste ajuste : controlador.consultarAjustes(actor, numero, dia)) {
            texto += ajuste.getFecha() + " | pago " + ajuste.getPagoCorregido().getNumero()
                    + " | $" + ajuste.getMontoAjuste() + " | " + ajuste.getMotivo() + "\n";
        }
        resultado.setText(texto);
    }

    private void emitir() {
        Formulario f = new Formulario("Emitir expensas");
        TextField periodo = f.texto("Período (aaaa-MM)", Fechas.hoy().substring(0, 7));
        TextField presupuesto = f.texto("Presupuesto mensual", "");
        if (!f.mostrar()) return;
        operar(() -> {
            int emitidas = controlador.emitirExpensas(actor, periodo.getText(),
                    Componentes.decimal(presupuesto.getText(), "Presupuesto")).size();
            Componentes.info("Expensas emitidas: " + emitidas);
        });
    }

    private void registrarPago() {
        Formulario f = new Formulario("Registrar pago");
        ComboBox<Lote> elegido = f.opciones("Lote", controlador.listarLotes(actor));
        TextField dia = f.texto("Fecha", Fechas.hoy());
        TextField importe = f.texto("Importe", "");
        ComboBox<MedioPago> medio = f.opciones("Medio de pago", Arrays.asList(MedioPago.values()));
        if (!f.mostrar()) return;
        operar(() -> Componentes.info(String.valueOf(controlador.registrarPago(actor,
                Formulario.elegido(elegido, "un lote").getNumero(), dia.getText(),
                Componentes.decimal(importe.getText(), "Importe"), Formulario.elegido(medio, "un medio de pago")))));
    }
}
