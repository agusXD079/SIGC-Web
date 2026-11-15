package com.sigc.controlador;

import com.sigc.dominio.ReglaNegocioException;
import com.sigc.servicio.PersistenciaException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Traduce excepciones a la pagina de error sin exponer detalles internos. */
@ControllerAdvice
public class ErroresController {
    private static final Logger LOG = LoggerFactory.getLogger(ErroresController.class);

    /** Regla de negocio incumplida o registro inexistente. */
    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String regla(ReglaNegocioException ex, Model model) {
        model.addAttribute("titulo", "No se pudo completar la acción");
        model.addAttribute("mensaje", ex.getMessage());
        return "error";
    }

    /** Foto mayor al limite configurado. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String grande(Model model) {
        model.addAttribute("titulo", "Archivo demasiado grande");
        model.addAttribute("mensaje", "La foto supera el máximo de 3 MB.");
        return "error";
    }

    /** Fallo de base de datos: se registra y se muestra un mensaje generico. */
    @ExceptionHandler(PersistenciaException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String persistencia(PersistenciaException ex, Model model) {
        LOG.error("Error de persistencia", ex);
        model.addAttribute("titulo", "Error de base de datos");
        model.addAttribute("mensaje", "No se pudo completar la operación. Verifique que MySQL esté activo.");
        return "error";
    }
}
