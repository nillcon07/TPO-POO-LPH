package modelo.reportes;

import java.util.*;
import modelo.comunidad.*;
import modelo.reclamos.*;
import modelo.mantenimiento.*;
import modelo.comun.Fechas;

public class GeneradorReportes {
    public List<Reclamo> reclamosPendientes(Barrio barrio) {
        List<Reclamo> resultado = new ArrayList<>();
        for (Reclamo reclamo : barrio.getReclamos()) {
            if (reclamo.getEstado() != EstadoReclamo.CERRADO && reclamo.getEstado() != EstadoReclamo.RESUELTO) resultado.add(reclamo);
        }
        return resultado;
    }

    public List<Lote> morosidad(Barrio barrio) {
        List<Lote> resultado = new ArrayList<>();
        for (Lote lote : barrio.getLotes()) if (lote.getCuenta().isMoroso()) resultado.add(lote);
        return resultado;
    }

    public List<Equipo> equiposFueraServicio(Barrio barrio) {
        List<Equipo> resultado = new ArrayList<>();
        for (Equipo equipo : barrio.getEquipos()) {
            if (equipo.getEstado() == EstadoEquipo.FUERA_DE_SERVICIO) resultado.add(equipo);
        }
        return resultado;
    }

    public List<Reclamo> historialReclamos(Barrio barrio, EstadoReclamo estado) {
        List<Reclamo> resultado = new ArrayList<>();
        for (Reclamo reclamo : barrio.getReclamos()) {
            if (estado == null || reclamo.getEstado() == estado) resultado.add(reclamo);
        }
        return resultado;
    }

    public double promedioHorasResolucion(Barrio barrio) {
        long minutos = 0;
        int cantidad = 0;
        for (OrdenMantenimiento orden : barrio.getOrdenes()) {
            if (orden.getFechaCierre() != null) {
                minutos += Fechas.minutosEntre(orden.getFechaApertura(), orden.getFechaCierre());
                cantidad++;
            }
        }
        if (cantidad == 0) return 0;
        return minutos / (cantidad * 60.0);
    }
}
