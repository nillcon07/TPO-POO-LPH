package modelo.archivos;

import java.nio.file.*;
import modelo.comunidad.Barrio;
import modelo.repositorios.RepositorioBarrio;

public final class RepositorioBarrioArchivo implements RepositorioBarrio {
    private final Path archivo;

    public RepositorioBarrioArchivo(Path archivo) { this.archivo = archivo; }

    @Override public Barrio cargar() {
        if (!Files.exists(archivo)) return null;
        Object objeto = AlmacenObjetos.leer(archivo);
        if (!(objeto instanceof Barrio)) throw new IllegalStateException("El archivo no contiene un barrio.");
        return (Barrio) objeto;
    }

    @Override public void guardar(Barrio barrio) { AlmacenObjetos.guardar(archivo, barrio); }
}
