package vista.javafx;

import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import controlador.GestorBarrioControlador;
import modelo.comun.RelojSesion;
import modelo.comunidad.Lote;
import modelo.finanzas.*;
import modelo.personas.Persona;

/** Pestaña de expensas: consulta de cuenta (titular y administrador), emisión, pagos y ajustes (administrador). */
final class PanelExpensas extends PanelBase {
    private final ComboBox<Lote> lote = new ComboBox<>();
    private final TextField fecha = new TextField();
    private final TextArea resultado = new TextArea();

    PanelExpensas(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        fecha.setPrefColumnCount(10);
        resultado.setEditable(false);
        HBox barra = Componentes.barra();
        barra.getChildren().addAll(new Label("Lote:"), lote, new Label("Fecha:"), fecha,
                Componentes.boton("Consultar cuenta", this::consultar));
        if (controlador.esAdministrador(actor)) {
            barra.getChildren().addAll(new Separator(javafx.geometry.Orientation.VERTICAL),
                    Componentes.boton("Emitir expensas", this::emitir),
                    Componentes.boton("Registrar pago", this::registrarPago),
                    Componentes.boton("Corregir pago", this::ajustarPago));
        }
        setTop(barra);
        setCenter(resultado);
        refrescar();
    }

    @Override String titulo() { return "Expensas"; }

    @Override void refrescar() {
        Lote anterior = lote.getValue();
        lote.setItems(FXCollections.observableArrayList(controlador.listarLotesConsultables(actor)));
        if (anterior != null && lote.getItems().contains(anterior)) lote.setValue(anterior);
        if (fecha.getText().isBlank()) fecha.setText(RelojSesion.getInstancia().hoy());
    }

    private void consultar() {
        int numero = Formulario.elegido(lote, "un lote").getNumero();
        String dia = fecha.getText().trim();
        EstadoCuenta cuenta = controlador.consultarCuenta(actor, numero, dia);
        String texto = cuenta + "\n\nPAGOS ORIGINALES\n";
        for (Pago pago : controlador.consultarPagos(actor, numero, dia)) texto += pago + "\n";
        texto += "\nAJUSTES\n";
        for (AsientoAjuste ajuste : controlador.consultarAjustes(actor, numero, dia)) {
            texto += ajuste.getFecha() + " | pago " + ajuste.getPagoCorregido().getNumero()
                    + " | $" + ajuste.getMontoAjuste() + " | " + ajuste.getMotivo() + "\n";
        }
        texto += "\nIMPUTACIONES EFECTIVAS\n";
        for (ImputacionPago imputacion : cuenta.getImputacionesEfectivas()) {
            texto += "Pago " + imputacion.getPago().getNumero() + " | período " + imputacion.getExpensa().getPeriodo()
                    + " | intereses $" + imputacion.getMontoInteres() + " | capital $" + imputacion.getMontoCapital() + "\n";
        }
        resultado.setText(texto);
    }

    private void emitir() {
        Formulario f = new Formulario("Emitir expensas");
        TextField periodo = f.texto("Período (aaaa-MM)", RelojSesion.getInstancia().hoy().substring(0, 7));
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
        TextField dia = f.texto("Fecha del pago", RelojSesion.getInstancia().hoy());
        TextField importe = f.texto("Importe", "");
        ComboBox<MedioPago> medio = f.opciones("Medio de pago", Arrays.asList(MedioPago.values()));
        if (!f.mostrar()) return;
        operar(() -> Componentes.info(String.valueOf(controlador.registrarPago(actor,
                Formulario.elegido(elegido, "un lote").getNumero(), dia.getText(),
                Componentes.decimal(importe.getText(), "Importe"), Formulario.elegido(medio, "un medio de pago")))));
    }

    private void ajustarPago() {
        int numero = Formulario.elegido(lote, "un lote").getNumero();
        Formulario f = new Formulario("Corregir pago - Lote " + numero);
        ComboBox<Pago> pago = f.opciones("Pago a corregir", controlador.consultarPagos(actor, numero, RelojSesion.getInstancia().hoy()));
        TextField dia = f.texto("Fecha del ajuste", RelojSesion.getInstancia().hoy());
        TextField monto = f.texto("Importe del ajuste (con signo)", "");
        TextField motivo = f.texto("Motivo", "");
        if (!f.mostrar()) return;
        operar(() -> controlador.ajustarPago(actor, numero, Formulario.elegido(pago, "un pago").getNumero(),
                dia.getText(), Componentes.decimal(monto.getText(), "Importe del ajuste"), motivo.getText()));
    }
}
