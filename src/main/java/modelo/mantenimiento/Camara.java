package modelo.mantenimiento;

public class Camara extends Equipo {
    private static final long serialVersionUID = 1L;
    private final String resolucion;
    private final int diasGrabacion;
    public Camara(String codigo, modelo.comunidad.Sector sector, String fechaInstalacion, String resolucion, int diasGrabacion) {
        super(codigo, sector, fechaInstalacion);
        this.resolucion = modelo.comun.Validaciones.texto(resolucion, "Resolución");
        modelo.comun.Validaciones.exigir(diasGrabacion > 0, "Los días de grabación deben ser positivos.");
        this.diasGrabacion = diasGrabacion;
    }
    public String getResolucion() { return resolucion; }
    public int getDiasGrabacion() { return diasGrabacion; }
    @Override public modelo.comun.Prioridad calcularPrioridadInicial() { return modelo.comun.Prioridad.ALTA; }
    @Override public String getTipoEquipo() { return "Camara"; }
}
