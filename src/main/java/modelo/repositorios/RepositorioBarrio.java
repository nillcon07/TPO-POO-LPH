package modelo.repositorios;

import modelo.comunidad.Barrio;

public interface RepositorioBarrio {
    /** Devuelve null cuando todavía no existe un barrio guardado. */
    Barrio cargar();
    void guardar(Barrio barrio);
}
