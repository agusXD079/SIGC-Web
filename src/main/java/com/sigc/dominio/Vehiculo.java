package com.sigc.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.jdo.annotations.*;

/**
 * Raiz de la jerarquia Vehiculo, Auto, Moto y Camion (conservada del sistema original). Cada
 * subclase tiene su propia tabla unida por identidad. Las transiciones de estado viven aqui para
 * que ninguna capa superior pueda saltearlas.
 */
@PersistenceCapable(table = "VEHICULOS", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME, column = "CLASE")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "VERSION_FILA")
public abstract class Vehiculo implements ITransaccionable {
    /** Precio maximo admitido (cabe en DECIMAL(14,2)). */
    public static final BigDecimal PRECIO_MAXIMO = new BigDecimal("999999999999.99");

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    @Column(name = "ID")
    private long id;

    @Column(allowsNull = "false", length = 60)
    private String marca;

    @Column(allowsNull = "false", length = 80)
    private String modelo;

    @Column(name = "VERSION_MODELO", length = 80)
    private String versionModelo;

    @Column(allowsNull = "false")
    private int anio;

    @Column(allowsNull = "false")
    private int kilometraje;

    @Column(allowsNull = "false", length = 14, scale = 2)
    private BigDecimal precio;

    @Column(length = 40)
    private String color;

    @Column(allowsNull = "false", length = 20)
    private Combustible combustible;

    @Column(allowsNull = "false", length = 20)
    private Transmision transmision;

    @Column(length = 1000)
    private String descripcion;

    @Column(allowsNull = "false", length = 20)
    private EstadoVehiculo estado;

    /** Solo el nombre del archivo; la carpeta es configurable (sigc.fotos.dir). */
    @Column(length = 120)
    private String foto;

    /** Baja logica: un vehiculo inactivo no se lista pero conserva su historial. */
    @Column(allowsNull = "false")
    private boolean activo;

    /** Constructor reservado a JDO. */
    protected Vehiculo() {}

    /**
     * Crea un vehiculo valido, DISPONIBLE y activo.
     *
     * @param marca marca
     * @param modelo modelo
     * @param versionModelo version o terminacion, opcional
     * @param anio anio de fabricacion
     * @param kilometraje kilometros recorridos
     * @param precio precio de lista
     * @param color color, opcional
     * @param combustible combustible
     * @param transmision transmision
     * @param descripcion texto libre, opcional
     */
    protected Vehiculo(
            String marca,
            String modelo,
            String versionModelo,
            int anio,
            int kilometraje,
            BigDecimal precio,
            String color,
            Combustible combustible,
            Transmision transmision,
            String descripcion) {
        actualizarDatos(
                marca, modelo, versionModelo, anio, kilometraje, precio, color, combustible,
                transmision, descripcion);
        this.estado = EstadoVehiculo.DISPONIBLE;
        this.activo = true;
    }

    /**
     * Modifica los datos comerciales sin tocar identidad, estado ni foto.
     *
     * @throws ReglaNegocioException si el vehiculo esta vendido o algun dato es invalido
     */
    public void actualizarDatos(
            String marca,
            String modelo,
            String versionModelo,
            int anio,
            int kilometraje,
            BigDecimal precio,
            String color,
            Combustible combustible,
            Transmision transmision,
            String descripcion) {
        if (estado == EstadoVehiculo.VENDIDO)
            throw new ReglaNegocioException("Un vehículo vendido no puede modificarse.");
        this.marca = Validaciones.texto(marca, "La marca", 60);
        this.modelo = Validaciones.texto(modelo, "El modelo", 80);
        this.versionModelo = Validaciones.textoOpcional(versionModelo, "La versión", 80);
        this.anio = Validaciones.entero(anio, "El año", 1950, LocalDate.now().getYear() + 1);
        this.kilometraje = Validaciones.entero(kilometraje, "El kilometraje", 0, 3_000_000);
        this.precio = Validaciones.importe(precio, "El precio", PRECIO_MAXIMO);
        this.color = Validaciones.textoOpcional(color, "El color", 40);
        this.combustible = Validaciones.requerido(combustible, "El combustible");
        this.transmision = Validaciones.requerido(transmision, "La transmisión");
        this.descripcion = Validaciones.textoOpcional(descripcion, "La descripción", 1000);
    }

    /**
     * @return nombre del tipo concreto ("Auto", "Moto" o "Camion")
     */
    public abstract String getTipo();

    /**
     * @return datos propios de la subclase para listados y fichas
     */
    public abstract String getDetalleEspecifico();

    @Override
    public void vender() throws VehiculoNoDisponibleException {
        exigirDisponible("vender");
        estado = EstadoVehiculo.VENDIDO;
    }

    @Override
    public void hacerTestDrive() throws VehiculoNoDisponibleException {
        exigirDisponible("hacer test drive de");
        estado = EstadoVehiculo.EN_TEST_DRIVE;
    }

    /**
     * Devuelve el vehiculo a DISPONIBLE al terminar la prueba de manejo.
     *
     * @throws ReglaNegocioException si no estaba en test drive
     */
    public void finalizarTestDrive() {
        if (estado != EstadoVehiculo.EN_TEST_DRIVE)
            throw new ReglaNegocioException("El vehículo no está en test drive.");
        estado = EstadoVehiculo.DISPONIBLE;
    }

    private void exigirDisponible(String operacion) throws VehiculoNoDisponibleException {
        if (!activo)
            throw new VehiculoNoDisponibleException(
                    "No se puede " + operacion + " un vehículo dado de baja: " + marca + " " + modelo);
        if (estado != EstadoVehiculo.DISPONIBLE)
            throw new VehiculoNoDisponibleException(
                    "No se puede " + operacion + " " + marca + " " + modelo
                            + " (estado: " + estado.getEtiqueta() + ").");
    }

    /**
     * Baja logica. No se permite con el vehiculo vendido ni en prueba de manejo.
     *
     * @throws ReglaNegocioException si el estado lo impide
     */
    public void darDeBaja() {
        if (estado == EstadoVehiculo.VENDIDO)
            throw new ReglaNegocioException(
                    "Un vehículo vendido no se da de baja: forma parte del historial de ventas.");
        if (estado == EstadoVehiculo.EN_TEST_DRIVE)
            throw new ReglaNegocioException(
                    "Finalice el test drive antes de dar de baja el vehículo.");
        activo = false;
    }

    /** Vuelve a habilitar un vehiculo dado de baja. */
    public void reactivar() {
        activo = true;
    }

    /**
     * @param nombreArchivo nombre generado por el servicio de fotos, o null para quitarla
     */
    public void asignarFoto(String nombreArchivo) {
        this.foto = nombreArchivo;
    }

    @Override
    public String mostrarInfo() {
        return "[" + getTipo() + "] " + marca + " " + modelo + " - $" + precio + " - "
                + estado.getEtiqueta();
    }

    /**
     * @return identidad generada por MySQL; cero mientras el objeto es transitorio
     */
    public long getId() {
        return id;
    }

    /**
     * @return marca
     */
    public String getMarca() {
        return marca;
    }

    /**
     * @return modelo
     */
    public String getModelo() {
        return modelo;
    }

    /**
     * @return version o terminacion, posiblemente vacia
     */
    public String getVersionModelo() {
        return versionModelo;
    }

    /**
     * @return anio de fabricacion
     */
    public int getAnio() {
        return anio;
    }

    /**
     * @return kilometraje
     */
    public int getKilometraje() {
        return kilometraje;
    }

    /**
     * @return precio de lista con dos decimales
     */
    public BigDecimal getPrecio() {
        return precio;
    }

    /**
     * @return color, posiblemente vacio
     */
    public String getColor() {
        return color;
    }

    /**
     * @return combustible
     */
    public Combustible getCombustible() {
        return combustible;
    }

    /**
     * @return transmision
     */
    public Transmision getTransmision() {
        return transmision;
    }

    /**
     * @return descripcion, posiblemente vacia
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * @return estado comercial actual
     */
    public EstadoVehiculo getEstado() {
        return estado;
    }

    /**
     * @return nombre del archivo de foto o null
     */
    public String getFoto() {
        return foto;
    }

    /**
     * @return false si fue dado de baja
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * @return marca, modelo y version para titulos
     */
    public String getTitulo() {
        return marca + " " + modelo + (versionModelo == null || versionModelo.isEmpty() ? "" : " " + versionModelo);
    }
}
