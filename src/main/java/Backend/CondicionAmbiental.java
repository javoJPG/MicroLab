package Backend;

public class CondicionAmbiental {
    private double temperatura;              // Grados Celsius (°C)
    private double ph;                       // Escala 0.0 a 14.0
    private double oxigeno;                  // Porcentaje (0% a 100%)
    private double humedad;                  // Porcentaje (0% a 100%)
    private double salinidad;                // Porcentaje (0% a 100%)
    private double disponibilidadNutrientes; // Porcentaje (0% a 100%)

    public CondicionAmbiental() {
    }

    public CondicionAmbiental(double temperatura, double ph, double oxigeno,
                              double humedad, double salinidad, double disponibilidadNutrientes) {
        this.temperatura = temperatura;
        this.ph = ph;
        this.oxigeno = oxigeno;
        this.humedad = humedad;
        this.salinidad = salinidad;
        this.disponibilidadNutrientes = disponibilidadNutrientes;
    }

    public CondicionAmbiental copiar() {
        return new CondicionAmbiental(
                this.temperatura, this.ph, this.oxigeno,
                this.humedad, this.salinidad, this.disponibilidadNutrientes
        );
    }

    public double getTemperatura() { return temperatura; }
    public void setTemperatura(double temperatura) { this.temperatura = temperatura; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public double getOxigeno() { return oxigeno; }
    public void setOxigeno(double oxigeno) { this.oxigeno = oxigeno; }

    public double getHumedad() { return humedad; }
    public void setHumedad(double humedad) { this.humedad = humedad; }

    public double getSalinidad() { return salinidad; }
    public void setSalinidad(double salinidad) { this.salinidad = salinidad; }

    public double getDisponibilidadNutrientes() { return disponibilidadNutrientes; }
    public void setDisponibilidadNutrientes(double disponibilidadNutrientes) {
        this.disponibilidadNutrientes = disponibilidadNutrientes;
    }

    @Override
    public String toString() {
        return String.format("Temp: %.1f°C | pH: %.1f | O2: %.1f%% | Hum: %.1f%% | Sal: %.1f%% | Nutr: %.1f%%",
                temperatura, ph, oxigeno, humedad, salinidad, disponibilidadNutrientes);
    }
}