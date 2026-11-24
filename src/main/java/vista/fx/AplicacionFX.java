package vista.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import controlador.GestorBarrioControlador;
import modelo.personas.Persona;
import principal.Arranque;

/**
 * Vista gráfica (JavaFX) del patrón MVC. Usa el mismo controlador que la consola:
 * las reglas y los permisos no cambian al cambiar de interfaz.
 */
public class AplicacionFX extends Application {
    private GestorBarrioControlador controlador;
    private Stage ventana;

    @Override
    public void start(Stage ventana) {
        this.ventana = ventana;
        try {
            controlador = Arranque.crearControlador(Arranque.RUTA_DATOS);
        } catch (RuntimeException e) {
            Componentes.error("No se pudo iniciar Los Aromos: " + e.getMessage());
            Platform.exit();
            return;
        }
        ventana.setTitle("Los Aromos — Gestión del barrio");
        mostrarIngreso();
        ventana.show();
    }

    void mostrarIngreso() {
        ventana.setScene(new VistaIngreso(this, controlador).escena());
        ventana.centerOnScreen();
    }

    void mostrarPrincipal(Persona actor) {
        ventana.setScene(new VentanaPrincipal(this, controlador, actor).escena());
        ventana.centerOnScreen();
    }

    /** Al cerrar la ventana se guardan los datos, igual que al salir de la consola. */
    @Override
    public void stop() {
        if (controlador == null) return;
        try {
            controlador.guardar();
        } catch (RuntimeException e) {
            System.err.println("No se pudieron guardar los datos al salir: " + e.getMessage());
        }
    }
}
