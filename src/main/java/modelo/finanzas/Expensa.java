package modelo.finanzas;

import java.io.Serializable;
import java.util.*;
import modelo.comunidad.Lote;
import modelo.comun.*;

public class Expensa implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String periodo, fechaVencimiento, fechaEmision;
    private final double importeBase;
    private final Lote lote;
    private final List<ImputacionPago> imputaciones = new ArrayList<>();
    public Expensa(String periodo, double importeBase, Lote lote) {
        this(periodo, importeBase, lote, Fechas.inicioPeriodo(periodo));
    }
    public Expensa(String periodo, double importeBase, Lote lote, String fechaEmision) {
        this.fechaVencimiento = Fechas.vencimientoPeriodo(periodo);
        Fechas.fecha(fechaEmision);
        this.fechaEmision = fechaEmision;
        this.periodo = periodo;
        Validaciones.exigir(Validaciones.esFinito(importeBase) && importeBase >= 0, "El importe de la expensa no puede ser negativo.");
        this.importeBase = Validaciones.dinero(importeBase);
        this.lote = Validaciones.requerido(lote, "Lote");
    }
    void registrarImputacion(ImputacionPago imputacion) { imputaciones.add(imputacion); }
    public String getPeriodo() { return periodo; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public String getFechaEmision() { return fechaEmision; }
    public double getImporteBase() { return importeBase; }
    public Lote getLote() { return lote; }
    /** Imputaciones originales, conservadas aun después de ajustes. */
    public List<ImputacionPago> getImputaciones() { return new ArrayList<>(imputaciones); }
    public EstadoExpensa getEstado(String fechaConsulta) {
        for (SaldoExpensa saldo : lote.getCuenta().consultar(fechaConsulta).getSaldos()) {
            if (saldo.getExpensa() == this) return saldo.getEstado();
        }
        throw new ReglaNegocioException("La expensa todavía no estaba emitida en la fecha consultada.");
    }
}
