package modelo.personas;

public class VisitanteProveedor extends Persona {
    private static final long serialVersionUID = 1L;
    private final TipoVisitante tipo;
    private final boolean esProveedorHabitual;
    public VisitanteProveedor(Persona persona, TipoVisitante tipo, boolean esProveedorHabitual) {
        super(persona);
        this.tipo = modelo.comun.Validaciones.requerido(tipo, "Tipo de visitante");
        modelo.comun.Validaciones.exigir(!esProveedorHabitual || tipo == TipoVisitante.PROVEEDOR, "Solo un proveedor puede ser habitual.");
        this.esProveedorHabitual = esProveedorHabitual;
    }
    public TipoVisitante getTipo() { return tipo; }
    public boolean isProveedorHabitual() { return esProveedorHabitual; }
}
