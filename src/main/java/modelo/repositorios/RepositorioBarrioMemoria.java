package modelo.repositorios;

import modelo.comunidad.Barrio;
import modelo.comun.Validaciones;

/** Almacenamiento de prueba; los cambios solo existen durante esta ejecución. */
public final class RepositorioBarrioMemoria implements RepositorioBarrio {
    private Barrio barrio;

    public RepositorioBarrioMemoria(Barrio barrio) { this.barrio = barrio; }
    @Override public Barrio cargar() { return barrio; }
    @Override public void guardar(Barrio barrio) { this.barrio = Validaciones.requerido(barrio, "Barrio"); }
}
