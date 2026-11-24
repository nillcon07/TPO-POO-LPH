package vista.javafx;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import controlador.GestorBarrioControlador;
import modelo.comun.Fechas;
import modelo.personas.Persona;

/** Pantalla inicial: se elige el usuario y la fecha y hora de la sesión. */
final class VistaIngreso {
    private final AplicacionFX aplicacion;
    private final GestorBarrioControlador controlador;

    VistaIngreso(AplicacionFX aplicacion, GestorBarrioControlador controlador) {
        this.aplicacion = aplicacion;
        this.controlador = controlador;
    }

    Scene escena() {
        ComboBox<Persona> actor = new ComboBox<>(FXCollections.observableArrayList(controlador.listarActores()));
        actor.setConverter(new StringConverter<>() {
            @Override public String toString(Persona persona) {
                return persona == null ? "" : persona + " - " + VentanaPrincipal.rol(controlador, persona);
            }
            @Override public Persona fromString(String texto) { return null; }
        });
        actor.setPrefWidth(400);
        actor.getSelectionModel().selectFirst();

        TextField fecha = new TextField(fechaInicial());

        Button ingresar = Componentes.boton("Ingresar", () -> {
            Persona elegido = Formulario.elegido(actor, "un usuario");
            Fechas.establecerMomento(fecha.getText().trim());
            controlador.verificarControles(Fechas.ahora());
            aplicacion.mostrarPrincipal(elegido);
        });
        ingresar.setDefaultButton(true);

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.setPadding(new Insets(20));
        grilla.add(new Label("Usuario:"), 0, 0);
        grilla.add(actor, 1, 0);
        grilla.add(new Label("Fecha y hora:"), 0, 1);
        grilla.add(fecha, 1, 1);
        grilla.add(new Label("(aaaa-MM-dd HH:mm)"), 1, 2);
        grilla.add(ingresar, 1, 3);
        return new Scene(grilla);
    }

    // Si ya había una sesión abierta se conserva su fecha; si no, se propone la del sistema.
    private static String fechaInicial() {
        try {
            return Fechas.ahora();
        } catch (IllegalArgumentException e) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }
}
