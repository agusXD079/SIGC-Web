package com.sigc.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Reglas del dominio de vehiculos: estados, bajas logicas y validaciones. No requieren base. */
class VehiculoTest {

    private static Auto auto() {
        return new Auto("Toyota", "Corolla", "XEI", 2021, 35000, new BigDecimal("18500000"),
                "Gris", Combustible.NAFTA, Transmision.AUTOMATICA, "Unico dueño", 4);
    }

    private static Moto moto() {
        return new Moto("Honda", "CB 190", null, 2022, 8000, new BigDecimal("3200000"),
                "Rojo", Combustible.NAFTA, Transmision.MANUAL, null, TipoMoto.NAKED, 190);
    }

    private static Camion camion() {
        return new Camion("Mercedes-Benz", "Atego", null, 2019, 210000, new BigDecimal("55000000"),
                "Blanco", Combustible.DIESEL, Transmision.MANUAL, null, new BigDecimal("8.50"));
    }

    @Test
    void vehiculoNuevoEstaDisponibleYActivoConPrecioDeDosDecimales() {
        Auto a = auto();
        assertThat(a.getEstado()).isEqualTo(EstadoVehiculo.DISPONIBLE);
        assertThat(a.isActivo()).isTrue();
        assertThat(a.getPrecio()).isEqualByComparingTo("18500000.00");
        assertThat(a.getPrecio().scale()).isEqualTo(2);
    }

    @Test
    void venderCambiaElEstadoYNoSePuedeVenderDosVeces() throws Exception {
        Auto a = auto();
        a.vender();
        assertThat(a.getEstado()).isEqualTo(EstadoVehiculo.VENDIDO);
        assertThatThrownBy(a::vender)
                .isInstanceOf(VehiculoNoDisponibleException.class)
                .hasMessageContaining("Vendido");
    }

    @Test
    void testDriveBloqueaLaVentaYFinalizarLoDevuelveADisponible() throws Exception {
        Moto m = moto();
        m.hacerTestDrive();
        assertThat(m.getEstado()).isEqualTo(EstadoVehiculo.EN_TEST_DRIVE);
        assertThatThrownBy(m::vender).isInstanceOf(VehiculoNoDisponibleException.class);
        assertThatThrownBy(m::hacerTestDrive).isInstanceOf(VehiculoNoDisponibleException.class);
        m.finalizarTestDrive();
        assertThat(m.getEstado()).isEqualTo(EstadoVehiculo.DISPONIBLE);
    }

    @Test
    void finalizarTestDriveSinTestDriveEsRechazado() {
        assertThatThrownBy(() -> auto().finalizarTestDrive())
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void bajaLogicaSoloConVehiculoDisponible() throws Exception {
        Auto libre = auto();
        libre.darDeBaja();
        assertThat(libre.isActivo()).isFalse();
        assertThatThrownBy(libre::vender).isInstanceOf(VehiculoNoDisponibleException.class);
        libre.reactivar();
        assertThat(libre.isActivo()).isTrue();

        Auto vendido = auto();
        vendido.vender();
        assertThatThrownBy(vendido::darDeBaja).isInstanceOf(ReglaNegocioException.class);

        Moto enPrueba = moto();
        enPrueba.hacerTestDrive();
        assertThatThrownBy(enPrueba::darDeBaja).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void unVehiculoVendidoNoSePuedeModificar() throws Exception {
        Auto a = auto();
        a.vender();
        assertThatThrownBy(() -> a.actualizarDatos("Toyota", "Corolla", "", 2021, 35000,
                        new BigDecimal("1"), "Gris", Combustible.NAFTA, Transmision.MANUAL, ""))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("vendido");
    }

    @Test
    void rechazaDatosInvalidos() {
        int anioFuturo = LocalDate.now().getYear() + 2;
        assertThatThrownBy(() -> new Auto("", "X", null, 2020, 0, BigDecimal.TEN, null,
                        Combustible.NAFTA, Transmision.MANUAL, null, 4))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("marca");
        assertThatThrownBy(() -> new Auto("A", "B", null, anioFuturo, 0, BigDecimal.TEN, null,
                        Combustible.NAFTA, Transmision.MANUAL, null, 4))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("año");
        assertThatThrownBy(() -> new Auto("A", "B", null, 2020, -1, BigDecimal.TEN, null,
                        Combustible.NAFTA, Transmision.MANUAL, null, 4))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("kilometraje");
        assertThatThrownBy(() -> new Auto("A", "B", null, 2020, 0, BigDecimal.ZERO, null,
                        Combustible.NAFTA, Transmision.MANUAL, null, 4))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("precio");
        assertThatThrownBy(() -> new Auto("A", "B", null, 2020, 0, null, null,
                        Combustible.NAFTA, Transmision.MANUAL, null, 4))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> new Auto("A", "B", null, 2020, 0, BigDecimal.TEN, null,
                        null, Transmision.MANUAL, null, 4))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("combustible");
    }

    @Test
    void rechazaDatosEspecificosInvalidos() {
        assertThatThrownBy(() -> new Auto("A", "B", null, 2020, 0, BigDecimal.TEN, null,
                        Combustible.NAFTA, Transmision.MANUAL, null, 9))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("puertas");
        assertThatThrownBy(() -> new Moto("A", "B", null, 2020, 0, BigDecimal.TEN, null,
                        Combustible.NAFTA, Transmision.MANUAL, null, null, 150))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> new Moto("A", "B", null, 2020, 0, BigDecimal.TEN, null,
                        Combustible.NAFTA, Transmision.MANUAL, null, TipoMoto.CROSS, 10))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("cilindrada");
        assertThatThrownBy(() -> new Camion("A", "B", null, 2020, 0, BigDecimal.TEN, null,
                        Combustible.DIESEL, Transmision.MANUAL, null, BigDecimal.ZERO))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void polimorfismoSobreLaJerarquiaOriginal() {
        List<Vehiculo> inventario = List.of(auto(), moto(), camion());
        assertThat(inventario).extracting(Vehiculo::getTipo).containsExactly("Auto", "Moto", "Camion");
        assertThat(inventario).extracting(Vehiculo::getDetalleEspecifico)
                .containsExactly("4 puertas", "Naked · 190 cc", "Carga 8.5 tn");
        assertThat(inventario.get(0).mostrarInfo()).startsWith("[Auto] Toyota Corolla");
        assertThat(TipoVehiculo.de(inventario.get(1))).isEqualTo(TipoVehiculo.MOTO);
    }

    @Test
    void tituloIncluyeLaVersionSoloSiExiste() {
        assertThat(auto().getTitulo()).isEqualTo("Toyota Corolla XEI");
        assertThat(moto().getTitulo()).isEqualTo("Honda CB 190");
    }
}
