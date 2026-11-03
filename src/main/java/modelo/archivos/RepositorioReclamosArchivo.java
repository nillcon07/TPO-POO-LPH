package modelo.archivos;

import modelo.reclamos.Reclamo;
import modelo.repositorios.RepositorioReclamos;
import modelo.comun.ReglaNegocioException;
import java.util.*;
import java.nio.file.*;

public class RepositorioReclamosArchivo implements RepositorioReclamos {
    private final Path archivo;
    private final List<Reclamo> reclamos;

    public RepositorioReclamosArchivo(Path archivo) {
        this.archivo = archivo;
        this.reclamos = new ArrayList<>();
        if (Files.exists(archivo)) {
            Object objeto = AlmacenObjetos.leer(archivo);
            if (!(objeto instanceof List<?>)) throw new IllegalStateException("Archivo de reclamos inválido.");
            List<?> lista = (List<?>) objeto;
            for (Object elemento : lista) {
                if (!(elemento instanceof Reclamo)) throw new IllegalStateException("Archivo de reclamos inválido.");
                reclamos.add((Reclamo) elemento);
            }
        }
    }

    @Override public void guardar(Reclamo reclamo) {
        List<Reclamo> candidatos = new ArrayList<>(reclamos);
        Iterator<Reclamo> iterador = candidatos.iterator();
        while (iterador.hasNext()) {
            if (iterador.next().getNumero() == reclamo.getNumero()) iterador.remove();
        }
        candidatos.add(reclamo);
        AlmacenObjetos.guardar(archivo, new ArrayList<>(candidatos));
        reclamos.clear();
        reclamos.addAll(candidatos);
    }

    @Override public Reclamo buscarPorNumero(int numero) {
        for (Reclamo reclamo : reclamos) {
            if (reclamo.getNumero() == numero) return reclamo;
        }
        throw new ReglaNegocioException("Reclamo inexistente.");
    }

    @Override public List<Reclamo> listarTodos() { return new ArrayList<>(reclamos); }
}
