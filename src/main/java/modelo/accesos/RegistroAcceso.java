package main.java.modelo.accesos;

import modelo.comun.*;
import modelo.personas.*;

public class RegistroAcceso implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String fechaHoraIngreso;
    private String fechaHoraEgreso;
    private final AutorizacionAcceso autorizacion;
    private final Guardia guardia;
    public RegistroAcceso(String fechaHora, AutorizacionAcceso autorizacion, Guardia guardia) {
        Validaciones.requerido(autorizacion, "Autorización"); Validaciones.requerido(guardia, "Guardia");
        Validaciones.exigir(guardia.getPuestoAsignado() == PuestoGuardia.PORTERIA, "El guardia debe estar asignado a portería.");
        Validaciones.exigir(autorizacion.estaVigente(fechaHora), "La autorización no está vigente.");
        this.fechaHoraIngreso = fechaHora; this.autorizacion = autorizacion; this.guardia = guardia;
    }
    public void registrarEgreso(String fechaHora) {
        Validaciones.exigir(fechaHoraEgreso == null, "El ingreso ya tiene egreso.");
        Fechas.noAnterior(fechaHoraIngreso, fechaHora);
        fechaHoraEgreso = fechaHora;
    }
    public String getFechaHoraIngreso() { return fechaHoraIngreso; }
    public String getFechaHoraEgreso() { return fechaHoraEgreso; }
    public AutorizacionAcceso getAutorizacion() { return autorizacion; }
    public Guardia getGuardia() { return guardia; }
    @Override public String toString() { return autorizacion.getPersonaAutorizada() + " | ingreso " + fechaHoraIngreso + " | egreso " + (fechaHoraEgreso == null ? "PENDIENTE" : fechaHoraEgreso); }
}
