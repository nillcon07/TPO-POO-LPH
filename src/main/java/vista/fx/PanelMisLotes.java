package vista.fx;

import javafx.scene.control.*;
import controlador.GestorBarrioControlador;
import modelo.comunidad.Lote;
import modelo.personas.Persona;

/** Solo para titulares: administran quién puede presentar reclamos por su lote. */
final class PanelMisLotes extends PanelBase {
    private final TableView<Lote> lotes = Componentes.tabla();

    PanelMisLotes(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        Componentes.columna(lotes, "Lote", Lote::getNumero);
        Componentes.columna(lotes, "Superficie m²", Lote::getSuperficie);
        Componentes.columna(lotes, "Residentes", Lote::getResidentes);
        Componentes.columna(lotes, "Autorizados a reclamar", Lote::getPersonasAutorizadasReclamo);
        setTop(Componentes.barra(
                Componentes.boton("Autorizar a reclamar", () -> permiso(true)),
                Componentes.boton("Revocar permiso", () -> permiso(false))));
        setCenter(lotes);
        refrescar();
    }

    @Override String titulo() { return "Mis lotes"; }

    @Override void refrescar() {
        Componentes.cargar(lotes, controlador.listarLotesDelTitular(actor));
    }

    private void permiso(boolean autorizar) {
        Lote lote = Componentes.seleccionado(lotes, "un lote");
        Formulario f = new Formulario((autorizar ? "Autorizar" : "Revocar") + " permiso de reclamo — lote " + lote.getNumero());
        ComboBox<Persona> persona = f.opciones("Persona",
                autorizar ? controlador.listarPersonas() : lote.getPersonasAutorizadasReclamo());
        if (!f.mostrar()) return;
        operar(() -> controlador.permisoReclamos(actor, lote.getNumero(),
                Formulario.elegido(persona, "una persona").getDocumento(), autorizar));
    }
}
