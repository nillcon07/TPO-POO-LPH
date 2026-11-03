package main.java.modelo.mantenimiento;

import java.io.Serializable;
import java.util.*;
import modelo.comunidad.Sector;
import modelo.comun.*;
import modelo.personas.Persona;

public abstract class Equipo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String codigo, fechaInstalacion;
    private final Sector sector;
    private EstadoEquipo estado = EstadoEquipo.OPERATIVO;
    private int numeroInterrupcion;
    private String inicioInterrupcionActual;
    private final List<Falla> fallas = new ArrayList<>();
    protected Equipo(String codigo, Sector sector, String fechaInstalacion) {
        this.codigo = Validaciones.texto(codigo, "Código de equipo");
        this.sector = Validaciones.requerido(sector, "Sector");
        Fechas.fecha(fechaInstalacion);
        this.fechaInstalacion = fechaInstalacion;
    }
    public abstract Prioridad calcularPrioridadInicial();
    public abstract String getTipoEquipo();
    public Falla registrarFalla(int numero, String fechaReporte, String fechaInicio, String descripcion, Persona informante) {
        Validaciones.exigir(Fechas.compararFechas(Fechas.dia(fechaReporte), fechaInstalacion) >= 0, "La falla precede a la instalación.");
        Falla falla = new Falla(numero, fechaReporte, fechaInicio, descripcion, informante, this);
        if (!tieneFallaActiva()) { numeroInterrupcion++; inicioInterrupcionActual = falla.getFechaInicioInterrupcion(); }
        else if (Fechas.minutosEntre(falla.getFechaInicioInterrupcion(), inicioInterrupcionActual) > 0) inicioInterrupcionActual = falla.getFechaInicioInterrupcion();
        fallas.add(falla);
        estado = EstadoEquipo.CON_FALLA;
        return falla;
    }
    public boolean tieneFallaActiva() {
        for (Falla falla : fallas) if (falla.getFechaRestablecimiento() == null) return true;
        return false;
    }
    public void iniciarReparacion() { Validaciones.exigir(tieneFallaActiva(), "El equipo no tiene fallas activas."); estado = EstadoEquipo.EN_REPARACION; }
    public void marcarFueraServicio() { estado = EstadoEquipo.FUERA_DE_SERVICIO; }
    void actualizarTrasReparacion() {
        if (!tieneFallaActiva()) {
            estado = EstadoEquipo.OPERATIVO;
        } else {
            estado = EstadoEquipo.CON_FALLA;
            for (Falla falla : fallas) {
                if (falla.getFechaRestablecimiento() == null && falla.getOrden() != null && falla.getOrden().getEmpleadoAsignado() != null) {
                    estado = EstadoEquipo.EN_REPARACION;
                    break;
                }
            }
        }
    }
    public String getCodigo() { return codigo; }
    public Sector getSector() { return sector; }
    public String getFechaInstalacion() { return fechaInstalacion; }
    public EstadoEquipo getEstado() { return estado; }
    public List<Falla> getFallas() { return new ArrayList<>(fallas); }
    public int getNumeroInterrupcion() { return numeroInterrupcion; }
    public boolean requiereAlertaInterrupcion(String fecha) {
        return this instanceof Camara && tieneFallaActiva() && Fechas.minutosEntre(inicioInterrupcionActual, fecha) > 48 * 60;
    }
    @Override public String toString() { return codigo + " | " + getTipoEquipo() + " | " + sector + " | " + estado; }
}
