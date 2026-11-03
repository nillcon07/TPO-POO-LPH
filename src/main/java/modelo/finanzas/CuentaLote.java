package modelo.finanzas;

import java.io.Serializable;
import java.util.*;
import modelo.comunidad.Lote;
import modelo.comun.*;


/** Experto en el historial financiero de un lote. Los intereses nunca se capitalizan. */
public class CuentaLote implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Lote lote;
    private final List<Expensa> expensas = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();
    private final List<AsientoAjuste> ajustes = new ArrayList<>();
    private boolean moroso;
    private int episodioMorosidad;

    public CuentaLote(Lote lote) { this.lote = Validaciones.requerido(lote, "Lote"); }

    public boolean tienePeriodo(String periodo) {
        for (Expensa expensa : expensas) {
            if (expensa.getPeriodo().equals(periodo)) return true;
        }
        return false;
    }

    public Expensa emitir(String periodo, double importe) {
        return emitir(periodo, importe, Fechas.inicioPeriodo(periodo));
    }

    public Expensa emitir(String periodo, double importe, String fechaEmision) {
        Validaciones.exigir(!tienePeriodo(periodo), "Ya existe una expensa de ese lote y período.");
        Expensa expensa = new Expensa(periodo, importe, lote, fechaEmision);
        expensas.add(expensa);
        return expensa;
    }

    public Pago registrarPago(String fecha, double importe, MedioPago medio) {
        if (!pagos.isEmpty()) Validaciones.exigir(Fechas.compararFechas(fecha, pagos.get(pagos.size() - 1).getFecha()) >= 0,
                "Los pagos se registran cronológicamente.");
        if (!ajustes.isEmpty()) Validaciones.exigir(Fechas.compararFechas(fecha, ajustes.get(ajustes.size() - 1).getFecha()) >= 0,
                "El pago no puede preceder al último ajuste.");
        Pago nuevo = new Pago(pagos.size() + 1, fecha, importe, medio, lote);
        List<Pago> candidatos = new ArrayList<>(pagos);
        candidatos.add(nuevo);
        EstadoCuenta resultado = calcular(fecha, candidatos, ajustes);
        List<ImputacionPago> originales = new ArrayList<>();
        for (ImputacionPago imputacion : resultado.getImputacionesEfectivas()) {
            if (imputacion.getPago() == nuevo) originales.add(imputacion);
        }
        nuevo.registrarImputaciones(originales);
        for (ImputacionPago imputacion : originales) imputacion.getExpensa().registrarImputacion(imputacion);
        pagos.add(nuevo);
        return nuevo;
    }

    public AsientoAjuste ajustar(int numeroPago, String fecha, double monto, String motivo) {
        Pago pago = null;
        for (Pago candidato : pagos) {
            if (candidato.getNumero() == numeroPago) { pago = candidato; break; }
        }
        Validaciones.exigir(pago != null, "Pago inexistente.");
        if (!pagos.isEmpty()) Validaciones.exigir(Fechas.compararFechas(fecha, pagos.get(pagos.size() - 1).getFecha()) >= 0,
                "La fecha del ajuste debe ser posterior o igual al último pago.");
        if (!ajustes.isEmpty()) Validaciones.exigir(Fechas.compararFechas(fecha, ajustes.get(ajustes.size() - 1).getFecha()) >= 0,
                "Los ajustes se registran cronológicamente.");
        AsientoAjuste nuevo = new AsientoAjuste(fecha, monto, motivo, pago);
        List<AsientoAjuste> candidatos = new ArrayList<>(ajustes);
        candidatos.add(nuevo);
        calcular(fecha, pagos, candidatos);
        ajustes.add(nuevo);
        return nuevo;
    }

    public EstadoCuenta consultar(String fecha) { return calcular(fecha, pagos, ajustes); }

    private EstadoCuenta calcular(String fecha, List<Pago> movimientos, List<AsientoAjuste> correcciones) {
        Fechas.fecha(fecha);
        List<Expensa> ordenadas = new ArrayList<>();
        for (Expensa expensa : expensas) {
            if (Fechas.compararFechas(expensa.getFechaEmision(), fecha) <= 0) ordenadas.add(expensa);
        }
        Collections.sort(ordenadas, new Comparator<Expensa>() {
            @Override public int compare(Expensa primera, Expensa segunda) {
                return primera.getPeriodo().compareTo(segunda.getPeriodo());
            }
        });
        List<Acumulador> cuentas = new ArrayList<>();
        for (Expensa expensa : ordenadas) cuentas.add(new Acumulador(expensa));
        List<ImputacionPago> efectivas = new ArrayList<>();
        for (Pago pago : movimientos) {
            String diaPago = pago.getFecha();
            if (Fechas.compararFechas(diaPago, fecha) > 0) continue;
            double efectivo = pago.getImporteTotal();
            for (AsientoAjuste ajuste : correcciones) {
                if (ajuste.getPagoCorregido() == pago && Fechas.compararFechas(ajuste.getFecha(), fecha) <= 0) {
                    efectivo += ajuste.getMontoAjuste();
                }
            }
            double restante = Validaciones.dinero(efectivo);
            Validaciones.exigir(restante >= 0, "El ajuste produce un pago efectivo negativo.");
            for (Acumulador cuenta : cuentas) {
                if (restante == 0) break;
                if (Fechas.compararFechas(diaPago, cuenta.expensa.getFechaEmision()) < 0) continue;
                cuenta.devengar(diaPago);
                double interes = Validaciones.dinero(cuenta.intereses);
                if (restante < interes) interes = restante;
                restante = Validaciones.dinero(restante - interes);
                cuenta.intereses = Validaciones.dinero(cuenta.intereses) - interes;
                double capital = cuenta.capital;
                if (restante < capital) capital = restante;
                restante = Validaciones.dinero(restante - capital);
                cuenta.capital = Validaciones.dinero(cuenta.capital - capital);
                if (interes + capital > 0) {
                    cuenta.reconocido += interes + capital;
                    efectivas.add(new ImputacionPago(pago, cuenta.expensa, interes, capital));
                }
            }
            Validaciones.exigir(restante == 0, "El importe genera un anticipo o saldo a favor, incluso al reconstruir pagos posteriores.");
        }
        List<SaldoExpensa> saldos = new ArrayList<>();
        for (Acumulador cuenta : cuentas) {
            cuenta.devengar(fecha);
            saldos.add(new SaldoExpensa(cuenta.expensa, cuenta.capital, cuenta.intereses, cuenta.reconocido, fecha));
        }
        return new EstadoCuenta(saldos, efectivas);
    }

    public Notificacion verificarMorosidad(String fechaHora) {
        EstadoCuenta estado = consultar(Fechas.dia(fechaHora));
        if (moroso && estado.getDeudaVencida() == 0) moroso = false;
        if (!moroso && estado.getPeriodosImpagos() > 3) {
            moroso = true;
            episodioMorosidad++;
            return new Notificacion("MORA-" + lote.getNumero() + "-" + episodioMorosidad, fechaHora, lote.getTitular(),
                    "Morosidad del lote " + lote.getNumero() + ": más de tres períodos vencidos con deuda", estado.getDeudaVencida());
        }
        return null;
    }

    public boolean isMoroso() { return moroso; }
    public List<Expensa> getExpensas() { return new ArrayList<>(expensas); }
    public List<Pago> getPagos() { return new ArrayList<>(pagos); }
    public List<AsientoAjuste> getAjustes() { return new ArrayList<>(ajustes); }

    private static final class Acumulador {
        private final Expensa expensa;
        private double capital, intereses, reconocido;
        private String ultimo;

        private Acumulador(Expensa expensa) {
            this.expensa = expensa;
            capital = expensa.getImporteBase();
            ultimo = expensa.getFechaVencimiento();
        }

        private void devengar(String hasta) {
            long dias = Fechas.dias(ultimo, hasta);
            if (dias > 0) {
                intereses += capital * (0.03 / 30) * dias;
                ultimo = hasta;
            }
        }
    }
}
