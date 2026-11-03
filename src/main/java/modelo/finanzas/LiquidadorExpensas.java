package modelo.finanzas;

import java.util.*;
import modelo.comunidad.Lote;
import modelo.comun.*;


public final class LiquidadorExpensas {
    public List<Expensa> emitir(List<Lote> lotes, String periodo, double presupuesto) {
        return emitir(lotes, periodo, presupuesto, Fechas.inicioPeriodo(periodo));
    }

    public List<Expensa> emitir(List<Lote> lotes, String periodo, double presupuesto, String fechaEmision) {
        Fechas.periodo(periodo);
        Fechas.fecha(fechaEmision);
        long total = Validaciones.centavos(Validaciones.positivo(presupuesto, "Presupuesto"));
        Validaciones.exigir(total > 0, "El presupuesto mínimo es un centavo.");
        Validaciones.exigir(!lotes.isEmpty(), "No hay lotes registrados.");
        List<Lote> ordenados = new ArrayList<>(lotes);
        Collections.sort(ordenados, new Comparator<Lote>() {
            @Override public int compare(Lote primero, Lote segundo) {
                return Integer.compare(primero.getNumero(), segundo.getNumero());
            }
        });
        double superficie = 0;
        for (Lote lote : ordenados) {
            Validaciones.exigir(!lote.getCuenta().tienePeriodo(periodo), "La emisión contiene períodos duplicados.");
            superficie += lote.getSuperficie();
        }
        Validaciones.positivo(superficie, "Superficie total");
        long[] importes = new long[ordenados.size()];
        long suma = 0;
        for (int i = 0; i < importes.length; i++) {
            importes[i] = Validaciones.centavos(total / 100.0 * ordenados.get(i).getSuperficie() / superficie);
            suma += importes[i];
        }
        long diferencia = total - suma;
        for (int i = 0; diferencia != 0; i = (i + 1) % importes.length) {
            if (diferencia > 0) { importes[i]++; diferencia--; }
            else if (importes[i] > 0) { importes[i]--; diferencia++; }
        }
        List<Expensa> emitidas = new ArrayList<>();
        for (int i = 0; i < importes.length; i++) {
            emitidas.add(ordenados.get(i).getCuenta().emitir(periodo, importes[i] / 100.0, fechaEmision));
        }
        return emitidas;
    }
}
