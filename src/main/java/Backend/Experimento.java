package Backend;

import java.util.ArrayList;
import java.util.List;

public class Experimento {
    private String id;
    private String fecha;
    private String idMuestra;
    private String idMicroorganismo;
    private String nombreMicroorganismo;
    private String tipoMicroorganismo;
    private CondicionAmbiental condicionInicial;
    private CondicionAmbiental condicionActual;
    private int poblacionInicial;
    private double actividadInicial;
    private List<CicloSimulacion> ciclos;

    public Experimento() {
        this.ciclos = new ArrayList<>();
    }

    public Experimento(String id, String fecha, String idMuestra,
                       Microorganismo microorganismo, CondicionAmbiental condicionInicial) {
        this.id = id;
        this.fecha = fecha;
        this.idMuestra = idMuestra;
        this.idMicroorganismo = microorganismo.getId();
        this.nombreMicroorganismo = microorganismo.getNombre();
        this.tipoMicroorganismo = microorganismo.getTipo();
        this.condicionInicial = condicionInicial.copiar();
        this.condicionActual = condicionInicial.copiar();
        this.poblacionInicial = microorganismo.getCantidadOrganismos();
        this.actividadInicial = microorganismo.getNivelActividad();
        this.ciclos = new ArrayList<>();
    }

    public CicloSimulacion ejecutarNuevoCiclo(CondicionAmbiental nuevaCondicion, Microorganismo microorganismo) {
        this.condicionActual = nuevaCondicion.copiar();
        int numeroNuevoCiclo = this.ciclos.size() + 1;
        CicloSimulacion resultadoCiclo = microorganismo.ejecutarCiclo(numeroNuevoCiclo, this.condicionActual);
        this.ciclos.add(resultadoCiclo);
        return resultadoCiclo;
    }

    public String obtenerComparacionInicialFinal() {
        if (ciclos == null || ciclos.isEmpty()) {
            return "El experimento aun no tiene ciclos ejecutados.";
        }
        CicloSimulacion ultimo = ciclos.get(ciclos.size() - 1);
        int diferenciaPoblacion = ultimo.getCantidadOrganismos() - this.poblacionInicial;
        double diferenciaActividad = ultimo.getNivelActividad() - this.actividadInicial;

        return String.format(
                "=== COMPARACION EXPERIMENTO %s ===\n" +
                        "Microorganismo: %s (%s) | Muestra: %s | Ciclos ejecutados: %d\n" +
                        "- Condicion Inicial : %s\n" +
                        "- Condicion Final   : %s\n" +
                        "- Poblacion Inicial : %d -> Poblacion Final: %d (Variacion: %+d)\n" +
                        "- Actividad Inicial : %.1f%% -> Actividad Final: %.1f%% (Variacion: %+.1f%%)\n" +
                        "- Estado Final      : %s (%s)",
                id, nombreMicroorganismo, tipoMicroorganismo, idMuestra, ciclos.size(),
                condicionInicial.toString(),
                condicionActual.toString(),
                poblacionInicial, ultimo.getCantidadOrganismos(), diferenciaPoblacion,
                actividadInicial, ultimo.getNivelActividad(), diferenciaActividad,
                ultimo.getEstadoGeneral(), ultimo.getDescripcionRespuesta()
        );
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getIdMuestra() { return idMuestra; }
    public void setIdMuestra(String idMuestra) { this.idMuestra = idMuestra; }

    public String getIdMicroorganismo() { return idMicroorganismo; }
    public void setIdMicroorganismo(String idMicroorganismo) {
        this.idMicroorganismo = idMicroorganismo;
    }

    public String getNombreMicroorganismo() { return nombreMicroorganismo; }
    public void setNombreMicroorganismo(String nombreMicroorganismo) {
        this.nombreMicroorganismo = nombreMicroorganismo;
    }

    public String getTipoMicroorganismo() { return tipoMicroorganismo; }
    public void setTipoMicroorganismo(String tipoMicroorganismo) {
        this.tipoMicroorganismo = tipoMicroorganismo;
    }

    public CondicionAmbiental getCondicionInicial() { return condicionInicial; }
    public void setCondicionInicial(CondicionAmbiental condicionInicial) {
        this.condicionInicial = condicionInicial;
    }

    public CondicionAmbiental getCondicionActual() { return condicionActual; }
    public void setCondicionActual(CondicionAmbiental condicionActual) {
        this.condicionActual = condicionActual;
    }

    public int getPoblacionInicial() { return poblacionInicial; }
    public void setPoblacionInicial(int poblacionInicial) {
        this.poblacionInicial = poblacionInicial;
    }

    public double getActividadInicial() { return actividadInicial; }
    public void setActividadInicial(double actividadInicial) {
        this.actividadInicial = actividadInicial;
    }

    public List<CicloSimulacion> getCiclos() { return ciclos; }
    public void setCiclos(List<CicloSimulacion> ciclos) { this.ciclos = ciclos; }
}