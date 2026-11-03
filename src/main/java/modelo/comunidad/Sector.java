package main.java.modelo.comunidad;

public class Sector implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String nombre;
    public Sector(String nombre) { this.nombre = modelo.comun.Validaciones.texto(nombre, "Sector"); }
    public String getNombre() { return nombre; }
    @Override public boolean equals(Object otro) {
        if (!(otro instanceof Sector)) return false;
        Sector sector = (Sector) otro;
        return nombre.equalsIgnoreCase(sector.nombre);
    }
    @Override public int hashCode() {
        String normalizado = "";
        for (int i = 0; i < nombre.length(); i++) {
            normalizado += Character.toLowerCase(Character.toUpperCase(nombre.charAt(i)));
        }
        return normalizado.hashCode();
    }
    @Override public String toString() { return nombre; }
}
