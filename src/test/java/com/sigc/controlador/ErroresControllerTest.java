package com.sigc.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.sigc.dominio.RegistroNoEncontradoException;
import com.sigc.dominio.ReglaNegocioException;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

/** Codigos HTTP de la pagina de error, sin base de datos. */
class ErroresControllerTest {
    /** Controlador de prueba que solo lanza las excepciones a traducir. */
    @Controller
    static class Rutas {
        @GetMapping("/inexistente")
        String inexistente() {
            throw new RegistroNoEncontradoException("No se encontró el registro solicitado.");
        }

        @GetMapping("/regla")
        String regla() {
            throw new ReglaNegocioException("Regla incumplida.");
        }
    }

    private final MockMvc mvc =
            MockMvcBuilders.standaloneSetup(new Rutas())
                    .setControllerAdvice(new ErroresController())
                    .build();

    @Test
    void registroInexistenteResponde404ConMensajeClaro() throws Exception {
        mvc.perform(get("/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("mensaje", "No se encontró el registro solicitado."));
    }

    @Test
    void reglaDeNegocioSigueRespondiendo400() throws Exception {
        mvc.perform(get("/regla"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("mensaje", "Regla incumplida."));
    }
}
