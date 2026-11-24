package vista.javafx;

import java.util.Arrays;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import controlador.GestorBarrioControlador;
import modelo.comun.RelojSesion;
import modelo.comunidad.Lote;
import modelo.personas.*;
import modelo.reclamos.*;

/** Pestaña de reclamos. Cada usuario ve solo los reclamos que puede consultar. */
final class PanelReclamos extends PanelBase {
    private final TableView<Reclamo> tabla = Componentes.tabla();

    PanelReclamos(GestorBarrioControlador controlador, Persona actor) {
        super(controlador, actor);
        Componentes.columna(tabla, "N°", Reclamo::getNumero);
        Componentes.columna(tabla, "Lote", r -> r.getLote().getNumero());
        Componentes.columna(tabla, "Tipo", Reclamo::getTipo);
        Componentes.columna(tabla, "Prioridad", Reclamo::getPrioridad);
        Componentes.columna(tabla, "Estado", Reclamo::getEstado);
        Componentes.columna(tabla, "Responsable", Reclamo::getResponsableAsignado);
        Componentes.columna(tabla, "Descripción", Reclamo::getDescripcion);
        setCenter(tabla);

        HBox barra = Componentes.barra(Componentes.boton("Ver detalle", this::detalle),
                Componentes.boton("Comentar", this::comentar));
        if (disponible(() -> controlador.listarLotesParaReclamar(actor)))
            barra.getChildren().add(Componentes.boton("Nuevo reclamo", this::registrar));
        if (controlador.esAdministrador(actor))
            barra.getChildren().add(Componentes.boton("Asignar responsable", this::asignar));
        if (actor instanceof Personal)
            barra.getChildren().add(Componentes.boton("Actualizar estado", this::actualizarEstado));
        else
            barra.getChildren().add(Componentes.boton("Reabrir", this::reabrir));
        setTop(barra);
        refrescar();
    }

    @Override String titulo() { return "Reclamos"; }

    @Override void refrescar() {
        Componentes.cargar(tabla, controlador.listarReclamos(actor));
    }

    private void detalle() {
        Reclamo elegido = Componentes.seleccionado(tabla, "un reclamo");
        Reclamo reclamo = controlador.consultarReclamo(actor, elegido.getNumero());
        String texto = reclamo + "\nPrioridad: " + reclamo.getPrioridad()
                + "\nResponsable: " + reclamo.getResponsableAsignado() + "\n\nHISTORIAL\n";
        for (CambioEstado cambio : reclamo.getCambiosEstado()) texto += cambio + "\n";
        texto += "\nCOMENTARIOS\n";
        for (Comentario comentario : reclamo.getComentarios()) texto += comentario + "\n";
        Componentes.texto("Reclamo " + reclamo.getNumero(), texto);
    }

    private void registrar() {
        Formulario f = new Formulario("Nuevo reclamo");
        ComboBox<Lote> lote = f.opciones("Lote", controlador.listarLotesParaReclamar(actor));
        ComboBox<TipoReclamo> tipo = f.opciones("Tipo", Arrays.asList(TipoReclamo.values()));
        TextArea descripcion = f.area("Descripción");
        if (!f.mostrar()) return;
        operar(() -> controlador.registrarReclamo(actor, Formulario.elegido(lote, "un lote").getNumero(),
                Formulario.elegido(tipo, "un tipo"), descripcion.getText(), RelojSesion.getInstancia().ahora()));
    }

    private void asignar() {
        Reclamo reclamo = Componentes.seleccionado(tabla, "un reclamo");
        Formulario f = new Formulario("Asignar reclamo " + reclamo.getNumero());
        ComboBox<Personal> responsable = f.opciones("Responsable",
                controlador.listarResponsablesHabilitados(actor, reclamo.getTipo()));
        if (!f.mostrar()) return;
        operar(() -> controlador.asignarReclamo(actor, reclamo.getNumero(),
                Formulario.elegido(responsable, "un responsable").getLegajo(), RelojSesion.getInstancia().ahora()));
    }

    private void actualizarEstado() {
        Reclamo reclamo = Componentes.seleccionado(tabla, "un reclamo");
        Formulario f = new Formulario("Actualizar estado del reclamo " + reclamo.getNumero());
        ComboBox<EstadoReclamo> estado = f.opciones("Nuevo estado", Arrays.asList(EstadoReclamo.values()));
        TextArea observacion = f.area("Observación");
        if (!f.mostrar()) return;
        operar(() -> controlador.actualizarEstado(actor, reclamo.getNumero(), Formulario.elegido(estado, "un estado"),
                RelojSesion.getInstancia().ahora(), observacion.getText()));
    }

    private void reabrir() {
        Reclamo reclamo = Componentes.seleccionado(tabla, "un reclamo");
        Formulario f = new Formulario("Reabrir reclamo " + reclamo.getNumero());
        TextArea motivo = f.area("Motivo");
        if (!f.mostrar()) return;
        operar(() -> controlador.reabrirReclamo(actor, reclamo.getNumero(), RelojSesion.getInstancia().ahora(), motivo.getText()));
    }

    private void comentar() {
        Reclamo reclamo = Componentes.seleccionado(tabla, "un reclamo");
        Formulario f = new Formulario("Comentar reclamo " + reclamo.getNumero());
        TextArea texto = f.area("Comentario");
        if (!f.mostrar()) return;
        operar(() -> controlador.agregarComentario(actor, reclamo.getNumero(), RelojSesion.getInstancia().ahora(), texto.getText()));
    }
}
