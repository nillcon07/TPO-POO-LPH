package modelo.personas;

import java.io.Serializable;
import modelo.comun.Validaciones;

public class Persona implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nombre, apellido, documento, telefono, correoElectronico;
    public Persona(String nombre, String apellido, String documento, String telefono, String correoElectronico) {
        this.nombre = Validaciones.texto(nombre, "Nombre");
        this.apellido = Validaciones.texto(apellido, "Apellido");
        this.documento = Validaciones.texto(documento, "Documento");
        this.telefono = Validaciones.texto(telefono, "Teléfono");
        this.correoElectronico = Validaciones.texto(correoElectronico, "Correo electrónico");
    }
    protected Persona(Persona persona) {
        this(persona.getNombre(), persona.getApellido(), persona.getDocumento(), persona.getTelefono(), persona.getCorreoElectronico());
    }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumento() { return documento; }
    public String getTelefono() { return telefono; }
    public String getCorreoElectronico() { return correoElectronico; }
    @Override public final boolean equals(Object otro) {
        if (!(otro instanceof Persona)) return false;
        Persona persona = (Persona) otro;
        return documento.equals(persona.documento);
    }
    @Override public final int hashCode() { return documento.hashCode(); }
    @Override public String toString() { return nombre + " " + apellido + " (" + documento + ")"; }
}
