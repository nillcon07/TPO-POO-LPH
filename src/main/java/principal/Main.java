package principal;

import java.nio.file.Paths;
import controlador.GestorBarrioControlador;
import modelo.archivos.RepositorioBarrioArchivo;
import modelo.archivos.RepositorioPersonasCsv;
import modelo.comun.Fechas;
import modelo.comunidad.Barrio;
import modelo.repositorios.RepositorioBarrio;
import modelo.repositorios.RepositorioPersonas;
import modelo.repositorios.RepositorioReclamosMemoria;
import vista.VistaConsola;

public class Main {
    public static final String RUTA_DATOS = "datos/barrio.dat";

    /** Arma el controlador con sus repositorios. Lo usan la consola y la interfaz JavaFX. */
    public static GestorBarrioControlador crearControlador(String rutaDatos) {
        RepositorioPersonas personas = new RepositorioPersonasCsv(Paths.get("datos", "personas.csv"));
        RepositorioBarrio almacenamiento = new RepositorioBarrioArchivo(Paths.get(rutaDatos));
        Barrio barrio = almacenamiento.cargar();
        if (barrio == null) barrio = DatosDemo.crear(personas);
        return new GestorBarrioControlador(barrio, new RepositorioReclamosMemoria(barrio), personas, almacenamiento);
    }

    public static void main(String[] args) {
        try {
            String ruta = RUTA_DATOS;
            boolean resumen = false;
            String fechaHora = null;
            for (String argumento : args) {
                if (argumento.startsWith("--datos=")) ruta = argumento.substring(8);
                else if (argumento.equals("--resumen")) resumen = true;
                else if (argumento.startsWith("--fecha=")) fechaHora = argumento.substring(8);
                else throw new IllegalArgumentException("Argumento no reconocido: " + argumento);
            }
            GestorBarrioControlador controlador = crearControlador(ruta);
            if (resumen) {
                if (fechaHora == null) throw new IllegalArgumentException("Para obtener el resumen indique --fecha=aaaa-MM-dd HH:mm.");
                Fechas.establecerMomento(fechaHora);
                controlador.verificarControles(Fechas.ahora());
                System.out.println("Fuente: DEMO: personas ficticias");
                System.out.println("Barrio: " + controlador.getNombreBarrio() + " | lotes: " + controlador.cantidadLotes());
                System.out.println(controlador.generarReportes(controlador.getAdministrador(), Fechas.ahora()));
            } else {
                VistaConsola vista = new VistaConsola(controlador);
                vista.iniciar();
            }
        } catch (RuntimeException e) {
            System.out.println("No se pudo iniciar Los Aromos: " + e.getMessage());
            System.exit(1);
        }
    }
}
