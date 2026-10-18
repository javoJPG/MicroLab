package Backend;

public class CicloSimulacion {
    private int numeroCiclo;
    private CondicionAmbiental condicionAplicada;
    private int cantidadOrganismos;
    private double nivelActividad;
    private double crecimiento;
    private int reproduccion;
    private String estadoGeneral;
    private String descripcionRespuesta;

    public CicloSimulacion() {
    }

    public CicloSimulacion(int numeroCiclo, CondicionAmbiental condicionAplicada,
                           int cantidadOrganismos, double nivelActividad,
                           double crecimiento, int reproduccion,
                           String estadoGeneral, String descripcionRespuesta) {
        this.numeroCiclo = numeroCiclo;
        this.condicionAplicada = condicionAplicada.copiar();
        this.cantidadOrganismos = cantidadOrganismos;
        this.nivelActividad = nivelActividad;
        this.crecimiento = crecimiento;
        this.reproduccion = reproduccion;
        this.estadoGeneral = estadoGeneral;
        this.descripcionRespuesta = descripcionRespuesta;
    }

    public int getNumeroCiclo() { return numeroCiclo; }
    public void setNumeroCiclo(int numeroCiclo) { this.numeroCiclo = numeroCiclo; }

    public CondicionAmbiental getCondicionAplicada() { return condicionAplicada; }
    public void setCondicionAplicada(CondicionAmbiental condicionAplicada) {
        this.condicionAplicada = condicionAplicada;
    }

    public int getCantidadOrganismos() { return cantidadOrganismos; }
    public void setCantidadOrganismos(int cantidadOrganismos) {
        this.cantidadOrganismos = cantidadOrganismos;
    }

    public double getNivelActividad() { return nivelActividad; }
    public void setNivelActividad(double nivelActividad) { this.nivelActividad = nivelActividad; }

    public double getCrecimiento() { return crecimiento; }
    public void setCrecimiento(double crecimiento) { this.crecimiento = crecimiento; }

    public int getReproduccion() { return reproduccion; }
    public void setReproduccion(int reproduccion) { this.reproduccion = reproduccion; }

    public String getEstadoGeneral() { return estadoGeneral; }
    public void setEstadoGeneral(String estadoGeneral) { this.estadoGeneral = estadoGeneral; }

    public String getDescripcionRespuesta() { return descripcionRespuesta; }
    public void setDescripcionRespuesta(String descripcionRespuesta) {
        this.descripcionRespuesta = descripcionRespuesta;
    }
}