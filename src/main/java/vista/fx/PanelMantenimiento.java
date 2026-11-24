package vista.fx;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import controlador.GestorBarrioControlador;
import modelo.comun.Fechas;
import modelo.mantenimiento.*;
import modelo.personas.*;

/** Equipos y mantenimiento: vecinos y guardias informan fallas; administración y técnicos gestionan órdenes. */
final class PanelMantenimiento extends PanelBase {
    private final TableView<Equipo> equipos = Componentes.tabla();
    private final TableView<OrdenMantenimiento> ordenes = Componentes.tabla();
    private final boolean gestionaOrdenes;

    PanelMantenimiento(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        boolean admin = controlador.esAdministrador(actor);
        gestionaOrdenes = admin || controlador.esEmpleadoMantenimiento(actor);

        Componentes.columna(equipos, "Código", Equipo::getCodigo);
        Componentes.columna(equipos, "Tipo", Equipo::getTipoEquipo);
        Componentes.columna(equipos, "Sector", Equipo::getSector);
        Componentes.columna(equipos, "Estado", Equipo::getEstado);
        Componentes.columna(ordenes, "Orden", OrdenMantenimiento::getNumero);
        Componentes.columna(ordenes, "Equipo", o -> o.getEquipoAfectado().getCodigo());
        Componentes.columna(ordenes, "Prioridad", OrdenMantenimiento::getPrioridad);
        Componentes.columna(ordenes, "Trabajo", OrdenMantenimiento::getDescripcionTrabajo);
        Componentes.columna(ordenes, "Técnico", OrdenMantenimiento::getEmpleadoAsignado);
        Componentes.columna(ordenes, "Cierre", OrdenMantenimiento::getFechaCierre);

        HBox barra = Componentes.barra();
        if (controlador.esVecino(actor) || actor instanceof Guardia)
            barra.getChildren().add(Componentes.boton("Informar falla", this::informarFalla));
        if (admin) {
            barra.getChildren().addAll(Componentes.boton("Agregar luminaria", this::agregarLuminaria),
                    Componentes.boton("Agregar cámara", this::agregarCamara),
                    Componentes.boton("Fuera de servicio", this::fueraDeServicio),
                    Componentes.boton("Generar orden", this::generarOrden),
                    Componentes.boton("Asignar técnico", this::asignarTecnico));
        }
        if (gestionaOrdenes) barra.getChildren().add(Componentes.boton("Cerrar orden", this::cerrarOrden));
        setTop(barra);

        VBox centro = new VBox(8, new Label("Equipos"), equipos);
        if (gestionaOrdenes) centro.getChildren().addAll(new Label("Órdenes de mantenimiento"), ordenes);
        setCenter(centro);
        refrescar();
    }

    @Override String titulo() { return "Mantenimiento"; }

    @Override void refrescar() {
        Componentes.cargar(equipos, controlador.listarEquipos());
        if (gestionaOrdenes) Componentes.cargar(ordenes, controlador.listarOrdenes(actor));
    }

    private void informarFalla() {
        Equipo equipo = Componentes.seleccionado(equipos, "un equipo");
        Formulario f = new Formulario("Informar falla en " + equipo.getCodigo());
        TextField inicio = f.texto("Inicio (aaaa-MM-dd HH:mm, opcional)", "");
        TextArea descripcion = f.area("Descripción");
        if (!f.mostrar()) return;
        operar(() -> controlador.registrarFalla(actor, equipo.getCodigo(), Fechas.ahora(), inicio.getText().trim(),
                descripcion.getText()));
    }

    private void agregarLuminaria() {
        Formulario f = new Formulario("Agregar luminaria");
        TextField codigo = f.texto("Código", ""), sector = f.texto("Sector", "");
        TextField fecha = f.texto("Instalación", Fechas.hoy()), lampara = f.texto("Lámpara", "LED");
        TextField potencia = f.texto("Potencia W", "");
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarLuminaria(actor, codigo.getText(), sector.getText(), fecha.getText(),
                lampara.getText(), Componentes.decimal(potencia.getText(), "Potencia")));
    }

    private void agregarCamara() {
        Formulario f = new Formulario("Agregar cámara");
        TextField codigo = f.texto("Código", ""), sector = f.texto("Sector", "");
        TextField fecha = f.texto("Instalación", Fechas.hoy()), resolucion = f.texto("Resolución", "1920x1080");
        TextField dias = f.texto("Días de grabación", "30");
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarCamara(actor, codigo.getText(), sector.getText(), fecha.getText(),
                resolucion.getText(), Componentes.entero(dias.getText(), "Días de grabación")));
    }

    private void fueraDeServicio() {
        Equipo equipo = Componentes.seleccionado(equipos, "un equipo");
        operar(() -> controlador.marcarEquipoFueraServicio(actor, equipo.getCodigo()));
    }

    private void generarOrden() {
        List<Falla> sinOrden = new ArrayList<>();
        for (Falla falla : controlador.listarFallas(actor)) if (falla.getOrden() == null) sinOrden.add(falla);
        if (sinOrden.isEmpty()) throw new IllegalArgumentException("No hay fallas pendientes de orden.");
        Formulario f = new Formulario("Generar orden de mantenimiento");
        ComboBox<Falla> falla = f.opciones("Falla", sinOrden);
        TextArea trabajo = f.area("Trabajo a realizar");
        if (!f.mostrar()) return;
        operar(() -> controlador.generarOrden(actor, Formulario.elegido(falla, "una falla").getNumero(),
                Fechas.ahora(), trabajo.getText()));
    }

    private void asignarTecnico() {
        OrdenMantenimiento orden = Componentes.seleccionado(ordenes, "una orden");
        Formulario f = new Formulario("Asignar técnico a la orden " + orden.getNumero());
        ComboBox<EmpleadoMantenimiento> tecnico = f.opciones("Técnico", controlador.listarTecnicos(actor));
        if (!f.mostrar()) return;
        operar(() -> controlador.asignarOrden(actor, orden.getNumero(), Formulario.elegido(tecnico, "un técnico").getLegajo()));
    }

    private void cerrarOrden() {
        OrdenMantenimiento orden = Componentes.seleccionado(ordenes, "una orden");
        Formulario f = new Formulario("Cerrar orden " + orden.getNumero());
        TextArea detalle = f.area("Detalle de la reparación");
        if (!f.mostrar()) return;
        operar(() -> controlador.cerrarOrden(actor, orden.getNumero(), Fechas.ahora(), detalle.getText()));
    }
}
