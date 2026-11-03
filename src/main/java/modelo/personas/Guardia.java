package modelo.personas;

public class Guardia extends Personal {
    private static final long serialVersionUID = 1L;
    private final Turno turno;
    private final PuestoGuardia puestoAsignado;
    public Guardia(Persona persona, String legajo, String fechaIngreso, Turno turno, PuestoGuardia puestoAsignado) {
        super(persona, legajo, fechaIngreso);
        this.turno = modelo.comun.Validaciones.requerido(turno, "Turno");
        this.puestoAsignado = modelo.comun.Validaciones.requerido(puestoAsignado, "Puesto");
    }
    public Turno getTurno() { return turno; }
    public PuestoGuardia getPuestoAsignado() { return puestoAsignado; }
    @Override public boolean puedeAtender(modelo.reclamos.TipoReclamo tipo) { return tipo == modelo.reclamos.TipoReclamo.SEGURIDAD; }
}
