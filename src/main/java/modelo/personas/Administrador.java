package modelo.personas;

public class Administrador extends Personal {
    private static final long serialVersionUID = 1L;
    public Administrador(Persona persona, String legajo, String fechaIngreso) { super(persona, legajo, fechaIngreso); }
    @Override public boolean puedeAtender(modelo.reclamos.TipoReclamo tipo) {
        return tipo == modelo.reclamos.TipoReclamo.ADMINISTRATIVO || tipo == modelo.reclamos.TipoReclamo.CONVIVENCIA;
    }
}
