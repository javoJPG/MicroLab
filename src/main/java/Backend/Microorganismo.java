package Backend;

public abstract class Microorganismo implements ISimulable {
    // Constantes de apoyo para tipos y estados (sin usar enum)
    public static final String TIPO_BACTERIA = "Bacteria";
    public static final String TIPO_PROTOZOO = "Protozoo";
    public static final String TIPO_HONGO = "Hongo Microscopico";
    public static final String TIPO_EXTREMOFILO = "Extremofilo";

    public static final String ESTADO_OPTIMO = "En desarrollo optimo";
    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_REDUCIDO = "Actividad reducida";
    public static final String ESTADO_DETENIDO = "Desarrollo detenido";
    public static final String ESTADO_INACTIVO = "Inactivo / Sin sobrevivencia";

    protected String id;
    protected String idMuestra;
    protected String nombre;
    protected String tipo;
    protected String estado;
    protected CondicionAmbiental condicionCrecimiento;
    protected String capacidadReproductiva;
    protected String alimentacion;
    protected String locomocion;
    protected String respuestaCondiciones;

    // Variables dinámicas de seguimiento y simulación
    protected int cantidadOrganismos;
    protected double nivelActividad; // 0.0 a 100.0
    protected double crecimiento;    // Porcentaje de crecimiento en el ciclo

    public Microorganismo() {
    }

    public Microorganismo(String id, String idMuestra, String nombre, String tipo,
                          String estado, CondicionAmbiental condicionCrecimiento,
                          String capacidadReproductiva, String alimentacion,
                          String locomocion, int cantidadInicial) {
        this.id = id;
        this.idMuestra = idMuestra;
        this.nombre = nombre;
        this.tipo = tipo;
        this.estado = estado;
        this.condicionCrecimiento = condicionCrecimiento;
        this.capacidadReproductiva = capacidadReproductiva;
        this.alimentacion = alimentacion;
        this.locomocion = locomocion;
        this.cantidadOrganismos = cantidadInicial;
        this.nivelActividad = 75.0;
        this.crecimiento = 0.0;
        this.respuestaCondiciones = "Condiciones iniciales registradas.";
    }

    protected void limitarValoresSimulacion() {
        if (this.cantidadOrganismos < 0) {
            this.cantidadOrganismos = 0;
        }
        if (this.nivelActividad < 0.0) {
            this.nivelActividad = 0.0;
        } else if (this.nivelActividad > 100.0) {
            this.nivelActividad = 100.0;
        }
        if (this.cantidadOrganismos == 0) {
            this.nivelActividad = 0.0;
            this.crecimiento = 0.0;
            this.estado = ESTADO_INACTIVO;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdMuestra() { return idMuestra; }
    public void setIdMuestra(String idMuestra) { this.idMuestra = idMuestra; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public CondicionAmbiental getCondicionCrecimiento() { return condicionCrecimiento; }
    public void setCondicionCrecimiento(CondicionAmbiental condicionCrecimiento) {
        this.condicionCrecimiento = condicionCrecimiento;
    }

    public String getCapacidadReproductiva() { return capacidadReproductiva; }
    public void setCapacidadReproductiva(String capacidadReproductiva) {
        this.capacidadReproductiva = capacidadReproductiva;
    }

    public String getAlimentacion() { return alimentacion; }
    public void setAlimentacion(String alimentacion) { this.alimentacion = alimentacion; }

    public String getLocomocion() { return locomocion; }
    public void setLocomocion(String locomocion) { this.locomocion = locomocion; }

    public String getRespuestaCondiciones() { return respuestaCondiciones; }
    public void setRespuestaCondiciones(String respuestaCondiciones) {
        this.respuestaCondiciones = respuestaCondiciones;
    }

    public int getCantidadOrganismos() { return cantidadOrganismos; }
    public void setCantidadOrganismos(int cantidadOrganismos) {
        this.cantidadOrganismos = cantidadOrganismos;
    }

    public double getNivelActividad() { return nivelActividad; }
    public void setNivelActividad(double nivelActividad) { this.nivelActividad = nivelActividad; }

    public double getCrecimiento() { return crecimiento; }
    public void setCrecimiento(double crecimiento) { this.crecimiento = crecimiento; }
}