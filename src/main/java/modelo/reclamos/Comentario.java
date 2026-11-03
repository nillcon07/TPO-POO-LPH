package main.java.modelo.reclamos;

public final class Comentario implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String fecha, texto;
    private final modelo.personas.Persona autor;
    public Comentario(String fecha, modelo.personas.Persona autor, String texto) {
        modelo.comun.Fechas.momento(fecha);
        this.fecha = fecha;
        this.autor = modelo.comun.Validaciones.requerido(autor, "Autor");
        this.texto = modelo.comun.Validaciones.texto(texto, "Comentario");
    }
    public String getFecha() { return fecha; }
    public modelo.personas.Persona getAutor() { return autor; }
    public String getTexto() { return texto; }
    @Override public String toString() { return fecha + " | " + autor + ": " + texto; }
}
