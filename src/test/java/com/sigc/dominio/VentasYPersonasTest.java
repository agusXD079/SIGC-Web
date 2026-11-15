package com.sigc.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Reglas de personas, ventas y pruebas de manejo del dominio. No requieren base de datos. */
class VentasYPersonasTest {
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 5, 4, 10, 30);

    private static Cliente cliente(String apellido, String dni) {
        return new Cliente("Ana", apellido, dni, "3794000000", "ana@mail.com", LocalDate.of(2026, 1, 1));
    }

    private static Vendedor vendedor() {
        return new Vendedor("Luis", "Paz", "30111222", null, null, "v-001");
    }

    private static Auto auto() {
        return new Auto("Toyota", "Corolla", "XEI", 2021, 35000, new BigDecimal("18500000"),
                "Gris", Combustible.NAFTA, Transmision.AUTOMATICA, null, 4);
    }

    @Test
    void dniSeNormalizaYSeValida() {
        assertThat(Validaciones.dni("30.111.222")).isEqualTo("30111222");
        assertThat(Validaciones.dni("1234567")).isEqualTo("1234567");
        assertThatThrownBy(() -> Validaciones.dni("12345")).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> Validaciones.dni("12a45678")).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> Validaciones.dni(" ")).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void emailEsOpcionalPeroSiSeIngresaDebeSerValido() {
        assertThat(Validaciones.email(null)).isEmpty();
        assertThat(Validaciones.email(" a@b.com ")).isEqualTo("a@b.com");
        assertThatThrownBy(() -> Validaciones.email("sin-arroba")).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void personasSeIgualanPorDniYSeOrdenanPorApellido() {
        Cliente a = cliente("Zapata", "20111222");
        Cliente b = cliente("Alvarez", "20111222");
        Cliente c = cliente("Alvarez", "20333444");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        Set<Persona> conjunto = new HashSet<>(List.of(a, c));
        assertThat(conjunto.add(b)).isFalse();

        List<Persona> lista = new ArrayList<>(List.of(a, c));
        Collections.sort(lista);
        assertThat(lista.get(0).getApellido()).isEqualTo("Alvarez");
    }

    @Test
    void legajoSeNormalizaEnMayusculasYRechazaCaracteresInvalidos() {
        assertThat(vendedor().getLegajo()).isEqualTo("V-001");
        assertThatThrownBy(() -> new Vendedor("A", "B", "30111222", null, null, "v 001"))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void bajaYReactivacionSonLogicas() {
        Cliente c = cliente("Gomez", "20111222");
        c.darDeBaja();
        assertThat(c.isActivo()).isFalse();
        c.reactivar();
        assertThat(c.isActivo()).isTrue();
    }

    @Test
    void ventaExigeVehiculoVendidoYClaveUuid() throws Exception {
        Auto a = auto();
        String clave = UUID.randomUUID().toString();
        assertThatThrownBy(() -> new Venta(a, cliente("Gomez", "20111222"), vendedor(), AHORA,
                new BigDecimal("1000"), ModalidadPago.CONTADO, clave))
                .isInstanceOf(ReglaNegocioException.class);

        a.vender();
        Venta v = new Venta(a, cliente("Gomez", "20111222"), vendedor(), AHORA,
                new BigDecimal("18000000"), ModalidadPago.TRANSFERENCIA, clave);
        assertThat(v.getPrecioVenta()).isEqualByComparingTo("18000000.00");
        assertThat(v.getModalidadPago()).isEqualTo(ModalidadPago.TRANSFERENCIA);
        assertThatThrownBy(() -> new Venta(a, cliente("Gomez", "20111222"), vendedor(), AHORA,
                new BigDecimal("18000000"), ModalidadPago.CONTADO, "no-es-uuid"))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void vehiculoVendidoNoSePuedeVenderNiProbarDeNuevo() throws Exception {
        Auto a = auto();
        a.vender();
        assertThatThrownBy(a::vender).isInstanceOf(VehiculoNoDisponibleException.class);
        assertThatThrownBy(a::hacerTestDrive).isInstanceOf(VehiculoNoDisponibleException.class);
    }

    @Test
    void testDriveFinalizarDevuelveElVehiculoADisponible() throws Exception {
        Auto a = auto();
        a.hacerTestDrive();
        TestDrive t = new TestDrive(a, cliente("Gomez", "20111222"), null, AHORA, "Ruta 12");
        assertThat(t.isEnCurso()).isTrue();
        assertThatThrownBy(a::vender).isInstanceOf(VehiculoNoDisponibleException.class);

        t.finalizar(AHORA.plusMinutes(30), "Sin novedades");
        a.finalizarTestDrive();
        assertThat(t.getEstado()).isEqualTo(EstadoTestDrive.FINALIZADO);
        assertThat(t.getObservaciones()).isEqualTo("Sin novedades");
        assertThat(a.getEstado()).isEqualTo(EstadoVehiculo.DISPONIBLE);
        assertThatThrownBy(() -> t.finalizar(AHORA.plusHours(1), null))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void testDriveNoPuedeFinalizarAntesDeIniciar() throws Exception {
        Auto a = auto();
        a.hacerTestDrive();
        TestDrive t = new TestDrive(a, cliente("Gomez", "20111222"), null, AHORA, null);
        assertThatThrownBy(() -> t.finalizar(AHORA.minusMinutes(1), null))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void testDriveSoloSeAbreConVehiculoEnPrueba() {
        assertThatThrownBy(() -> new TestDrive(auto(), cliente("Gomez", "20111222"), null, AHORA, null))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
