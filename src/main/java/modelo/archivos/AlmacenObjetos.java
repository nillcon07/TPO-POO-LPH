package modelo.archivos;

import java.io.*;
import java.nio.file.*;

final class AlmacenObjetos {
    private AlmacenObjetos() { }

    static Object leer(Path archivo) {
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo.toString()))) {
            return entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo leer " + archivo + ". Se conservó el archivo original.", e);
        }
    }

    static void guardar(Path archivo, Serializable objeto) {
        Path destino = archivo.toAbsolutePath();
        Path temporal = Paths.get(destino.toString() + ".tmp");
        try {
            Files.createDirectories(destino.getParent());
            try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(temporal.toString()))) {
                salida.writeObject(objeto);
            }
            // El archivo anterior se reemplaza una vez que se terminó de escribir el nuevo.
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar " + archivo + ".", e);
        }
    }
}
