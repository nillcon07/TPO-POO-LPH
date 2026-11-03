package modelo.repositorios;

import java.util.List;
import modelo.comunidad.Barrio;
import modelo.reclamos.Reclamo;
import modelo.comun.ReglaNegocioException;

public final class RepositorioReclamosMemoria implements RepositorioReclamos {
    private final Barrio barrio;

    public RepositorioReclamosMemoria(Barrio barrio) { this.barrio = barrio; }
    @Override public void guardar(Reclamo reclamo) { barrio.guardarReclamo(reclamo); }

    @Override public Reclamo buscarPorNumero(int numero) {
        for (Reclamo reclamo : barrio.getReclamos()) {
            if (reclamo.getNumero() == numero) return reclamo;
        }
        throw new ReglaNegocioException("Reclamo inexistente.");
    }

    @Override public List<Reclamo> listarTodos() { return barrio.getReclamos(); }
}
