package main.java.modelo.mantenimiento;

import modelo.comun.*;
import modelo.personas.EmpleadoMantenimiento;

public class OrdenMantenimiento implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final String fechaApertura, descripcionTrabajo;
    private String fechaCierre, detalleReparacion;
    private Prioridad prioridad;
    private final Falla fallaOrigen;
    private final Equipo equipoAfectado;
    private EmpleadoMantenimiento empleadoAsignado;
    OrdenMantenimiento(int numero, String fecha, String trabajo, Falla falla) {
        Validaciones.exigir(numero > 0, "Número de orden inválido.");
        this.numero = numero;
        this.fechaApertura = fecha;
        this.descripcionTrabajo = Validaciones.texto(trabajo, "Trabajo");
        this.fallaOrigen = falla;
        this.equipoAfectado = falla.getEquipo();
        this.prioridad = equipoAfectado.calcularPrioridadInicial();
    }
    public void asignar(EmpleadoMantenimiento empleado) {
        Validaciones.exigir(fechaCierre == null, "La orden ya está cerrada.");
        this.empleadoAsignado = Validaciones.requerido(empleado, "Empleado");
        equipoAfectado.iniciarReparacion();
    }
    public void elevarPrioridad() { if (fechaCierre == null && prioridad.ordinal() < Prioridad.ALTA.ordinal()) prioridad = Prioridad.ALTA; }
    public void cerrar(String fecha, String detalle) {
        Validaciones.exigir(fechaCierre == null, "La orden ya está cerrada.");
        Validaciones.requerido(empleadoAsignado, "Técnico asignado");
        Fechas.noAnterior(fechaApertura, fecha);
        Validaciones.texto(detalle, "Detalle de reparación");
        fallaOrigen.restablecer(fecha);
        this.detalleReparacion = detalle.trim();
        this.fechaCierre = fecha;
        equipoAfectado.actualizarTrasReparacion();
    }
    public int getNumero() { return numero; }
    public String getFechaApertura() { return fechaApertura; }
    public String getFechaCierre() { return fechaCierre; }
    public String getDescripcionTrabajo() { return descripcionTrabajo; }
    public String getDetalleReparacion() { return detalleReparacion; }
    public Prioridad getPrioridad() { return prioridad; }
    public Falla getFallaOrigen() { return fallaOrigen; }
    public Equipo getEquipoAfectado() { return equipoAfectado; }
    public EmpleadoMantenimiento getEmpleadoAsignado() { return empleadoAsignado; }
    @Override public String toString() { return "Orden " + numero + " | " + equipoAfectado.getCodigo() + " | " + prioridad + " | " + (fechaCierre == null ? "ABIERTA" : "CERRADA"); }
}
