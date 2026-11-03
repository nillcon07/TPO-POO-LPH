package main.java.modelo.repositorios;

import main.java.modelo.reclamos.Reclamo;

import java.util.List;

public interface RepositorioReclamos {
    void guardar(Reclamo reclamo);
    Reclamo buscarPorNumero(int numero);
    List<Reclamo> listarTodos();
}
