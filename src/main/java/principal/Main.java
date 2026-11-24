package principal;

import controlador.GestorBarrioControlador;
import modelo.comun.RelojSesion;
import vista.VistaConsola;

public class Main {
    public static void main(String[] args) {
        try {
            String ruta = CreadorControlador.RUTA_DATOS;
            boolean resumen = false;
            String fechaHora = null;
            for (String argumento : args) {
                if (argumento.startsWith("--datos=")) ruta = argumento.substring(8);
                else if (argumento.equals("--resumen")) resumen = true;
                else if (argumento.startsWith("--fecha=")) fechaHora = argumento.substring(8);
                else throw new IllegalArgumentException("Argumento no reconocido: " + argumento);
            }
            GestorBarrioControlador controlador = CreadorControlador.crearControlador(ruta);
            if (resumen) {
                if (fechaHora == null) throw new IllegalArgumentException("Para obtener el resumen indique --fecha=aaaa-MM-dd HH:mm.");
                RelojSesion.getInstancia().establecer(fechaHora);
                controlador.verificarControles(RelojSesion.getInstancia().ahora());
                System.out.println("Fuente: DEMO: personas ficticias");
                System.out.println("Barrio: " + controlador.getNombreBarrio() + " | lotes: " + controlador.cantidadLotes());
                System.out.println(controlador.generarReportes(controlador.getAdministrador(), RelojSesion.getInstancia().ahora()));
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
