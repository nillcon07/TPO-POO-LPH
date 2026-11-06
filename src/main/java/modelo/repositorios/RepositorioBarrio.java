package modelo.repositorios;

import modelo.comunidad.Barrio;

public interface RepositorioBarrio {
    Barrio cargar();
    void guardar(Barrio barrio);
}
