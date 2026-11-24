package principal;

import modelo.comunidad.*;
import modelo.personas.*;
import modelo.mantenimiento.*;
import modelo.repositorios.RepositorioPersonas;

public final class DatosDemo {
    private DatosDemo() { }

    public static Barrio crear(RepositorioPersonas personas) {
        Administrador admin = new Administrador(personas.buscarPorDocumento("100"), "A001", "2026-01-01");
        Barrio barrio = new Barrio("Los Aromos", admin);
        Lote lote = barrio.agregarLote(1, 400, personas.buscarPorDocumento("101"));
        barrio.agregarLote(2, 600, personas.buscarPorDocumento("102"));
        lote.agregarResidente(personas.buscarPorDocumento("103"), TipoResidente.FAMILIAR);
        barrio.agregarGuardia(personas.buscarPorDocumento("104"), "G001", "2026-01-01", Turno.MANANA, PuestoGuardia.PORTERIA);
        barrio.agregarEmpleado(personas.buscarPorDocumento("105"), "M001", "2026-01-01", Especialidad.ELECTRICIDAD);
        Sector acceso = new Sector("Acceso principal");
        barrio.agregarEquipo(new Camara("CAM-01", acceso, "2026-01-01", "1920x1080", 30));
        barrio.agregarEquipo(new Luminaria("LUM-01", acceso, "2026-01-01", "LED", 80));
        return barrio;
    }
}
