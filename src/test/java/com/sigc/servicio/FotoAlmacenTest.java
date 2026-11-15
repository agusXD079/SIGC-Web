package com.sigc.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sigc.dominio.ReglaNegocioException;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/** Validacion de fotos: firma real del archivo, tamaño y nombres seguros. Usa una carpeta temporal. */
class FotoAlmacenTest {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};

    @TempDir Path carpeta;

    @Test
    void guardaUnaImagenValidaConNombreGenerado() {
        FotoAlmacen almacen = new FotoAlmacen(carpeta.toString());
        String nombre = almacen.guardar(new MockMultipartFile("foto", "../../auto.exe.png", "image/png", PNG));
        assertThat(nombre).matches("[0-9a-f-]{36}\\.png");
        assertThat(carpeta.resolve(nombre)).exists();
        assertThat(almacen.cargar(nombre)).isPresent();
        almacen.eliminar(nombre);
        assertThat(carpeta.resolve(nombre)).doesNotExist();
    }

    @Test
    void ignoraElTipoDeclaradoYValidaLaFirma() {
        FotoAlmacen almacen = new FotoAlmacen(carpeta.toString());
        assertThat(almacen.guardar(new MockMultipartFile("foto", "x.txt", "text/plain", JPG))).endsWith(".jpg");
        assertThatThrownBy(() -> almacen.guardar(
                        new MockMultipartFile("foto", "x.png", "image/png", "no soy imagen".getBytes())))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void rechazaArchivosMayoresA3Mb() {
        FotoAlmacen almacen = new FotoAlmacen(carpeta.toString());
        byte[] grande = new byte[3 * 1024 * 1024 + 1];
        System.arraycopy(PNG, 0, grande, 0, PNG.length);
        assertThatThrownBy(() -> almacen.guardar(new MockMultipartFile("foto", "g.png", "image/png", grande)))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("3 MB");
    }

    @Test
    void noSirveNombresQueNoFueronGenerados() {
        FotoAlmacen almacen = new FotoAlmacen(carpeta.toString());
        assertThat(almacen.cargar("../pom.xml")).isEmpty();
        assertThat(almacen.cargar("secreto.png")).isEmpty();
        assertThat(almacen.cargar(null)).isEmpty();
        almacen.eliminar("../../etc/passwd"); // no debe fallar ni borrar nada
    }
}
