package main.java.modelo.reclamos;

public enum TipoReclamo {
    SEGURIDAD(modelo.comun.Prioridad.ALTA),
    MANTENIMIENTO(modelo.comun.Prioridad.MEDIA),
    CONVIVENCIA(modelo.comun.Prioridad.BAJA),
    ADMINISTRATIVO(modelo.comun.Prioridad.BAJA);

    private final modelo.comun.Prioridad prioridad;
    TipoReclamo(modelo.comun.Prioridad prioridad) { this.prioridad = prioridad; }
    public modelo.comun.Prioridad getPrioridad() { return prioridad; }
}
