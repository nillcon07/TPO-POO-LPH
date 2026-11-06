package modelo.repositorios;

import java.util.ArrayList;
import java.util.List;
import modelo.comun.Validaciones;
import modelo.personas.Persona;

public final class RepositorioPersonasMemoria extends RepositorioPersonasBase {
    private final List<Persona> personas = new ArrayList<>();

    public RepositorioPersonasMemoria(List<Persona> iniciales) {
        Validaciones.requerido(iniciales, "Personas iniciales");
        for (Persona persona : iniciales) agregarPersona(persona);
    }

    @Override public void agregarPersona(Persona persona) {
        validarAlta(persona);
        personas.add(persona);
    }

    @Override public List<Persona> listar() { return new ArrayList<>(personas); }
}
