package modelo.repositorios;

import modelo.reclamos.Reclamo;

import java.util.List;

public interface RepositorioReclamos {
    void guardar(Reclamo reclamo);
    Reclamo buscarPorNumero(int numero);
    List<Reclamo> listarTodos();
}
