package Backend;

/**
 * Clase que representa una observacion cientifica realizada sobre un
 * microorganismo especifico de una muestra bajo determinadas condiciones.
 */
public class Observacion {

    // Atributos de la observacion
    private String id;
    private String idMuestra;
    private String idMicroorganismo;
    private String fecha;
    private CondicionAmbiental condicionAnalisis;
    private String descripcionResultados;

    /**
     * Constructor vacio requerido para la persistencia en JSON.
     */
    public Observacion() {
    }

    /**
     * Constructor completo para registrar una nueva observacion en el laboratorio.
     */
    public Observacion(
            String id,
            String idMuestra,
            String idMicroorganismo,
            String fecha,
            CondicionAmbiental condicionAnalisis,
            String descripcionResultados
    ) {
        this.id = id;
        this.idMuestra = idMuestra;
        this.idMicroorganismo = idMicroorganismo;
        this.fecha = fecha;
        this.condicionAnalisis = (condicionAnalisis != null) ? condicionAnalisis.copiar() : null;
        this.descripcionResultados = descripcionResultados;
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdMuestra() {
        return idMuestra;
    }

    public void setIdMuestra(String idMuestra) {
        this.idMuestra = idMuestra;
    }

    public String getIdMicroorganismo() {
        return idMicroorganismo;
    }

    public void setIdMicroorganismo(String idMicroorganismo) {
        this.idMicroorganismo = idMicroorganismo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public CondicionAmbiental getCondicionAnalisis() {
        return condicionAnalisis;
    }

    public void setCondicionAnalisis(CondicionAmbiental condicionAnalisis) {
        this.condicionAnalisis = condicionAnalisis;
    }

    public String getDescripcionResultados() {
        return descripcionResultados;
    }

    public void setDescripcionResultados(String descripcionResultados) {
        this.descripcionResultados = descripcionResultados;
    }

}