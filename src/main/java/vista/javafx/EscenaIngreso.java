package vista.javafx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import controlador.GestorBarrioControlador;
import modelo.comun.RelojSesion;
import modelo.personas.Persona;

/** Pantalla inicial: se elige el usuario y la fecha y hora de la sesión. */
final class EscenaIngreso {
    private final AplicacionFX aplicacion;
    private final GestorBarrioControlador controlador;

    EscenaIngreso(AplicacionFX aplicacion, GestorBarrioControlador controlador) {
        this.aplicacion = aplicacion;
        this.controlador = controlador;
    }

    Scene escena() {
        ComboBox<Persona> actor = new ComboBox<>(FXCollections.observableArrayList(controlador.listarActores()));
        actor.setPrefWidth(400);

        RelojSesion reloj = RelojSesion.getInstancia();
        boolean sesionIniciada = reloj.tieneMomento();
        TextField fecha = new TextField(fechaInicial());
        fecha.setDisable(sesionIniciada);

        Button ingresar = Componentes.boton("Ingresar", () -> {
            Persona elegido = Formulario.elegido(actor, "un usuario");
            if (!sesionIniciada) reloj.establecer(fecha.getText().trim());
            controlador.verificarControles(reloj.ahora());
            aplicacion.mostrarPrincipal(elegido);
        });

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.setPadding(new Insets(20));
        grilla.add(new Label("Usuario:"), 0, 0);
        grilla.add(actor, 1, 0);
        grilla.add(new Label("Fecha y hora:"), 0, 1);
        grilla.add(fecha, 1, 1);
        grilla.add(new Label(sesionIniciada
                ? "(se cambia desde Sesión > Cambiar fecha y hora)"
                : "(aaaa-MM-dd HH:mm)"), 1, 2);
        grilla.add(ingresar, 1, 3);
        return new Scene(grilla);
    }

    // Si ya había una sesión abierta se muestra su fecha; si no, el campo queda vacío para ingresarla.
    private static String fechaInicial() {
        RelojSesion reloj = RelojSesion.getInstancia();
        if (reloj.tieneMomento()) return reloj.ahora();
        return "";
    }
}
