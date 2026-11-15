package com.sigc.repositorio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Construccion del filtro JDOQL de busqueda de personas (sin base de datos). */
class BusquedaPersonasTest {
    @Test
    void sinTextoSoloFiltraActivos() {
        BusquedaPersonas b = BusquedaPersonas.de("  ", false);
        assertThat(b.filtro).isEqualTo("activo == true");
        assertThat(b.parametros).isEmpty();
        assertThat(BusquedaPersonas.de(null, true).filtro).isEmpty();
    }

    @Test
    void unApellidoConGuionSeBuscaTalCualYNoPorDni() {
        BusquedaPersonas b = BusquedaPersonas.de("Perez-Gomez", true);
        assertThat(b.parametros.get("t0")).isEqualTo("perez-gomez");
        assertThat(b.parametros.containsKey("d0")).isFalse();
        assertThat(b.filtro.contains("dni.indexOf")).isFalse();
    }

    @Test
    void unDniConPuntosSeBuscaSoloPorDigitos() {
        BusquedaPersonas b = BusquedaPersonas.de("30.111", true);
        assertThat(b.parametros.get("d0")).isEqualTo("30111");
        assertThat(b.filtro.contains("dni.indexOf(:d0)")).isTrue();
    }

    @Test
    void cadaPalabraDebeCoincidirYElTextoNuncaVaEnLaExpresion() {
        BusquedaPersonas b = BusquedaPersonas.de("ana 'x; drop", false);
        assertThat(b.filtro.contains("drop")).isFalse();
        assertThat(b.filtro.contains("&&")).isTrue();
        assertThat(b.parametros.get("t2")).isEqualTo("drop");
    }
}
