package Backend;

import java.util.ArrayList;
import java.util.List;

public class Muestra {
    private String id;
    private String origen;
    private String fechaRecoleccion;
    private String responsableAnalisis;
    private CondicionAmbiental condicionInicial;
    private String observacionesGenerales;
    private List<Microorganismo> microorganismos;
    private List<String> historialAnalisis;

    public Muestra() {
        this.microorganismos = new ArrayList<>();
        this.historialAnalisis = new ArrayList<>();
    }

    public Muestra(String id, String origen, String fechaRecoleccion,
                   String responsableAnalisis, CondicionAmbiental condicionInicial,
                   String observacionesGenerales) {
        this.id = id;
        this.origen = origen;
        this.fechaRecoleccion = fechaRecoleccion;
        this.responsableAnalisis = responsableAnalisis;
        this.condicionInicial = condicionInicial;
        this.observacionesGenerales = observacionesGenerales;
        this.microorganismos = new ArrayList<>();
        this.historialAnalisis = new ArrayList<>();
        registrarEventoHistorial("Muestra registrada el " + fechaRecoleccion + " por " + responsableAnalisis);
    }

    public void agregarMicroorganismo(Microorganismo micro) {
        this.microorganismos.add(micro);
        registrarEventoHistorial("Microorganismo identificado: " + micro.getNombre() + " (" + micro.getTipo() + ")");
    }

    public Microorganismo buscarMicroorganismoPorId(String idMicro) {
        for (Microorganismo m : microorganismos) {
            if (m.getId().equalsIgnoreCase(idMicro)) {
                return m;
            }
        }
        return null;
    }

    public void registrarEventoHistorial(String evento) {
        if (this.historialAnalisis == null) {
            this.historialAnalisis = new ArrayList<>();
        }
        this.historialAnalisis.add(evento);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getFechaRecoleccion() { return fechaRecoleccion; }
    public void setFechaRecoleccion(String fechaRecoleccion) { this.fechaRecoleccion = fechaRecoleccion; }

    public String getResponsableAnalisis() { return responsableAnalisis; }
    public void setResponsableAnalisis(String responsableAnalisis) {
        this.responsableAnalisis = responsableAnalisis;
    }

    public CondicionAmbiental getCondicionInicial() { return condicionInicial; }
    public void setCondicionInicial(CondicionAmbiental condicionInicial) {
        this.condicionInicial = condicionInicial;
    }

    public String getObservacionesGenerales() { return observacionesGenerales; }
    public void setObservacionesGenerales(String observacionesGenerales) {
        this.observacionesGenerales = observacionesGenerales;
    }

    public List<Microorganismo> getMicroorganismos() { return microorganismos; }
    public void setMicroorganismos(List<Microorganismo> microorganismos) {
        this.microorganismos = microorganismos;
    }

    public List<String> getHistorialAnalisis() { return historialAnalisis; }
    public void setHistorialAnalisis(List<String> historialAnalisis) {
        this.historialAnalisis = historialAnalisis;
    }
}