package main.java.modelo.personas;

public class Residente extends Persona {
    private static final long serialVersionUID = 1L;
    private final modelo.comunidad.Lote lote;
    private final TipoResidente tipoResidente;
    public Residente(Persona persona, modelo.comunidad.Lote lote, TipoResidente tipoResidente) {
        super(persona);
        this.lote = modelo.comun.Validaciones.requerido(lote, "Lote");
        this.tipoResidente = modelo.comun.Validaciones.requerido(tipoResidente, "Tipo de residente");
    }
    public modelo.comunidad.Lote getLote() { return lote; }
    public TipoResidente getTipoResidente() { return tipoResidente; }
}
