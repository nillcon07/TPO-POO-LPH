package vista.fx;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import controlador.GestorBarrioControlador;
import modelo.comun.Fechas;
import modelo.personas.*;

/**
 * Ventana de trabajo. Las pestañas se arman según el rol del actor: así cada usuario solo ve
 * lo que puede usar. Igualmente, cada operación vuelve a validarse en el controlador y el modelo.
 */
final class VentanaPrincipal {
    private final AplicacionFX aplicacion;
    private final GestorBarrioControlador controlador;
    private final Persona actor;
    private final List<PanelBase> paneles = new ArrayList<>();
    private final Label fecha = new Label();

    VentanaPrincipal(AplicacionFX aplicacion, GestorBarrioControlador controlador, Persona actor) {
        this.aplicacion = aplicacion;
        this.controlador = controlador;
        this.actor = actor;
    }

    static String rol(GestorBarrioControlador controlador, Persona persona) {
        if (controlador.esAdministrador(persona)) return "Administrador";
        if (persona instanceof Guardia) return "Guardia de " + ((Guardia) persona).getPuestoAsignado();
        if (controlador.esEmpleadoMantenimiento(persona)) return "Mantenimiento";
        if (controlador.esTitular(persona)) return "Titular";
        if (controlador.esVecino(persona)) return "Residente";
        return "Persona";
    }

    Scene escena() {
        boolean admin = controlador.esAdministrador(actor);
        paneles.add(new PanelReclamos(controlador, actor));
        if (admin) paneles.add(new PanelComunidad(controlador, actor));
        if (controlador.esTitular(actor)) paneles.add(new PanelMisLotes(controlador, actor));
        if (admin || controlador.esTitular(actor)) paneles.add(new PanelExpensas(controlador, actor));
        paneles.add(new PanelMantenimiento(controlador, actor));
        if (admin || controlador.esGuardiaPorteria(actor) || controlador.esVecino(actor))
            paneles.add(new PanelAccesos(controlador, actor));
        if (admin) paneles.add(new PanelReportes(controlador, actor));

        TabPane pestanas = new TabPane();
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        for (PanelBase panel : paneles) {
            Tab pestana = new Tab(panel.titulo(), panel);
            pestana.setOnSelectionChanged(evento -> {
                if (pestana.isSelected()) Componentes.ejecutar(panel::refrescar);
            });
            pestanas.getTabs().add(pestana);
        }

        BorderPane raiz = new BorderPane(pestanas);
        raiz.setTop(encabezado());
        actualizarFecha();
        return new Scene(raiz, 1100, 680);
    }

    private HBox encabezado() {
        Label usuario = new Label(actor + " — " + rol(controlador, actor));
        usuario.setStyle("-fx-font-weight: bold;");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barra = new HBox(10, new Label("Barrio " + controlador.getNombreBarrio() + " |"), usuario, espacio, fecha,
                Componentes.boton("Avanzar fecha", this::avanzarFecha),
                Componentes.boton("Guardar", () -> { controlador.guardar(); Componentes.info("Datos guardados."); }),
                Componentes.boton("Cambiar usuario", aplicacion::mostrarIngreso));
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(8, 10, 8, 10));
        barra.setStyle("-fx-background-color: #e8eef4;");
        return barra;
    }

    private void avanzarFecha() {
        Formulario f = new Formulario("Avanzar fecha y hora de la sesión");
        TextField nueva = f.texto("Fecha y hora (aaaa-MM-dd HH:mm)", Fechas.ahora());
        if (!f.mostrar()) return;
        String valor = nueva.getText().trim();
        Fechas.noAnterior(Fechas.ahora(), valor);
        Fechas.establecerMomento(valor);
        controlador.verificarControles(Fechas.ahora());
        actualizarFecha();
        for (PanelBase panel : paneles) panel.refrescar();
    }

    private void actualizarFecha() {
        fecha.setText("Sesión: " + Fechas.ahora());
    }
}
