package modelo.personas;

public abstract class Personal extends Persona {
    private static final long serialVersionUID = 1L;
    private final String legajo, fechaIngreso;
    protected Personal(Persona persona, String legajo, String fechaIngreso) {
        super(persona);
        modelo.comun.Fechas.fecha(fechaIngreso);
        this.legajo = modelo.comun.Validaciones.texto(legajo, "Legajo");
        this.fechaIngreso = fechaIngreso;
    }
    public String getLegajo() { return legajo; }
    public String getFechaIngreso() { return fechaIngreso; }
    public abstract boolean puedeAtender(modelo.reclamos.TipoReclamo tipo);
}
