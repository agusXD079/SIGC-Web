package com.sigc.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ValidacionesTest {
    @Test
    void textoObligatorioLimpiaEspaciosYControlaElLargo() {
        assertThat(Validaciones.texto("  Ford ", "La marca", 10)).isEqualTo("Ford");
        assertThatThrownBy(() -> Validaciones.texto("   ", "La marca", 10)).hasMessage("La marca es obligatorio.");
        assertThatThrownBy(() -> Validaciones.texto("x".repeat(11), "La marca", 10)).hasMessageContaining("10");
    }

    @Test
    void textoOpcionalAdmiteVacioOnulo() {
        assertThat(Validaciones.textoOpcional(null, "Color", 5)).isEmpty();
        assertThat(Validaciones.textoOpcional("  ", "Color", 5)).isEmpty();
        assertThat(Validaciones.textoOpcional(" Rojo ", "Color", 5)).isEqualTo("Rojo");
    }

    @Test
    void importeSeRedondeaADosDecimalesYDebeSerPositivo() {
        BigDecimal max = new BigDecimal("1000");
        assertThat(Validaciones.importe(new BigDecimal("10.005"), "Precio", max)).isEqualByComparingTo("10.01");
        assertThatThrownBy(() -> Validaciones.importe(new BigDecimal("-1"), "Precio", max))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> Validaciones.importe(new BigDecimal("1000.01"), "Precio", max))
                .hasMessageContaining("máximo");
    }

    @Test
    void enteroControlaElRango() {
        assertThat(Validaciones.entero(5, "N", 1, 5)).isEqualTo(5);
        assertThatThrownBy(() -> Validaciones.entero(6, "N", 1, 5)).hasMessageContaining("entre 1 y 5");
    }
}
