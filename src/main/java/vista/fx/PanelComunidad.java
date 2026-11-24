package vista.fx;

import java.util.Arrays;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import controlador.GestorBarrioControlador;
import modelo.comun.Fechas;
import modelo.comunidad.Lote;
import modelo.personas.*;

/** Comunidad y personal: solo se muestra al administrador (y el controlador lo vuelve a exigir). */
final class PanelComunidad extends PanelBase {
    private final TableView<Lote> lotes = Componentes.tabla();
    private final TableView<Personal> personal = Componentes.tabla();

    PanelComunidad(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        Componentes.columna(lotes, "Lote", Lote::getNumero);
        Componentes.columna(lotes, "Superficie m²", Lote::getSuperficie);
        Componentes.columna(lotes, "Titular", Lote::getTitular);
        Componentes.columna(lotes, "Residentes", Lote::getResidentes);
        Componentes.columna(lotes, "Autorizados a reclamar", Lote::getPersonasAutorizadasReclamo);
        Componentes.columna(personal, "Legajo", Personal::getLegajo);
        Componentes.columna(personal, "Rol", p -> p.getClass().getSimpleName());
        Componentes.columna(personal, "Persona", p -> p);
        Componentes.columna(personal, "Ingreso", Personal::getFechaIngreso);

        setTop(Componentes.barra(
                Componentes.boton("Registrar persona", this::registrarPersona),
                Componentes.boton("Agregar lote", this::agregarLote),
                Componentes.boton("Agregar residente", this::agregarResidente),
                Componentes.boton("Quitar residente", this::quitarResidente),
                Componentes.boton("Cambiar titular", this::cambiarTitular),
                Componentes.boton("Registrar guardia", this::agregarGuardia),
                Componentes.boton("Registrar técnico", this::agregarEmpleado)));
        setCenter(new VBox(8, new Label("Lotes"), lotes, new Label("Personal"), personal));
        refrescar();
    }

    @Override String titulo() { return "Comunidad"; }

    @Override void refrescar() {
        Componentes.cargar(lotes, controlador.listarLotes(actor));
        Componentes.cargar(personal, controlador.listarPersonal(actor));
    }

    private void registrarPersona() {
        Formulario f = new Formulario("Registrar persona");
        TextField documento = f.texto("Documento", "");
        TextField nombre = f.texto("Nombre", "");
        TextField apellido = f.texto("Apellido", "");
        TextField telefono = f.texto("Teléfono", "");
        TextField correo = f.texto("Correo", "");
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarPersona(actor, documento.getText(), nombre.getText(), apellido.getText(),
                telefono.getText(), correo.getText()));
    }

    private void agregarLote() {
        Formulario f = new Formulario("Agregar lote");
        TextField numero = f.texto("Número", "");
        TextField superficie = f.texto("Superficie m²", "");
        ComboBox<Persona> titular = f.opciones("Titular", controlador.listarPersonas());
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarLote(actor, Componentes.entero(numero.getText(), "Número"),
                Componentes.decimal(superficie.getText(), "Superficie"),
                Formulario.elegido(titular, "un titular").getDocumento()));
    }

    private void agregarResidente() {
        Lote lote = Componentes.seleccionado(lotes, "un lote");
        Formulario f = new Formulario("Agregar residente al lote " + lote.getNumero());
        ComboBox<Persona> persona = f.opciones("Persona", controlador.listarPersonas());
        ComboBox<TipoResidente> tipo = f.opciones("Tipo", Arrays.asList(TipoResidente.values()));
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarResidente(actor, lote.getNumero(),
                Formulario.elegido(persona, "una persona").getDocumento(), Formulario.elegido(tipo, "un tipo")));
    }

    private void quitarResidente() {
        Lote lote = Componentes.seleccionado(lotes, "un lote");
        Formulario f = new Formulario("Quitar residente del lote " + lote.getNumero());
        ComboBox<Residente> residente = f.opciones("Residente", lote.getResidentes());
        if (!f.mostrar()) return;
        operar(() -> controlador.quitarResidente(actor, lote.getNumero(),
                Formulario.elegido(residente, "un residente").getDocumento()));
    }

    private void cambiarTitular() {
        Lote lote = Componentes.seleccionado(lotes, "un lote");
        Formulario f = new Formulario("Cambiar titular del lote " + lote.getNumero());
        ComboBox<Persona> titular = f.opciones("Nuevo titular", controlador.listarPersonas());
        if (!f.mostrar()) return;
        operar(() -> controlador.cambiarTitular(actor, lote.getNumero(),
                Formulario.elegido(titular, "un titular").getDocumento()));
    }

    private void agregarGuardia() {
        Formulario f = new Formulario("Registrar guardia");
        ComboBox<Persona> persona = f.opciones("Persona", controlador.listarPersonas());
        TextField legajo = f.texto("Legajo", "");
        TextField fecha = f.texto("Fecha de ingreso", Fechas.hoy());
        ComboBox<Turno> turno = f.opciones("Turno", Arrays.asList(Turno.values()));
        ComboBox<PuestoGuardia> puesto = f.opciones("Puesto", Arrays.asList(PuestoGuardia.values()));
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarGuardia(actor, Formulario.elegido(persona, "una persona").getDocumento(),
                legajo.getText(), fecha.getText(), Formulario.elegido(turno, "un turno"),
                Formulario.elegido(puesto, "un puesto")));
    }

    private void agregarEmpleado() {
        Formulario f = new Formulario("Registrar técnico de mantenimiento");
        ComboBox<Persona> persona = f.opciones("Persona", controlador.listarPersonas());
        TextField legajo = f.texto("Legajo", "");
        TextField fecha = f.texto("Fecha de ingreso", Fechas.hoy());
        ComboBox<Especialidad> especialidad = f.opciones("Especialidad", Arrays.asList(Especialidad.values()));
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarEmpleado(actor, Formulario.elegido(persona, "una persona").getDocumento(),
                legajo.getText(), fecha.getText(), Formulario.elegido(especialidad, "una especialidad")));
    }
}
