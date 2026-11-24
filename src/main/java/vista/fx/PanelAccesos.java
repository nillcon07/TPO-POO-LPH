package vista.fx;

import java.util.Arrays;
import java.util.List;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import controlador.GestorBarrioControlador;
import modelo.accesos.*;
import modelo.comun.Fechas;
import modelo.comunidad.Lote;
import modelo.personas.*;

/** Visitas y accesos: vecinos autorizan, la portería registra ingresos y egresos, el administrador supervisa. */
final class PanelAccesos extends PanelBase {
    private final TableView<AutorizacionAcceso> autorizaciones = Componentes.tabla();
    private final TableView<RegistroAcceso> accesos = Componentes.tabla();
    private final boolean veAccesos;

    PanelAccesos(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        boolean porteria = controlador.esGuardiaPorteria(actor);
        veAccesos = porteria || controlador.esAdministrador(actor);

        Componentes.columna(autorizaciones, "N°", AutorizacionAcceso::getNumero);
        Componentes.columna(autorizaciones, "Visitante", AutorizacionAcceso::getPersonaAutorizada);
        Componentes.columna(autorizaciones, "Tipo", a -> a.getPersonaAutorizada().getTipo());
        Componentes.columna(autorizaciones, "Lote", a -> a.getLoteVisitado().getNumero());
        Componentes.columna(autorizaciones, "Desde", a -> a.getFechaInicio() + " " + a.getHoraDesde());
        Componentes.columna(autorizaciones, "Hasta", a -> a.getFechaFin() + " " + a.getHoraHasta());
        Componentes.columna(autorizaciones, "Autorizó", AutorizacionAcceso::getPersonaAutoriza);
        Componentes.columna(accesos, "Visitante", r -> r.getAutorizacion().getPersonaAutorizada());
        Componentes.columna(accesos, "Ingreso", RegistroAcceso::getFechaHoraIngreso);
        Componentes.columna(accesos, "Egreso", r -> r.getFechaHoraEgreso() == null ? "PENDIENTE" : r.getFechaHoraEgreso());
        Componentes.columna(accesos, "Guardia", RegistroAcceso::getGuardia);

        HBox barra = Componentes.barra();
        if (disponible(() -> controlador.listarLotesParaAutorizarVisitas(actor)))
            barra.getChildren().add(Componentes.boton("Autorizar visita", this::autorizarVisita));
        if (controlador.esAdministrador(actor))
            barra.getChildren().add(Componentes.boton("Registrar proveedor habitual", this::proveedorHabitual));
        if (porteria) {
            barra.getChildren().addAll(Componentes.boton("Registrar ingreso", this::registrarIngreso),
                    Componentes.boton("Registrar egreso", this::registrarEgreso));
        }
        setTop(barra);

        VBox centro = new VBox(8, new Label("Autorizaciones"), autorizaciones);
        if (veAccesos) centro.getChildren().addAll(new Label("Ingresos y egresos"), accesos);
        setCenter(centro);
        refrescar();
    }

    @Override String titulo() { return "Accesos"; }

    @Override void refrescar() {
        Componentes.cargar(autorizaciones, controlador.listarAutorizaciones(actor));
        if (veAccesos) Componentes.cargar(accesos, controlador.listarAccesos(actor));
    }

    private void autorizarVisita() {
        Formulario f = new Formulario("Autorizar visita o proveedor");
        ComboBox<Lote> lote = f.opciones("Lote", controlador.listarLotesParaAutorizarVisitas(actor));
        ComboBox<Persona> persona = f.opciones("Visitante", controlador.listarPersonas());
        ComboBox<TipoVisitante> tipo = f.opciones("Tipo", Arrays.asList(TipoVisitante.values()));
        TextField inicio = f.texto("Fecha inicio", Fechas.hoy()), fin = f.texto("Fecha fin", Fechas.hoy());
        TextField desde = f.texto("Hora desde (HH:mm)", "08:00"), hasta = f.texto("Hora hasta (HH:mm)", "20:00");
        CheckBox recurrente = f.casilla("Recurrente (proveedor habitual)");
        if (!f.mostrar()) return;
        operar(() -> controlador.autorizarVisita(actor, Formulario.elegido(persona, "un visitante").getDocumento(),
                Formulario.elegido(tipo, "un tipo"), Formulario.elegido(lote, "un lote").getNumero(),
                inicio.getText().trim(), fin.getText().trim(), desde.getText().trim(), hasta.getText().trim(),
                recurrente.isSelected()));
    }

    private void proveedorHabitual() {
        Formulario f = new Formulario("Registrar proveedor habitual");
        ComboBox<Persona> persona = f.opciones("Persona", controlador.listarPersonas());
        if (!f.mostrar()) return;
        operar(() -> controlador.identificarVisitante(actor, Formulario.elegido(persona, "una persona").getDocumento(),
                TipoVisitante.PROVEEDOR, true));
    }

    private void registrarIngreso() {
        Guardia guardia = (Guardia) actor;
        Formulario f = new Formulario("Registrar ingreso");
        TextField documento = f.texto("Documento del visitante", "");
        if (!f.mostrar()) return;
        List<AutorizacionAcceso> vigentes = controlador.buscarAutorizaciones(guardia, documento.getText().trim(), Fechas.ahora());
        if (vigentes.isEmpty()) throw new IllegalArgumentException("No hay autorizaciones vigentes para ese documento.");
        Formulario elegir = new Formulario("Autorización vigente");
        ComboBox<AutorizacionAcceso> autorizacion = elegir.opciones("Autorización", vigentes);
        if (!elegir.mostrar()) return;
        operar(() -> controlador.registrarIngreso(guardia,
                Formulario.elegido(autorizacion, "una autorización").getNumero(), Fechas.ahora()));
    }

    private void registrarEgreso() {
        Formulario f = new Formulario("Registrar egreso");
        TextField documento = f.texto("Documento del visitante", "");
        if (!f.mostrar()) return;
        operar(() -> controlador.registrarEgreso((Guardia) actor, documento.getText().trim(), Fechas.ahora()));
    }
}
