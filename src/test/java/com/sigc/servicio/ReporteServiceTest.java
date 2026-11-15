package com.sigc.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sigc.dominio.*;
import com.sigc.servicio.vista.FiltroVentas;
import com.sigc.servicio.vista.ReporteVentas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** Calculo de reportes sobre ventas en memoria (sin base de datos). */
class ReporteServiceTest {
    private final Cliente cliente = new Cliente("Ana", "Gomez", "20111222", null, null, LocalDate.of(2026, 1, 1));
    private final Vendedor paz = new Vendedor("Luis", "Paz", "30111222", null, null, "V1");
    private final Vendedor ruiz = new Vendedor("Eva", "Ruiz", "30333444", null, null, "V2");
    private final List<Venta> ventas = new ArrayList<>();

    private void vender(Vehiculo v, Vendedor vendedor, LocalDateTime cuando, String precio) throws Exception {
        v.vender();
        ventas.add(new Venta(v, cliente, vendedor, cuando, new BigDecimal(precio),
                ModalidadPago.CONTADO, UUID.randomUUID().toString()));
    }

    private static Auto auto() {
        return new Auto("Toyota", "Corolla", null, 2021, 1000, new BigDecimal("100"), "Gris",
                Combustible.NAFTA, Transmision.MANUAL, null, 4);
    }

    private static Moto moto() {
        return new Moto("Honda", "CB", null, 2022, 10, new BigDecimal("100"), "Rojo",
                Combustible.NAFTA, Transmision.MANUAL, null, TipoMoto.NAKED, 190);
    }

    private ReporteService servicio() {
        VentaService falso = new VentaService(null, null, null, null, null, null) {
            @Override
            public List<Venta> listar(FiltroVentas f) {
                return ventas;
            }
        };
        return new ReporteService(falso);
    }

    @Test
    void agrupaPorVendedorMesYTipo() throws Exception {
        vender(auto(), paz, LocalDateTime.of(2026, 3, 10, 9, 0), "1000.00");
        vender(moto(), paz, LocalDateTime.of(2026, 3, 20, 9, 0), "500.50");
        vender(auto(), ruiz, LocalDateTime.of(2026, 5, 2, 9, 0), "2000.00");

        ReporteVentas r = servicio().generar(null, null);

        assertThat(r.cantidad()).isEqualTo(3);
        assertThat(r.monto()).isEqualByComparingTo("3500.50");
        assertThat(r.porVendedor().get(0).etiqueta()).isEqualTo("Paz, Luis");
        assertThat(r.porVendedor().get(0).cantidad()).isEqualTo(2);
        assertThat(r.porVendedor().get(0).ancho()).isEqualTo(100);
        assertThat(r.porVendedor().get(1).ancho()).isEqualTo(50);
        assertThat(r.porPeriodo()).hasSize(2);
        assertThat(r.porPeriodo().get(0).etiqueta()).isEqualTo("03/2026");
        assertThat(r.porTipo()).hasSize(3);
        assertThat(r.porTipo().get(0).cantidad()).isEqualTo(2);
    }

    @Test
    void conRangoCompletoIncluyeLosMesesSinVentasEnCero() throws Exception {
        vender(auto(), paz, LocalDateTime.of(2026, 3, 10, 9, 0), "100.00");
        ReporteVentas r = servicio().generar(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 30));
        assertThat(r.porPeriodo()).hasSize(3);
        assertThat(r.porPeriodo().get(0).cantidad()).isZero();
        assertThat(r.porPeriodo().get(1).cantidad()).isEqualTo(1);
    }

    @Test
    void sinVentasNoDivideEntreCero() {
        ReporteVentas r = servicio().generar(null, null);
        assertThat(r.cantidad()).isZero();
        assertThat(r.porVendedor()).isEmpty();
        assertThat(r.porTipo().get(0).ancho()).isZero();
    }

    @Test
    void rechazaUnRangoInvertido() {
        assertThatThrownBy(() -> servicio().generar(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1)))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
