package modelo.comun;

/** Singleton: fecha y hora de la sesión, única y compartida por todo el sistema. */
public final class RelojSesion {
    private static final RelojSesion INSTANCIA = new RelojSesion();
    private String momentoActual;

    private RelojSesion() { }

    public static RelojSesion getInstancia() {
        return INSTANCIA;
    }

    public void establecer(String fechaHora) {
        Fechas.momento(fechaHora);
        momentoActual = fechaHora;
    }

    public void avanzar(String fechaHora) {
        Fechas.noAnterior(ahora(), fechaHora);
        establecer(fechaHora);
    }

    public boolean tieneMomento() {
        return momentoActual != null;
    }

    public String ahora() {
        Validaciones.exigir(momentoActual != null, "Primero indique la fecha y hora de la sesión.");
        return momentoActual;
    }

    public String hoy() {
        return Fechas.dia(ahora());
    }
}
