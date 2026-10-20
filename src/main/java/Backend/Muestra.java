package Backend;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa una muestra biologica recolectada y analizada
 * en el laboratorio, la cual contiene microorganismos identificados
 * y un historial de eventos.
 */
public class Muestra {

    // Atributos descriptivos de la muestra
    private String id;
    private String origen;
    private String fechaRecoleccion;
    private String responsableAnalisis;
    private CondicionAmbiental condicionInicial;
    private String observacionesGenerales;

    // Listas asociadas a los microorganismos e historial de analisis
    private List<Microorganismo> microorganismos;
    private List<String> historialAnalisis;

    /**
     * Constructor vacio requerido para la persistencia en JSON.
     */
    public Muestra() {
        this.microorganismos = new ArrayList<>();
        this.historialAnalisis = new ArrayList<>();
    }

    /**
     * Constructor completo para registrar una nueva muestra en el laboratorio.
     */
    public Muestra(
            String id,
            String origen,
            String fechaRecoleccion,
            String responsableAnalisis,
            CondicionAmbiental condicionInicial,
            String observacionesGenerales
    ) {
        this.id = id;
        this.origen = origen;
        this.fechaRecoleccion = fechaRecoleccion;
        this.responsableAnalisis = responsableAnalisis;
        this.condicionInicial = (condicionInicial != null) ? condicionInicial.copiar() : null;
        this.observacionesGenerales = observacionesGenerales;
        this.microorganismos = new ArrayList<>();
        this.historialAnalisis = new ArrayList<>();

        registrarEventoHistorial("Muestra registrada el " + fechaRecoleccion + " por " + responsableAnalisis);
    }

    /**
     * Asocia un microorganismo a la muestra y registra el evento en el historial.
     *
     * @param micro Microorganismo identificado en la muestra.
     */
    public void agregarMicroorganismo(Microorganismo micro) {
        if (micro == null) {
            return;
        }

        if (this.microorganismos == null) {
            this.microorganismos = new ArrayList<>();
        }

        this.microorganismos.add(micro);
        registrarEventoHistorial(
                "Microorganismo identificado: " + micro.getNombre() + " (" + micro.getTipo() + ")"
        );
    }

    /**
     * Busca un microorganismo dentro de esta muestra por su identificador.
     *
     * @param idMicro Identificador del microorganismo buscado.
     * @return El microorganismo encontrado, o null si no existe en la muestra.
     */
    public Microorganismo buscarMicroorganismoPorId(String idMicro) {
        if (idMicro == null || this.microorganismos == null) {
            return null;
        }

        for (Microorganismo m : this.microorganismos) {
            if (m != null && m.getId() != null && m.getId().equalsIgnoreCase(idMicro.trim())) {
                return m;
            }
        }

        return null;
    }

    /**
     * Agrega una entrada descriptiva al historial de eventos de la muestra.
     *
     * @param evento Texto del evento ocurrido sobre la muestra.
     */
    public void registrarEventoHistorial(String evento) {
        if (this.historialAnalisis == null) {
            this.historialAnalisis = new ArrayList<>();
        }

        this.historialAnalisis.add(evento);
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getFechaRecoleccion() {
        return fechaRecoleccion;
    }

    public void setFechaRecoleccion(String fechaRecoleccion) {
        this.fechaRecoleccion = fechaRecoleccion;
    }

    public String getResponsableAnalisis() {
        return responsableAnalisis;
    }

    public void setResponsableAnalisis(String responsableAnalisis) {
        this.responsableAnalisis = responsableAnalisis;
    }

    public CondicionAmbiental getCondicionInicial() {
        return condicionInicial;
    }

    public void setCondicionInicial(CondicionAmbiental condicionInicial) {
        this.condicionInicial = condicionInicial;
    }

    public String getObservacionesGenerales() {
        return observacionesGenerales;
    }

    public void setObservacionesGenerales(String observacionesGenerales) {
        this.observacionesGenerales = observacionesGenerales;
    }

    public List<Microorganismo> getMicroorganismos() {
        if (microorganismos == null) {
            microorganismos = new ArrayList<>();
        }
        return microorganismos;
    }

    public void setMicroorganismos(List<Microorganismo> microorganismos) {
        this.microorganismos = microorganismos;
    }

    public List<String> getHistorialAnalisis() {
        if (historialAnalisis == null) {
            historialAnalisis = new ArrayList<>();
        }
        return historialAnalisis;
    }

    public void setHistorialAnalisis(List<String> historialAnalisis) {
        this.historialAnalisis = historialAnalisis;
    }

}