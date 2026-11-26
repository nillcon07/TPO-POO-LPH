package vista.javafx;

import java.util.Arrays;
import java.util.List;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import controlador.GestorBarrioControlador;
import modelo.accesos.*;
import modelo.comun.RelojSesion;
import modelo.comunidad.Lote;
import modelo.personas.*;

// Pestaña de visitas y accesos.
// Administrador: consulta y proveedores habituales. Vecinos: autorizan visitas.
// Guardia: consulta autorizaciones por documento y registra ingresos y egresos.
final class PanelAccesos extends PanelBase {
    private final TableView<AutorizacionAcceso> autorizaciones = Componentes.tabla();
    private final TableView<RegistroAcceso> accesos = Componentes.tabla();
    private final boolean admin;

    PanelAccesos(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        admin = controlador.esAdministrador(actor);

        Componentes.columna(autorizaciones, "N°", AutorizacionAcceso::getNumero);
        Componentes.columna(autorizaciones, "Visitante", AutorizacionAcceso::getPersonaAutorizada);
        Componentes.columna(autorizaciones, "Lote", a -> a.getLoteVisitado().getNumero());
        Componentes.columna(autorizaciones, "Desde", a -> a.getFechaInicio() + " " + a.getHoraDesde());
        Componentes.columna(autorizaciones, "Hasta", a -> a.getFechaFin() + " " + a.getHoraHasta());
        Componentes.columna(accesos, "Visitante", r -> r.getAutorizacion().getPersonaAutorizada());
        Componentes.columna(accesos, "Ingreso", RegistroAcceso::getFechaHoraIngreso);
        Componentes.columna(accesos, "Egreso", r -> r.getFechaHoraEgreso() == null ? "PENDIENTE" : r.getFechaHoraEgreso());

        HBox barra = Componentes.barra();
        if (admin) {
            barra.getChildren().add(Componentes.boton("Registrar proveedor habitual", this::proveedorHabitual));
            setCenter(new VBox(6, new Label("Autorizaciones"), autorizaciones, new Label("Ingresos y egresos"), accesos));
        }
        if (controlador.esVecino(actor))
            barra.getChildren().add(Componentes.boton("Autorizar visita o proveedor", this::autorizarVisita));
        if (actor instanceof Guardia) {
            barra.getChildren().addAll(Componentes.boton("Consultar autorización", this::consultarAutorizacion),
                    Componentes.boton("Registrar ingreso", this::registrarIngreso),
                    Componentes.boton("Registrar egreso", this::registrarEgreso));
        }
        setTop(barra);
        refrescar();
    }

    @Override String titulo() { return "Accesos"; }

    @Override void refrescar() {
        if (!admin) return;
        Componentes.cargar(autorizaciones, controlador.listarAutorizaciones(actor));
        Componentes.cargar(accesos, controlador.listarAccesos(actor));
    }

    private void autorizarVisita() {
        Formulario f = new Formulario("Autorizar visita o proveedor");
        ComboBox<Lote> lote = f.opciones("Lote", controlador.listarLotesParaAutorizarVisitas(actor));
        ComboBox<Persona> persona = f.opciones("Persona", controlador.listarPersonas());
        ComboBox<TipoVisitante> tipo = f.opciones("Tipo", Arrays.asList(TipoVisitante.values()));
        TextField inicio = f.texto("Fecha de inicio", RelojSesion.getInstancia().hoy()), fin = f.texto("Fecha de fin", RelojSesion.getInstancia().hoy());
        TextField desde = f.texto("Hora desde (HH:mm)", ""), hasta = f.texto("Hora hasta (HH:mm)", "");
        CheckBox recurrente = f.casilla("Autorización recurrente");
        if (!f.mostrar()) return;
        operar(() -> controlador.autorizarVisita(actor, Formulario.elegido(persona, "una persona").getDocumento(),
                Formulario.elegido(tipo, "un tipo"), Formulario.elegido(lote, "un lote").getNumero(),
                inicio.getText().trim(), fin.getText().trim(), desde.getText().trim(), hasta.getText().trim(),
                recurrente.isSelected()));
    }

    private void proveedorHabitual() {
        ChoiceDialog<String> modo = new ChoiceDialog<>("Seleccionar persona existente",
                "Seleccionar persona existente", "Registrar nuevo proveedor");
        modo.setTitle("Registrar proveedor habitual");
        modo.setHeaderText(null);
        String eleccion = modo.showAndWait().orElse(null);
        if (eleccion == null) return;

        if (eleccion.startsWith("Seleccionar")) {
            Formulario f = new Formulario("Registrar proveedor habitual");
            ComboBox<Persona> persona = f.opciones("Persona", controlador.listarPersonas());
            if (!f.mostrar()) return;
            operar(() -> controlador.identificarVisitante(actor,
                    Formulario.elegido(persona, "una persona").getDocumento(), TipoVisitante.PROVEEDOR, true));
        } else {
            Formulario f = new Formulario("Registrar nuevo proveedor");
            TextField documento = f.texto("Documento", ""), nombre = f.texto("Nombre", "");
            TextField apellido = f.texto("Apellido", ""), telefono = f.texto("Teléfono", "");
            TextField correo = f.texto("Correo electrónico", "");
            if (!f.mostrar()) return;
            operar(() -> {
                Persona nueva = controlador.agregarPersona(actor, documento.getText(), nombre.getText(),
                        apellido.getText(), telefono.getText(), correo.getText());
                controlador.identificarVisitante(actor, nueva.getDocumento(), TipoVisitante.PROVEEDOR, true);
            });
        }
    }

    private List<AutorizacionAcceso> vigentes(String documento) {
        return controlador.buscarAutorizaciones((Guardia) actor, documento, RelojSesion.getInstancia().ahora());
    }

    private String pedirDocumento(String titulo) {
        Formulario f = new Formulario(titulo);
        TextField documento = f.texto("Documento del visitante", "");
        return f.mostrar() ? documento.getText().trim() : null;
    }

    private void consultarAutorizacion() {
        String documento = pedirDocumento("Consultar autorización");
        if (documento == null) return;
        String texto = "";
        for (AutorizacionAcceso autorizacion : vigentes(documento)) texto += autorizacion + "\n";
        Componentes.texto("Autorizaciones vigentes", texto.isEmpty() ? "Sin registros." : texto);
    }

    private void registrarIngreso() {
        String documento = pedirDocumento("Registrar ingreso");
        if (documento == null) return;
        Formulario f = new Formulario("Registrar ingreso");
        ComboBox<AutorizacionAcceso> autorizacion = f.opciones("Autorización vigente", vigentes(documento));
        if (!f.mostrar()) return;
        operar(() -> Componentes.info(String.valueOf(controlador.registrarIngreso((Guardia) actor,
                Formulario.elegido(autorizacion, "una autorización").getNumero(), RelojSesion.getInstancia().ahora()))));
    }

    private void registrarEgreso() {
        String documento = pedirDocumento("Registrar egreso");
        if (documento == null) return;
        operar(() -> Componentes.info(String.valueOf(
                controlador.registrarEgreso((Guardia) actor, documento, RelojSesion.getInstancia().ahora()))));
    }
}
