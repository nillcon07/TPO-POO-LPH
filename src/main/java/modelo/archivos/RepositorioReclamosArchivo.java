package main.java.modelo.archivos;

import main.java.modelo.reclamos.Reclamo;
import main.java.modelo.repositorios.RepositorioReclamos;

import java.util.List;

public class RepositorioReclamosArchivo implements RepositorioReclamos {
    @Override
    public void guardar(Reclamo reclamo) {

    }

    @Override
    public Reclamo buscarPorNumero(int numero) {
        return null;
    }

    @Override
    public List<Reclamo> listarTodos() {
        return null;
    }
}
