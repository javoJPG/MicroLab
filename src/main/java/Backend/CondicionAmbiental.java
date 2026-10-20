package Backend;

/**
 * Clase que representa las condiciones ambientales de una muestra,
 * microorganismo, observacion o ciclo de simulacion.
 */
public class CondicionAmbiental {

    // Atributos de las condiciones fisicas y quimicas del ambiente
    private double temperatura;              // Grados Celsius (°C)
    private double ph;                       // Escala de acidez/alcalinidad (0.0 a 14.0)
    private double oxigeno;                  // Porcentaje de oxigeno (0% a 100%)
    private double humedad;                  // Porcentaje de humedad (0% a 100%)
    private double salinidad;                // Porcentaje de salinidad (0% a 100%)
    private double disponibilidadNutrientes; // Porcentaje de nutrientes (0% a 100%)

    /**
     * Constructor vacio requerido para la serializacion/deserializacion JSON.
     */
    public CondicionAmbiental() {
    }

    /**
     * Constructor completo para inicializar todas las variables ambientales.
     */
    public CondicionAmbiental(
            double temperatura,
            double ph,
            double oxigeno,
            double humedad,
            double salinidad,
            double disponibilidadNutrientes
    ) {
        this.temperatura = temperatura;
        this.ph = ph;
        this.oxigeno = oxigeno;
        this.humedad = humedad;
        this.salinidad = salinidad;
        this.disponibilidadNutrientes = disponibilidadNutrientes;
    }

    /**
     * Crea y devuelve una copia independiente de esta condicion ambiental
     * para evitar modificaciones no deseadas por referencia.
     *
     * @return Nueva instancia de CondicionAmbiental con los mismos valores.
     */
    public CondicionAmbiental copiar() {
        return new CondicionAmbiental(
                this.temperatura,
                this.ph,
                this.oxigeno,
                this.humedad,
                this.salinidad,
                this.disponibilidadNutrientes
        );
    }

    // ==================== GETTERS Y SETTERS ====================

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public double getPh() {
        return ph;
    }

    public void setPh(double ph) {
        this.ph = ph;
    }

    public double getOxigeno() {
        return oxigeno;
    }

    public void setOxigeno(double oxigeno) {
        this.oxigeno = oxigeno;
    }

    public double getHumedad() {
        return humedad;
    }

    public void setHumedad(double humedad) {
        this.humedad = humedad;
    }

    public double getSalinidad() {
        return salinidad;
    }

    public void setSalinidad(double salinidad) {
        this.salinidad = salinidad;
    }

    public double getDisponibilidadNutrientes() {
        return disponibilidadNutrientes;
    }

    public void setDisponibilidadNutrientes(double disponibilidadNutrientes) {
        this.disponibilidadNutrientes = disponibilidadNutrientes;
    }

    /**
     * Devuelve una representacion legible en texto de todas las variables ambientales.
     */
    @Override
    public String toString() {
        return String.format(
                "Temp: %.1f°C | pH: %.1f | O2: %.1f%% | Hum: %.1f%% | Sal: %.1f%% | Nutr: %.1f%%",
                temperatura, ph, oxigeno, humedad, salinidad, disponibilidadNutrientes
        );
    }

}