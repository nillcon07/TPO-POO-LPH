package vista.fx;

import javafx.scene.control.TextArea;
import controlador.GestorBarrioControlador;
import modelo.comun.Fechas;
import modelo.personas.Persona;

/** Reportes y alertas del administrador. */
final class PanelReportes extends PanelBase {
    private final TextArea texto = new TextArea();

    PanelReportes(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        texto.setEditable(false);
        setTop(Componentes.barra(Componentes.boton("Generar reportes", this::refrescar)));
        setCenter(texto);
        refrescar();
    }

    @Override String titulo() { return "Reportes"; }

    @Override void refrescar() {
        texto.setText(controlador.generarReportes(actor, Fechas.ahora()));
    }
}
