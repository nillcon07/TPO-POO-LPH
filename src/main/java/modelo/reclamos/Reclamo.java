package main.java.modelo.reclamos;

import java.io.Serializable;
import java.util.*;
import modelo.comun.*;
import modelo.comunidad.Lote;
import modelo.personas.*;


public class Reclamo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final String fecha, descripcion;
    private final TipoReclamo tipo;
    private final Prioridad prioridad;
    private EstadoReclamo estado = EstadoReclamo.ABIERTO;
    private final Lote lote;
    private final Persona personaPresentante;
    private Personal responsableAsignado;
    private final List<CambioEstado> cambiosEstado = new ArrayList<>();
    private final List<Comentario> comentarios = new ArrayList<>();
    public Reclamo(int numero, String fecha, TipoReclamo tipo, String descripcion, Lote lote, Persona presentante) {
        Validaciones.exigir(numero > 0, "Número de reclamo inválido.");
        Fechas.momento(fecha);
        this.numero = numero;
        this.fecha = fecha;
        this.tipo = Validaciones.requerido(tipo, "Tipo");
        this.prioridad = tipo.getPrioridad();
        this.descripcion = Validaciones.texto(descripcion, "Descripción");
        this.lote = Validaciones.requerido(lote, "Lote");
        this.personaPresentante = Validaciones.requerido(presentante, "Presentante");
        Validaciones.exigir(lote.puedeReclamar(presentante), "La persona no tiene permiso para reclamar por este lote.");
        cambiosEstado.add(new CambioEstado(fecha, estado, "Registro del reclamo"));
    }
    private void validarFecha(String fecha) { Fechas.noAnterior(cambiosEstado.get(cambiosEstado.size() - 1).getFecha(), fecha); }
    private void validarCapacidad(Personal responsable, Collection<Reclamo> reclamos) {
        int enCurso = 0;
        for (Reclamo reclamo : reclamos) {
            if (reclamo != this && reclamo.estado == EstadoReclamo.EN_CURSO && responsable.equals(reclamo.responsableAsignado)) enCurso++;
        }
        Validaciones.exigir(enCurso < 5, "El responsable ya tiene cinco reclamos EN_CURSO.");
    }
    public void asignar(Administrador actor, Personal responsable, String fecha, Collection<Reclamo> reclamos) {
        Validaciones.requerido(actor, "Administrador");
        Validaciones.requerido(responsable, "Responsable");
        validarFecha(fecha);
        Validaciones.exigir(estado != EstadoReclamo.RESUELTO && estado != EstadoReclamo.CERRADO, "No se puede asignar un reclamo resuelto o cerrado.");
        Validaciones.exigir(responsable.puedeAtender(tipo), "El responsable no está habilitado para este tipo de reclamo.");
        if (estado == EstadoReclamo.EN_CURSO) validarCapacidad(responsable, reclamos);
        responsableAsignado = responsable;
        if (estado == EstadoReclamo.ABIERTO) estado = EstadoReclamo.ASIGNADO;
        cambiosEstado.add(new CambioEstado(fecha, estado, "Asignado a " + responsable.getLegajo()));
    }
    public void actualizarEstado(Persona actor, EstadoReclamo nuevo, String fecha, String observacion, Collection<Reclamo> reclamos) {
        Validaciones.requerido(nuevo, "Estado");
        validarFecha(fecha);
        Validaciones.texto(observacion, "Observación");
        Validaciones.exigir(actor instanceof Administrador || responsableAsignado != null && responsableAsignado.equals(actor), "Solo el responsable o el administrador puede actualizar el estado.");
        Validaciones.exigir(estado != EstadoReclamo.CERRADO && nuevo.ordinal() == estado.ordinal() + 1, "Solo se permite avanzar al estado siguiente.");
        Validaciones.exigir(estado != EstadoReclamo.ABIERTO, "Debe asignar un responsable antes de avanzar.");
        if (nuevo == EstadoReclamo.EN_CURSO) validarCapacidad(responsableAsignado, reclamos);
        if (nuevo == EstadoReclamo.CERRADO) {
            Validaciones.exigir(actor instanceof Administrador, "Solo el administrador puede cerrar el reclamo.");
            Validaciones.exigir(puedeCerrar(fecha), "El plazo de cinco días para reabrir aún no terminó.");
        }
        estado = nuevo;
        cambiosEstado.add(new CambioEstado(fecha, estado, observacion));
    }
    public void reabrir(Persona vecino, String fecha, String observacion) {
        validarFecha(fecha);
        Validaciones.texto(observacion, "Observación");
        Validaciones.exigir(lote.puedeReclamar(vecino), "El vecino no está autorizado para reclamar por este lote.");
        Validaciones.exigir(estado == EstadoReclamo.RESUELTO, "Solo se puede reabrir un reclamo resuelto.");
        Validaciones.exigir(Fechas.minutosEntre(ultimaResolucion(), fecha) <= 5 * 24 * 60, "Venció el plazo de reapertura de cinco días.");
        estado = EstadoReclamo.ASIGNADO;
        cambiosEstado.add(new CambioEstado(fecha, estado, "Reapertura: " + observacion));
    }
    public void agregarComentario(Persona autor, String fecha, String texto) {
        Fechas.noAnterior(this.fecha, fecha);
        Validaciones.exigir(autor instanceof Personal || lote.puedeReclamar(autor), "El autor no puede comentar este reclamo.");
        comentarios.add(new Comentario(fecha, autor, texto));
    }
    public boolean puedeCerrar(String fecha) {
        return estado == EstadoReclamo.RESUELTO && Fechas.minutosEntre(ultimaResolucion(), fecha) > 5 * 24 * 60;
    }
    private String ultimaResolucion() {
        for (int i = cambiosEstado.size() - 1; i >= 0; i--) if (cambiosEstado.get(i).getNuevoEstado() == EstadoReclamo.RESUELTO) return cambiosEstado.get(i).getFecha();
        throw new IllegalStateException("El reclamo no tiene resolución.");
    }
    public boolean requiereEscalamiento(String fecha) {
        if (prioridad != Prioridad.ALTA) return false;
        for (CambioEstado cambio : cambiosEstado) {
            if (cambio.getNuevoEstado() == EstadoReclamo.EN_CURSO) return false;
        }
        return Fechas.minutosEntre(this.fecha, fecha) >= 24 * 60;
    }
    public int getNumero() { return numero; }
    public String getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public TipoReclamo getTipo() { return tipo; }
    public Prioridad getPrioridad() { return prioridad; }
    public EstadoReclamo getEstado() { return estado; }
    public Lote getLote() { return lote; }
    public Persona getPersonaPresentante() { return personaPresentante; }
    public Personal getResponsableAsignado() { return responsableAsignado; }
    public List<CambioEstado> getCambiosEstado() { return new ArrayList<>(cambiosEstado); }
    public List<Comentario> getComentarios() { return new ArrayList<>(comentarios); }
    @Override public String toString() { return "#" + numero + " | lote " + lote.getNumero() + " | " + tipo + " | " + estado + " | " + descripcion; }
}
