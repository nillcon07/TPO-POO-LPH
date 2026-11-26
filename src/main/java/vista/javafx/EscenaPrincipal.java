package vista.javafx;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import controlador.GestorBarrioControlador;
import modelo.comun.RelojSesion;
import modelo.personas.*;

/** Ventana de trabajo: muestra solo las pestañas que corresponden al rol del usuario. */
final class EscenaPrincipal {
    private final AplicacionFX aplicacion;
    private final GestorBarrioControlador controlador;
    private final Persona actor;
    private final List<PanelBase> paneles = new ArrayList<>();
    private final Label estado = new Label();

    EscenaPrincipal(AplicacionFX aplicacion, GestorBarrioControlador controlador, Persona actor) {
        this.aplicacion = aplicacion;
        this.controlador = controlador;
        this.actor = actor;
    }

    static String rol(GestorBarrioControlador controlador, Persona persona) {
        if (controlador.esAdministrador(persona)) return "Administrador";
        if (persona instanceof Guardia) return "Guardia";
        if (controlador.esEmpleadoMantenimiento(persona)) return "Mantenimiento";
        if (controlador.esTitular(persona)) return "Titular";
        if (controlador.esVecino(persona)) return "Residente";
        return "Persona";
    }

    Scene escena() {
        boolean admin = controlador.esAdministrador(actor);
        boolean titular = controlador.esTitular(actor);
        paneles.add(new PanelReclamos(controlador, actor));
        if (admin) paneles.add(new PanelComunidad(controlador, actor));
        if (titular) paneles.add(new PanelMisLotes(controlador, actor));
        if (admin || titular) paneles.add(new PanelExpensas(controlador, actor));
        paneles.add(new PanelMantenimiento(controlador, actor));
        if (admin || actor instanceof Guardia || controlador.esVecino(actor))
            paneles.add(new PanelAccesos(controlador, actor));
        if (admin) paneles.add(new PanelReportes(controlador, actor));

        TabPane pestanas = new TabPane();
        for (PanelBase panel : paneles) {
            Tab pestana = new Tab(panel.titulo(), panel);
            pestana.setOnSelectionChanged(evento -> {
                if (pestana.isSelected()) Componentes.ejecutar(panel::refrescar);
            });
            pestanas.getTabs().add(pestana);
        }

        HBox barraEstado = new HBox(estado);
        barraEstado.setPadding(new Insets(4, 8, 4, 8));

        BorderPane raiz = new BorderPane(pestanas);
        raiz.setTop(menu());
        raiz.setBottom(barraEstado);
        actualizarEstado();
        return new Scene(raiz, 1000, 640);
    }

    private MenuBar menu() {
        Menu sesion = new Menu("Sesión");
        sesion.getItems().addAll(
                opcion("Cambiar usuario", aplicacion::mostrarIngreso),
                opcion("Cambiar fecha y hora...", this::cambiarFecha),
                opcion("Verificar plazos y alertas", this::verificarControles),
                new SeparatorMenuItem(),
                opcion("Guardar", () -> {
                    controlador.guardar();
                    Componentes.info("Datos guardados correctamente.");
                }),
                opcion("Salir", Platform::exit));
        return new MenuBar(sesion);
    }

    private static MenuItem opcion(String texto, Runnable accion) {
        MenuItem item = new MenuItem(texto);
        item.setOnAction(evento -> Componentes.ejecutar(accion));
        return item;
    }

    private void cambiarFecha() {
        Formulario f = new Formulario("Fecha y hora de la sesión");
        TextField nueva = f.texto("Fecha y hora (aaaa-MM-dd HH:mm)", RelojSesion.getInstancia().ahora());
        if (!f.mostrar()) return;
        String valor = nueva.getText().trim();
        RelojSesion.getInstancia().avanzar(valor);
        verificarControles();
    }

    private void verificarControles() {
        controlador.verificarControles(RelojSesion.getInstancia().ahora());
        actualizarEstado();
        for (PanelBase panel : paneles) panel.refrescar();
    }

    private void actualizarEstado() {
        estado.setText("Usuario: " + actor + " (" + rol(controlador, actor) + ")     Fecha: " + RelojSesion.getInstancia().ahora());
    }
}
