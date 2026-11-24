package principal;

import javafx.application.Application;
import vista.fx.AplicacionFX;

/**
 * Punto de entrada de la interfaz gráfica.
 * Es una clase aparte que NO extiende Application: así IntelliJ puede ejecutarla con JavaFX
 * en el classpath (dependencias de Maven) sin configurar módulos ni "--module-path".
 */
public final class LanzadorFX {
    private LanzadorFX() { }

    public static void main(String[] args) {
        Application.launch(AplicacionFX.class, args);
    }
}
