package vista.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import controlador.GestorBarrioControlador;
import modelo.personas.Persona;
import principal.CreadorControlador;

/** Vista gráfica del sistema. Usa el mismo controlador que la versión de consola. */
public class AplicacionFX extends Application {
    private GestorBarrioControlador controlador;
    private Stage ventana;

    @Override
    public void start(Stage ventana) {
        this.ventana = ventana;
        try {
            controlador = CreadorControlador.crearControlador(CreadorControlador.RUTA_DATOS);
        } catch (RuntimeException e) {
            Componentes.error("No se pudo iniciar Los Aromos: " + e.getMessage());
            Platform.exit();
            return;
        }
        ventana.setTitle("Los Aromos");
        mostrarIngreso();
        ventana.show();
    }

    void mostrarIngreso() {
        ventana.setScene(new VistaIngreso(this, controlador).escena());
        ventana.sizeToScene();
        ventana.centerOnScreen();
    }

    void mostrarPrincipal(Persona actor) {
        ventana.setScene(new VentanaPrincipal(this, controlador, actor).escena());
        ventana.sizeToScene();
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
