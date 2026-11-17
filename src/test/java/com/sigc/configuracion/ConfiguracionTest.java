package com.sigc.configuracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

/** El reloj de negocio no debe producir fracciones de segundo (MySQL las redondea al guardar). */
class ConfiguracionTest {
    @Test
    void elRelojDeNegocioTienePrecisionDeSegundos() {
        Clock reloj = new Configuracion().reloj();
        for (int i = 0; i < 50; i++) assertThat(LocalDateTime.now(reloj).getNano()).isZero();
        assertThat(reloj.getZone()).isEqualTo(ZoneId.of("America/Argentina/Buenos_Aires"));
    }
}
