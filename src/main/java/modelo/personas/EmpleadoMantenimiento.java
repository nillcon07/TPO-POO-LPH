package modelo.personas;

public class EmpleadoMantenimiento extends Personal {
    private static final long serialVersionUID = 1L;
    private final Especialidad especialidad;
    public EmpleadoMantenimiento(Persona persona, String legajo, String fechaIngreso, Especialidad especialidad) {
        super(persona, legajo, fechaIngreso);
        this.especialidad = modelo.comun.Validaciones.requerido(especialidad, "Especialidad");
    }
    public Especialidad getEspecialidad() { return especialidad; }
    @Override public boolean puedeAtender(modelo.reclamos.TipoReclamo tipo) { return tipo == modelo.reclamos.TipoReclamo.MANTENIMIENTO; }
}
