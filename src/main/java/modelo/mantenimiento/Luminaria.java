package modelo.mantenimiento;

public class Luminaria extends Equipo {
    private static final long serialVersionUID = 1L;
    private final String tipoLampara;
    private final double potenciaWatts;
    public Luminaria(String codigo, modelo.comunidad.Sector sector, String fechaInstalacion, String tipoLampara, double potenciaWatts) {
        super(codigo, sector, fechaInstalacion);
        this.tipoLampara = modelo.comun.Validaciones.texto(tipoLampara, "Tipo de lámpara");
        this.potenciaWatts = modelo.comun.Validaciones.positivo(potenciaWatts, "Potencia");
    }
    public String getTipoLampara() { return tipoLampara; }
    public double getPotenciaWatts() { return potenciaWatts; }
    @Override public modelo.comun.Prioridad calcularPrioridadInicial() { return modelo.comun.Prioridad.MEDIA; }
    @Override public String getTipoEquipo() { return "Luminaria"; }
}
