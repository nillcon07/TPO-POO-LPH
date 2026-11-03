package main.java.modelo.mantenimiento;

import modelo.comun.*;
import modelo.personas.Persona;

public class Falla implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final String fechaReporte, fechaInicioInterrupcion, descripcion;
    private String fechaRestablecimiento;
    private final Persona informante;
    private final Equipo equipo;
    private OrdenMantenimiento orden;
    Falla(int numero, String reporte, String inicio, String descripcion, Persona informante, Equipo equipo) {
        Validaciones.exigir(numero > 0, "Número de falla inválido.");
        Fechas.momento(reporte);
        boolean estimada = inicio == null || inicio.isBlank();
        if (estimada) inicio = reporte;
        Fechas.noAnterior(inicio, reporte);
        Validaciones.exigir(Fechas.compararFechas(Fechas.dia(inicio), equipo.getFechaInstalacion()) >= 0, "La interrupción precede a la instalación.");
        this.numero = numero;
        this.fechaReporte = reporte;
        this.fechaInicioInterrupcion = inicio;
        this.descripcion = Validaciones.texto(descripcion, "Descripción") + (estimada ? " (Inicio estimado con la fecha de reporte)" : "");
        this.informante = Validaciones.requerido(informante, "Informante");
        this.equipo = equipo;
    }
    public OrdenMantenimiento generarOrden(int numero, String fecha, String trabajo) {
        Validaciones.exigir(orden == null, "La falla ya tiene una orden de mantenimiento.");
        Fechas.noAnterior(fechaReporte, fecha);
        OrdenMantenimiento nueva = new OrdenMantenimiento(numero, fecha, trabajo, this);
        orden = nueva;
        return nueva;
    }
    void restablecer(String fecha) { Fechas.noAnterior(fechaReporte, fecha); fechaRestablecimiento = fecha; }
    public boolean requiereAlerta(String fecha) {
        return equipo instanceof Camara && fechaRestablecimiento == null
                && Fechas.minutosEntre(fechaInicioInterrupcion, fecha) > 48 * 60;
    }
    public int getNumero() { return numero; }
    public String getFechaReporte() { return fechaReporte; }
    public String getFechaInicioInterrupcion() { return fechaInicioInterrupcion; }
    public String getFechaRestablecimiento() { return fechaRestablecimiento; }
    public String getDescripcion() { return descripcion; }
    public Persona getInformante() { return informante; }
    public Equipo getEquipo() { return equipo; }
    public OrdenMantenimiento getOrden() { return orden; }
    @Override public String toString() { return "Falla " + numero + " | " + equipo.getCodigo() + " | " + descripcion; }
}
