package modelo.repositorios;

import modelo.personas.Persona;
import modelo.comun.ReglaNegocioException;
import modelo.comun.Validaciones;

public abstract class RepositorioPersonasBase implements RepositorioPersonas {
    @Override
    public void validarAlta(Persona persona) {
        Validaciones.requerido(persona, "Persona");
        for (Persona existente : listar()) {
            Validaciones.exigir(!existente.equals(persona), "Ya existe una persona con ese documento.");
        }
        String[] campos = {persona.getDocumento(), persona.getNombre(), persona.getApellido(),
                persona.getTelefono(), persona.getCorreoElectronico()};
        for (String campo : campos) {
            for (int i = 0; i < campo.length(); i++) {
                char caracter = campo.charAt(i);
                Validaciones.exigir(caracter != ';' && caracter != '\n' && caracter != '\r',
                        "Los datos de una persona no pueden contener punto y coma ni saltos de línea.");
            }
        }
    }

    @Override
    public Persona buscarPorDocumento(String documento) {
        for (Persona persona : listar()) {
            if (persona.getDocumento().equals(documento)) return persona;
        }
        throw new ReglaNegocioException("La persona no existe en la fuente de personas.");
    }
}
