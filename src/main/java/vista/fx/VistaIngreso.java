package vista.fx;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import controlador.GestorBarrioControlador;
import modelo.comun.Fechas;
import modelo.personas.Persona;

/** Pantalla inicial: se elige el actor de la sesión y la fecha y hora simulada. */
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
                return persona == null ? "" : persona + " — " + VentanaPrincipal.rol(controlador, persona);
            }
            @Override public Persona fromString(String texto) { return null; }
        });
        actor.setMaxWidth(Double.MAX_VALUE);
        actor.getSelectionModel().selectFirst();

        TextField fecha = new TextField(fechaInicial());

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.add(new Label("Usuario:"), 0, 0);
        grilla.add(actor, 1, 0);
        grilla.add(new Label("Fecha y hora (aaaa-MM-dd HH:mm):"), 0, 1);
        grilla.add(fecha, 1, 1);

        Button ingresar = Componentes.boton("Ingresar", () -> {
            Persona elegido = Formulario.elegido(actor, "un usuario");
            Fechas.establecerMomento(fecha.getText().trim());
            controlador.verificarControles(Fechas.ahora());
            aplicacion.mostrarPrincipal(elegido);
        });
        ingresar.setDefaultButton(true);

        Label titulo = new Label("Barrio " + controlador.getNombreBarrio());
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        VBox raiz = new VBox(16, titulo, new Label("Seleccione con qué usuario trabajar."), grilla, ingresar);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(30));
        return new Scene(raiz, 620, 320);
    }

    /** Si ya hay una sesión se conserva su fecha (no se permite retroceder); si no, se propone la del sistema. */
    private static String fechaInicial() {
        try {
            return Fechas.ahora();
        } catch (IllegalArgumentException e) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }
}
