package vista.javafx;

import javafx.scene.control.*;
import controlador.GestorBarrioControlador;
import modelo.comunidad.Lote;
import modelo.personas.Persona;

// Pestaña del titular para autorizar o revocar permisos de reclamo sobre sus lotes.
final class PanelMisLotes extends PanelBase {
    private final TableView<Lote> lotes = Componentes.tabla();

    PanelMisLotes(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        Componentes.columna(lotes, "Lote", Lote::getNumero);
        Componentes.columna(lotes, "Autorizados a reclamar", Lote::getPersonasAutorizadasReclamo);
        setTop(Componentes.barra(
                Componentes.boton("Autorizar", () -> permiso(true)),
                Componentes.boton("Revocar", () -> permiso(false))));
        setCenter(lotes);
        refrescar();
    }

    @Override String titulo() { return "Permisos de reclamo"; }

    @Override void refrescar() {
        Componentes.cargar(lotes, controlador.listarLotesDelTitular(actor));
    }

    private void permiso(boolean autorizar) {
        Lote lote = Componentes.seleccionado(lotes, "un lote");
        Formulario f = new Formulario((autorizar ? "Autorizar" : "Revocar") + " permiso - Lote " + lote.getNumero());
        ComboBox<Persona> persona = f.opciones("Persona", controlador.listarPersonas());
        if (!f.mostrar()) return;
        operar(() -> controlador.permisoReclamos(actor, lote.getNumero(),
                Formulario.elegido(persona, "una persona").getDocumento(), autorizar));
    }
}
