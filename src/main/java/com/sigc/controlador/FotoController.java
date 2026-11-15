package com.sigc.controlador;

import com.sigc.servicio.FotoAlmacen;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;

/** Sirve las fotos guardadas; solo acepta nombres generados por el sistema. */
@Controller
public class FotoController {
    private final FotoAlmacen almacen;

    /**
     * @param almacen carpeta de fotos
     */
    public FotoController(FotoAlmacen almacen) {
        this.almacen = almacen;
    }

    /**
     * @param nombre nombre del archivo
     * @return la imagen o 404
     */
    @GetMapping("/fotos/{nombre:.+}")
    public ResponseEntity<Resource> foto(@PathVariable String nombre) {
        return almacen.cargar(nombre)
                .map(
                        recurso ->
                                ResponseEntity.ok()
                                        .contentType(MediaType.parseMediaType(FotoAlmacen.tipoMime(nombre)))
                                        .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                                        .header("X-Content-Type-Options", "nosniff")
                                        .body(recurso))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
