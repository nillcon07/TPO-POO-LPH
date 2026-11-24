package vista.fx;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/** Diálogo con campos etiquetados. Solo captura datos: la validación la hace el modelo. */
final class Formulario {
    private final Dialog<ButtonType> dialogo = new Dialog<>();
    private final GridPane grilla = new GridPane();
    private int fila;

    Formulario(String titulo) {
        dialogo.setTitle(titulo);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        grilla.setHgap(10);
        grilla.setVgap(8);
        grilla.setPadding(new Insets(10));
        dialogo.getDialogPane().setContent(grilla);
    }

    TextField texto(String etiqueta, String inicial) {
        TextField campo = new TextField(inicial);
        campo.setPrefColumnCount(22);
        agregar(etiqueta, campo);
        return campo;
    }

    TextArea area(String etiqueta) {
        TextArea campo = new TextArea();
        campo.setPrefRowCount(3);
        campo.setPrefColumnCount(22);
        campo.setWrapText(true);
        agregar(etiqueta, campo);
        return campo;
    }

    <T> ComboBox<T> opciones(String etiqueta, List<T> valores) {
        ComboBox<T> combo = new ComboBox<>(FXCollections.observableArrayList(valores));
        combo.setMaxWidth(Double.MAX_VALUE);
        if (!valores.isEmpty()) combo.getSelectionModel().selectFirst();
        agregar(etiqueta, combo);
        return combo;
    }

    CheckBox casilla(String etiqueta) {
        CheckBox casilla = new CheckBox();
        agregar(etiqueta, casilla);
        return casilla;
    }

    private void agregar(String etiqueta, Control control) {
        grilla.add(new Label(etiqueta), 0, fila);
        grilla.add(control, 1, fila);
        fila++;
    }

    /** true si el usuario confirmó con Aceptar. */
    boolean mostrar() {
        return dialogo.showAndWait().filter(boton -> boton == ButtonType.OK).isPresent();
    }

    /** Lee la opción elegida o rechaza si no hay ninguna. */
    static <T> T elegido(ComboBox<T> combo, String campo) {
        T valor = combo.getValue();
        if (valor == null) throw new IllegalArgumentException("Seleccione " + campo + ".");
        return valor;
    }
}
