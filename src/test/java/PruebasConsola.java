import java.nio.file.Paths;

import controlador.GestorBarrioControlador;
import modelo.archivos.RepositorioPersonasCsv;
import principal.DatosDemo;
import modelo.comunidad.Barrio;
import modelo.comun.Fechas;
import modelo.finanzas.CuentaLote;
import modelo.mantenimiento.EstadoEquipo;
import modelo.reclamos.EstadoReclamo;
import modelo.reclamos.Reclamo;
import modelo.repositorios.*;
import vista.VistaConsola;

public final class PruebasConsola {
    private static final class Contexto {
        final RepositorioPersonas personas = new RepositorioPersonasMemoria(new RepositorioPersonasCsv(Paths.get("datos/personas.csv")).listar());
        final Barrio barrio = DatosDemo.crear(personas);
        int guardados;
        final GestorBarrioControlador controlador = new GestorBarrioControlador(
                barrio, new RepositorioReclamosMemoria(barrio), personas, new RepositorioBarrio() {
            public Barrio cargar() { return barrio; }
            public void guardar(Barrio valor) { guardados++; }
        });
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new IllegalStateException(mensaje);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Indique el número de escenario (1 a 11) como argumento de ejecución.");
            return;
        }
        int escenario = Integer.parseInt(args[0]);
        exigir(escenario >= 1 && escenario <= 11, "El escenario debe estar entre 1 y 11.");
        Contexto x = new Contexto();
        VistaConsola vista = new VistaConsola(x.controlador);
        vista.iniciar();

        switch (escenario) {
            case 1:
                exigir(x.guardados > 0, "Guarda antes de salir.");
                break;
            case 2:
                exigir(x.barrio.getLotes().size() == 2, "No se agregó un lote incompleto.");
                break;
            case 3:
                exigir(x.barrio.buscarLote(3).getSuperficie() == 250.5, "Conversión decimal.");
                exigir(x.barrio.buscarLote(3).getTitular().getDocumento().equals("101"), "Persona existente seleccionada.");
                break;
            case 4:
                Reclamo reclamo = x.barrio.getReclamos().get(0);
                exigir(reclamo.getEstado() == EstadoReclamo.ASIGNADO, "La reapertura conserva el flujo.");
                exigir(reclamo.getResponsableAsignado().getLegajo().equals("M001"), "Se mantiene el responsable.");
                exigir(reclamo.getCambiosEstado().size() == 5 && reclamo.getComentarios().size() == 1, "Se guarda la evolución completa.");
                break;
            case 5:
                CuentaLote cuenta = x.barrio.buscarLote(1).getCuenta();
                exigir(cuenta.getPagos().size() == 1 && cuenta.getAjustes().size() == 1, "Se registran ambos movimientos.");
                exigir(cuenta.getPagos().get(0).getImporteTotal() == 100.25, "El pago original permanece intacto.");
                exigir(cuenta.getAjustes().get(0).getMontoAjuste() == -25.25, "El ajuste conserva el signo.");
                break;
            case 6:
                exigir(x.barrio.getOrdenes().get(0).getFechaCierre() != null, "Se cierra la orden.");
                exigir(x.barrio.buscarEquipo("CAM-01").getEstado() == EstadoEquipo.OPERATIVO, "Equipo restablecido.");
                break;
            case 7:
                exigir(x.barrio.getAutorizaciones().size() == 1, "Autorización creada por titular.");
                exigir(x.barrio.getRegistrosAcceso().size() == 1, "Se rechaza un ingreso duplicado.");
                exigir(x.barrio.getRegistrosAcceso().get(0).getFechaHoraEgreso() != null, "Egreso registrado.");
                break;
            case 8:
                exigir(x.barrio.getReclamos().isEmpty(), "No crea el reclamo del actor sin permiso.");
                break;
            case 9:
                exigir(x.barrio.getNotificaciones().size() == 1, "Avanzar la fecha genera una sola alerta.");
                exigir(Fechas.ahora().equals("2026-11-05 10:00"), "La fecha se actualiza con el texto válido.");
                break;
            case 10:
                exigir(x.controlador.listarPersonas().size() == 10, "Se agrega una sola persona válida.");
                exigir(x.personas.buscarPorDocumento("109").getNombre().equals("Lucía"), "Se conservan sus datos.");
                exigir(x.barrio.buscarLote(3).getTitular().getDocumento().equals("109"), "La nueva persona se puede seleccionar como titular.");
                break;
            case 11:
                exigir(x.controlador.listarPersonas().size() == 9, "Una entrada incompleta no agrega personas.");
                break;
            default:
                throw new IllegalStateException("Escenario inexistente.");
        }
        System.out.println("OK: escenario de consola " + escenario);
    }
}
