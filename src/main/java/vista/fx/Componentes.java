package vista.fx;

import java.util.List;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/** Utilidades de la vista: tablas, botones y mensajes. No contiene reglas de negocio. */
final class Componentes {
    private Componentes() { }

    static <T> TableView<T> tabla() {
        TableView<T> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("Sin registros."));
        return tabla;
    }

    /** Agrega una columna cuyo texto se obtiene del objeto de la fila (función lambda). */
    static <T> void columna(TableView<T> tabla, String titulo, Function<T, Object> valor) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(celda -> {
            Object dato = valor.apply(celda.getValue());
            return new SimpleStringProperty(dato == null ? "" : String.valueOf(dato));
        });
        tabla.getColumns().add(columna);
    }

    /**
     * Recarga la tabla. refresh() es necesario porque las filas son los mismos objetos del modelo
     * (no observables): sin él, JavaFX no vuelve a dibujar las celdas que cambiaron por dentro.
     */
    static <T> void cargar(TableView<T> tabla, List<T> elementos) {
        tabla.setItems(FXCollections.observableArrayList(elementos));
        tabla.refresh();
    }

    /** Devuelve la fila seleccionada o lanza un error legible si no hay selección. */
    static <T> T seleccionado(TableView<T> tabla, String que) {
        T elegido = tabla.getSelectionModel().getSelectedItem();
        if (elegido == null) throw new IllegalArgumentException("Seleccione " + que + " en la tabla.");
        return elegido;
    }

    static Button boton(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.setOnAction(evento -> ejecutar(accion));
        return boton;
    }

    static HBox barra(Button... botones) {
        HBox barra = new HBox(8, botones);
        barra.setPadding(new Insets(8, 0, 8, 0));
        return barra;
    }

    /** Ejecuta una acción y muestra los rechazos del modelo como diálogo de error. */
    static void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException e) {
            error(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    static void error(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    static void info(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alerta.setTitle("Los Aromos");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    static void texto(String titulo, String contenido) {
        TextArea area = new TextArea(contenido);
        area.setEditable(false);
        area.setPrefSize(600, 400);
        Alert alerta = new Alert(Alert.AlertType.NONE, "", ButtonType.CLOSE);
        alerta.setTitle(titulo);
        alerta.getDialogPane().setContent(area);
        alerta.setResizable(true);
        alerta.showAndWait();
    }

    static int entero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " debe ser un número entero.");
        }
    }

    static double decimal(String texto, String campo) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " debe ser un número.");
        }
    }
}
