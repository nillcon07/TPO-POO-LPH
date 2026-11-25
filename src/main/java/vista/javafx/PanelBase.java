package vista.javafx;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import controlador.GestorBarrioControlador;
import modelo.personas.Persona;

/**
 * Pestaña de la ventana principal. Cada subclase arma sus controles y sabe refrescarse:
 * la ventana las trata a todas igual (polimorfismo) sin conocer su contenido.
 */
abstract class PanelBase extends BorderPane {
    protected final GestorBarrioControlador controlador;
    protected final Persona actor;

    protected PanelBase(GestorBarrioControlador controlador, Persona actor) {
        this.controlador = controlador;
        this.actor = actor;
        setPadding(new Insets(10));
    }

    abstract String titulo();

    /** Vuelve a leer los datos desde el controlador. */
    abstract void refrescar();

    /** Ejecuta una operación del usuario; si el modelo la rechaza se informa y no se refresca. */
    protected void operar(Runnable operacion) {
        Componentes.ejecutar(() -> {
            operacion.run();
            refrescar();
        });
    }
}
