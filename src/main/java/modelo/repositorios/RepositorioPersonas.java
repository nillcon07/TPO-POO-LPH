package modelo.repositorios;

import java.util.List;
import modelo.personas.Persona;

public interface RepositorioPersonas {
    List<Persona> listar();
    void agregarPersona(Persona persona);
    void validarAlta(Persona persona);
    Persona buscarPorDocumento(String documento);
}
