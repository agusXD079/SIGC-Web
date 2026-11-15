package com.sigc.servicio;

import com.sigc.dominio.ReglaNegocioException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Guarda fotos de vehiculos en una carpeta configurable (fuera del repositorio). Valida tamaño,
 * tipo declarado y firma real del archivo, y genera el nombre: el cliente nunca lo elige.
 */
@Component
public class FotoAlmacen {
    private static final long MAXIMO_BYTES = 3L * 1024 * 1024;
    private static final Pattern NOMBRE_VALIDO =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)");

    private final Path carpeta;

    /**
     * @param carpeta ruta de la carpeta de fotos (propiedad sigc.fotos.dir)
     */
    public FotoAlmacen(@Value("${sigc.fotos.dir}") String carpeta) {
        this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
    }

    /**
     * Valida y guarda una foto.
     *
     * @param archivo archivo recibido del formulario
     * @return nombre generado, para guardar en la base
     * @throws ReglaNegocioException si el archivo no es una imagen JPG, PNG o WebP de hasta 3 MB
     */
    public String guardar(MultipartFile archivo) {
        if (archivo.getSize() > MAXIMO_BYTES)
            throw new ReglaNegocioException("La foto supera el máximo de 3 MB.");
        try {
            byte[] contenido;
            try (InputStream in = archivo.getInputStream()) {
                contenido = in.readNBytes((int) MAXIMO_BYTES + 1);
            }
            if (contenido.length > MAXIMO_BYTES)
                throw new ReglaNegocioException("La foto supera el máximo de 3 MB.");
            String extension = extensionPorFirma(contenido);
            if (extension == null)
                throw new ReglaNegocioException("La foto debe ser una imagen JPG, PNG o WebP.");
            Files.createDirectories(carpeta);
            String nombre = UUID.randomUUID() + "." + extension;
            Files.write(carpeta.resolve(nombre), contenido);
            return nombre;
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar la foto.", ex);
        }
    }

    /**
     * Borra una foto si existe; ignora nulos y nombres invalidos.
     *
     * @param nombre nombre guardado en la base
     */
    public void eliminar(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) return;
        try {
            Files.deleteIfExists(carpeta.resolve(nombre));
        } catch (IOException ignorada) {
            // Un archivo huerfano no afecta la consistencia de la base.
        }
    }

    /**
     * @param nombre nombre solicitado por la URL
     * @return recurso si el nombre es valido y el archivo existe
     */
    public Optional<Resource> cargar(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) return Optional.empty();
        Path ruta = carpeta.resolve(nombre).normalize();
        if (!ruta.startsWith(carpeta) || !Files.isRegularFile(ruta)) return Optional.empty();
        return Optional.of(new PathResource(ruta));
    }

    /**
     * @param nombre nombre de archivo ya validado
     * @return tipo MIME segun la extension
     */
    public static String tipoMime(String nombre) {
        if (nombre.endsWith(".png")) return "image/png";
        if (nombre.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private static String extensionPorFirma(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF)
            return "jpg";
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G')
            return "png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P')
            return "webp";
        return null;
    }
}
