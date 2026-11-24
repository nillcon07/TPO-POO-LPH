package principal;

import java.nio.file.Paths;
import controlador.GestorBarrioControlador;
import modelo.archivos.RepositorioBarrioArchivo;
import modelo.archivos.RepositorioPersonasCsv;
import modelo.comunidad.Barrio;
import modelo.repositorios.RepositorioBarrio;
import modelo.repositorios.RepositorioPersonas;
import modelo.repositorios.RepositorioReclamosMemoria;

/** Arma el controlador con sus repositorios. Lo comparten la consola y la interfaz JavaFX. */
public final class Arranque {
    public static final String RUTA_DATOS = "datos/barrio.dat";

    private Arranque() { }

    public static GestorBarrioControlador crearControlador(String rutaDatos) {
        RepositorioPersonas personas = new RepositorioPersonasCsv(Paths.get("datos", "personas.csv"));
        RepositorioBarrio almacenamiento = new RepositorioBarrioArchivo(Paths.get(rutaDatos));
        Barrio barrio = almacenamiento.cargar();
        if (barrio == null) barrio = DatosDemo.crear(personas);
        return new GestorBarrioControlador(barrio, new RepositorioReclamosMemoria(barrio), personas, almacenamiento);
    }
}
