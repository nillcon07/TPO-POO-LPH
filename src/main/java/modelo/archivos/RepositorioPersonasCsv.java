package modelo.archivos;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import modelo.personas.Persona;
import modelo.repositorios.RepositorioPersonasBase;
import modelo.comun.Validaciones;

public final class RepositorioPersonasCsv extends RepositorioPersonasBase {
    private final Path archivo;
    private final List<Persona> personas;
    public RepositorioPersonasCsv(Path archivo) {
        this.archivo = Validaciones.requerido(archivo, "Archivo de personas");
        try {
            if (!Files.exists(archivo)) {
                Files.createDirectories(archivo.toAbsolutePath().getParent());
                Files.writeString(archivo, "documento;nombre;apellido;telefono;correoElectronico\n");
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el catálogo de personas.", e);
        }
        List<Persona> leidas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo.toString()))) {
            if (!"documento;nombre;apellido;telefono;correoElectronico".equals(lector.readLine())) throw new IllegalArgumentException("Cabecera de personas inválida.");
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] c = separarCampos(linea);
                Persona nueva = new Persona(c[1], c[2], c[0], c[3], c[4]);
                for (Persona persona : leidas) {
                    Validaciones.exigir(!persona.getDocumento().equals(nueva.getDocumento()), "Documento duplicado en personas.");
                }
                leidas.add(nueva);
            }
        } catch (IOException e) { throw new IllegalStateException("No se pudo leer el catálogo de personas.", e); }
        personas = new ArrayList<>(leidas);
    }

    private String[] separarCampos(String linea) {
        String[] campos = {"", "", "", "", ""};
        int columna = 0;
        for (int i = 0; i < linea.length(); i++) {
            char caracter = linea.charAt(i);
            if (caracter == ';') {
                columna++;
                if (columna >= campos.length) throw new IllegalArgumentException("Fila de personas inválida.");
            } else {
                campos[columna] += caracter;
            }
        }
        if (columna != campos.length - 1) throw new IllegalArgumentException("Fila de personas inválida.");
        return campos;
    }

    @Override public void agregarPersona(Persona persona) {
        validarAlta(persona);
        String linea = persona.getDocumento() + ";" + persona.getNombre() + ";" + persona.getApellido()
                + ";" + persona.getTelefono() + ";" + persona.getCorreoElectronico();
        try {
            int ultimoCaracter = -1;
            try (BufferedReader lector = new BufferedReader(new FileReader(archivo.toString()))) {
                int caracter;
                while ((caracter = lector.read()) != -1) ultimoCaracter = caracter;
            }
            try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo.toString(), true))) {
                if (ultimoCaracter != -1 && ultimoCaracter != '\n') escritor.newLine();
                escritor.write(linea);
                escritor.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la persona en el catálogo.", e);
        }
        personas.add(persona);
    }

    @Override public List<Persona> listar() { return new ArrayList<>(personas); }
}
