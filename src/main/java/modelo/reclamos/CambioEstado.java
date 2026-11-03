package main.java.modelo.reclamos;

public final class CambioEstado implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String fecha, observacion;
    private final EstadoReclamo nuevoEstado;
    public CambioEstado(String fecha, EstadoReclamo nuevoEstado, String observacion) {
        modelo.comun.Fechas.momento(fecha);
        this.fecha = fecha;
        this.nuevoEstado = modelo.comun.Validaciones.requerido(nuevoEstado, "Estado");
        this.observacion = modelo.comun.Validaciones.texto(observacion, "Observación");
    }
    public String getFecha() { return fecha; }
    public EstadoReclamo getNuevoEstado() { return nuevoEstado; }
    public String getObservacion() { return observacion; }
    @Override public String toString() { return fecha + " | " + nuevoEstado + " | " + observacion; }
}
