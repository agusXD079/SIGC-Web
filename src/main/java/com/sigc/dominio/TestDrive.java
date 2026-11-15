package com.sigc.dominio;

import java.time.LocalDateTime;

import javax.jdo.annotations.*;

/**
 * Prueba de manejo de un vehiculo por un cliente. Mientras esta EN_CURSO ocupa la referencia
 * unica {@code vehiculoAbierto}; al finalizar esa referencia se anula. Asi la base impide dos
 * pruebas abiertas del mismo vehiculo y se conservan todas las finalizadas como historial.
 */
@PersistenceCapable(table = "TEST_DRIVES", detachable = "true")
public class TestDrive {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    @Column(name = "ID")
    private long id;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "VEHICULO_ID", allowsNull = "false")
    private Vehiculo vehiculo;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "CLIENTE_ID", allowsNull = "false")
    private Cliente cliente;

    /** Vendedor que acompaña la prueba; opcional. */
    @Persistent(defaultFetchGroup = "true")
    @Column(name = "VENDEDOR_ID", allowsNull = "true")
    private Vendedor vendedor;

    @Column(name = "FECHA_INICIO", allowsNull = "false")
    private LocalDateTime fechaInicio;

    @Column(name = "FECHA_FIN", allowsNull = "true")
    private LocalDateTime fechaFin;

    @Column(allowsNull = "false", length = 20)
    private EstadoTestDrive estado;

    @Column(length = 300)
    private String observaciones;

    @Unique(name = "UQ_TD_VEHICULO_ABIERTO")
    @Column(name = "VEHICULO_ABIERTO_ID", allowsNull = "true")
    private Vehiculo vehiculoAbierto;

    /** Constructor reservado a JDO. */
    protected TestDrive() {}

    /**
     * Abre una prueba para un vehiculo que ya paso a EN_TEST_DRIVE en la misma transaccion.
     *
     * @param vehiculo vehiculo probado
     * @param cliente cliente activo que lo prueba
     * @param vendedor vendedor activo que acompaña, o null
     * @param fechaInicio momento de inicio
     * @param observaciones notas opcionales
     */
    public TestDrive(
            Vehiculo vehiculo,
            Cliente cliente,
            Vendedor vendedor,
            LocalDateTime fechaInicio,
            String observaciones) {
        this.vehiculo = Validaciones.requerido(vehiculo, "El vehículo");
        this.cliente = Validaciones.requerido(cliente, "El cliente");
        this.vendedor = vendedor;
        this.fechaInicio = Validaciones.requerido(fechaInicio, "La fecha de inicio");
        this.observaciones = Validaciones.textoOpcional(observaciones, "Las observaciones", 300);
        if (vehiculo.getEstado() != EstadoVehiculo.EN_TEST_DRIVE)
            throw new ReglaNegocioException("El vehículo debe estar EN_TEST_DRIVE para abrir la prueba.");
        this.estado = EstadoTestDrive.EN_CURSO;
        this.vehiculoAbierto = vehiculo;
    }

    /**
     * Cierra la prueba. El servicio devuelve el vehiculo a DISPONIBLE en la misma transaccion.
     *
     * @param fechaFin momento de finalizacion, no anterior al inicio
     * @param observacionesFinales notas opcionales; si vienen vacias se conservan las anteriores
     * @throws ReglaNegocioException si ya estaba finalizada o la fecha es invalida
     */
    public void finalizar(LocalDateTime fechaFin, String observacionesFinales) {
        if (estado != EstadoTestDrive.EN_CURSO)
            throw new ReglaNegocioException("La prueba de manejo ya fue finalizada.");
        Validaciones.requerido(fechaFin, "La fecha de fin");
        if (fechaFin.isBefore(fechaInicio))
            throw new ReglaNegocioException("La finalización no puede ser anterior al inicio.");
        String notas = Validaciones.textoOpcional(observacionesFinales, "Las observaciones", 300);
        if (!notas.isEmpty()) this.observaciones = notas;
        this.fechaFin = fechaFin;
        this.estado = EstadoTestDrive.FINALIZADO;
        this.vehiculoAbierto = null;
    }

    /**
     * @return true mientras la prueba no fue finalizada
     */
    public boolean isEnCurso() {
        return estado == EstadoTestDrive.EN_CURSO;
    }

    /**
     * @return identidad generada por MySQL
     */
    public long getId() {
        return id;
    }

    /**
     * @return vehiculo probado
     */
    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    /**
     * @return cliente que realizo la prueba
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * @return vendedor que acompaño, o null
     */
    public Vendedor getVendedor() {
        return vendedor;
    }

    /**
     * @return momento de inicio
     */
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    /**
     * @return momento de fin, o null si sigue en curso
     */
    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    /**
     * @return estado de la prueba
     */
    public EstadoTestDrive getEstado() {
        return estado;
    }

    /**
     * @return notas, posiblemente vacias
     */
    public String getObservaciones() {
        return observaciones;
    }
}
