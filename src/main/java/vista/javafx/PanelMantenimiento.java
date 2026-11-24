package vista.javafx;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import controlador.GestorBarrioControlador;
import modelo.comun.Fechas;
import modelo.mantenimiento.*;
import modelo.personas.*;

/**
 * Pestaña de equipos y mantenimiento.
 * Administrador: equipos, fallas y órdenes. Técnico: sus órdenes para cerrar.
 * Vecinos y guardias: informar fallas.
 */
final class PanelMantenimiento extends PanelBase {
    private final TableView<Equipo> equipos = Componentes.tabla();
    private final TableView<Falla> fallas = Componentes.tabla();
    private final TableView<OrdenMantenimiento> ordenes = Componentes.tabla();
    private final boolean admin;
    private final boolean tecnico;

    PanelMantenimiento(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        admin = controlador.esAdministrador(actor);
        tecnico = controlador.esEmpleadoMantenimiento(actor);

        Componentes.columna(equipos, "Código", Equipo::getCodigo);
        Componentes.columna(equipos, "Tipo", Equipo::getTipoEquipo);
        Componentes.columna(equipos, "Sector", Equipo::getSector);
        Componentes.columna(equipos, "Estado", Equipo::getEstado);
        Componentes.columna(fallas, "Falla", Falla::getNumero);
        Componentes.columna(fallas, "Equipo", f -> f.getEquipo().getCodigo());
        Componentes.columna(fallas, "Fecha", Falla::getFechaReporte);
        Componentes.columna(fallas, "Descripción", Falla::getDescripcion);
        Componentes.columna(ordenes, "Orden", OrdenMantenimiento::getNumero);
        Componentes.columna(ordenes, "Equipo", o -> o.getEquipoAfectado().getCodigo());
        Componentes.columna(ordenes, "Prioridad", OrdenMantenimiento::getPrioridad);
        Componentes.columna(ordenes, "Trabajo", OrdenMantenimiento::getDescripcionTrabajo);
        Componentes.columna(ordenes, "Técnico", OrdenMantenimiento::getEmpleadoAsignado);
        Componentes.columna(ordenes, "Reparación", OrdenMantenimiento::getDetalleReparacion);

        HBox barra = Componentes.barra();
        VBox centro = new VBox(6);
        if (admin) {
            barra.getChildren().addAll(Componentes.boton("Agregar cámara", this::agregarCamara),
                    Componentes.boton("Agregar luminaria", this::agregarLuminaria),
                    Componentes.boton("Generar orden", this::generarOrden),
                    Componentes.boton("Asignar técnico", this::asignarTecnico),
                    Componentes.boton("Cerrar orden", this::cerrarOrden),
                    Componentes.boton("Marcar fuera de servicio", this::fueraDeServicio));
            centro.getChildren().addAll(new Label("Equipos"), equipos, new Label("Fallas"), fallas,
                    new Label("Órdenes"), ordenes);
        } else if (tecnico) {
            barra.getChildren().add(Componentes.boton("Cerrar orden", this::cerrarOrden));
            centro.getChildren().addAll(new Label("Órdenes asignadas"), ordenes);
        } else {
            barra.getChildren().add(Componentes.boton("Informar falla", this::informarFalla));
            centro.getChildren().addAll(new Label("Equipos"), equipos);
        }
        setTop(barra);
        setCenter(centro);
        refrescar();
    }

    @Override String titulo() { return "Mantenimiento"; }

    @Override void refrescar() {
        if (admin) {
            Componentes.cargar(equipos, controlador.listarEquipos());
            Componentes.cargar(fallas, controlador.listarFallas(actor));
            Componentes.cargar(ordenes, controlador.listarOrdenes(actor));
        } else if (tecnico) {
            Componentes.cargar(ordenes, controlador.listarOrdenesCerrables(actor));
        } else {
            Componentes.cargar(equipos, controlador.listarEquipos());
        }
    }

    private void informarFalla() {
        Equipo equipo = Componentes.seleccionado(equipos, "un equipo");
        Formulario f = new Formulario("Informar falla - " + equipo.getCodigo());
        TextField inicio = f.texto("Inicio (aaaa-MM-dd HH:mm, vacío si se desconoce)", "");
        TextArea descripcion = f.area("Descripción");
        if (!f.mostrar()) return;
        operar(() -> controlador.registrarFalla(actor, equipo.getCodigo(), Fechas.ahora(), inicio.getText().trim(),
                descripcion.getText()));
    }

    private void agregarCamara() {
        Formulario f = new Formulario("Agregar cámara");
        TextField codigo = f.texto("Código", ""), sector = f.texto("Sector", "");
        TextField fecha = f.texto("Fecha de instalación", Fechas.hoy()), resolucion = f.texto("Resolución", "");
        TextField dias = f.texto("Días de grabación", "");
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarCamara(actor, codigo.getText(), sector.getText(), fecha.getText(),
                resolucion.getText(), Componentes.entero(dias.getText(), "Días de grabación")));
    }

    private void agregarLuminaria() {
        Formulario f = new Formulario("Agregar luminaria");
        TextField codigo = f.texto("Código", ""), sector = f.texto("Sector", "");
        TextField fecha = f.texto("Fecha de instalación", Fechas.hoy()), lampara = f.texto("Tipo de lámpara", "");
        TextField potencia = f.texto("Potencia W", "");
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarLuminaria(actor, codigo.getText(), sector.getText(), fecha.getText(),
                lampara.getText(), Componentes.decimal(potencia.getText(), "Potencia")));
    }

    private void fueraDeServicio() {
        Equipo equipo = Componentes.seleccionado(equipos, "un equipo");
        operar(() -> controlador.marcarEquipoFueraServicio(actor, equipo.getCodigo()));
    }

    private void generarOrden() {
        Falla falla = Componentes.seleccionado(fallas, "una falla");
        Formulario f = new Formulario("Generar orden - Falla " + falla.getNumero());
        TextArea trabajo = f.area("Trabajo a realizar");
        if (!f.mostrar()) return;
        operar(() -> controlador.generarOrden(actor, falla.getNumero(), Fechas.ahora(), trabajo.getText()));
    }

    private void asignarTecnico() {
        OrdenMantenimiento orden = Componentes.seleccionado(ordenes, "una orden");
        Formulario f = new Formulario("Asignar técnico - Orden " + orden.getNumero());
        ComboBox<EmpleadoMantenimiento> tecnicoElegido = f.opciones("Técnico", controlador.listarTecnicos(actor));
        if (!f.mostrar()) return;
        operar(() -> controlador.asignarOrden(actor, orden.getNumero(),
                Formulario.elegido(tecnicoElegido, "un técnico").getLegajo()));
    }

    private void cerrarOrden() {
        OrdenMantenimiento orden = Componentes.seleccionado(ordenes, "una orden");
        Formulario f = new Formulario("Cerrar orden " + orden.getNumero());
        TextArea detalle = f.area("Detalle de reparación");
        if (!f.mostrar()) return;
        operar(() -> controlador.cerrarOrden(actor, orden.getNumero(), Fechas.ahora(), detalle.getText()));
    }
}
