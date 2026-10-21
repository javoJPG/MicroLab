package Backend;

/**
 * Subclase que representa un Protozoo en el laboratorio.
 * Requiere ambientes humedos y bien oxigenados para mantener su movilidad
 * y reproducirse adecuadamente.
 */
public class Protozoo extends Microorganismo {

    // Factor que indica que tan sensible es el protozoo a la falta de oxigeno y humedad
    private double sensibilidadOxigenoHumedad;

    /**
     * Constructor vacio requerido para la persistencia en JSON.
     */
    public Protozoo() {
        super();
        this.tipo = TIPO_PROTOZOO;
    }

    /**
     * Constructor completo para inicializar un Protozoo.
     */
    public Protozoo(
            String id,
            String idMuestra,
            String nombre,
            String estado,
            CondicionAmbiental condicionCrecimiento,
            String capacidadReproductiva,
            String alimentacion,
            String locomocion,
            int cantidadInicial,
            double sensibilidadOxigenoHumedad
    ) {
        super(
                id, idMuestra, nombre, TIPO_PROTOZOO, estado, condicionCrecimiento,
                capacidadReproductiva, alimentacion, locomocion, cantidadInicial
        );
        this.sensibilidadOxigenoHumedad = sensibilidadOxigenoHumedad;
    }

    /**
     * Evalua la reaccion del protozoo frente a la humedad, oxigeno, salinidad y temperatura.
     */
    @Override
    public String reaccionarACondiciones(CondicionAmbiental c) {
        if (cantidadOrganismos <= 0) {
            this.cantidadOrganismos = 0;
            this.estado = ESTADO_INACTIVO;
            this.respuestaCondiciones = "Poblacion de protozoos extinguida.";
            return this.respuestaCondiciones;
        }

        if (c == null) {
            this.respuestaCondiciones = "No se proporcionaron condiciones ambientales.";
            return this.respuestaCondiciones;
        }

        // Evaluacion de condiciones criticas, optimas y suboptimas
        if (c.getHumedad() < 35.0 || c.getOxigeno() < 25.0 || c.getSalinidad() > 45.0) {
            this.estado = ESTADO_DETENIDO;
            this.respuestaCondiciones = "El protozoo detiene su desarrollo por baja humedad/oxigeno o alta salinidad.";

        } else if (c.getHumedad() >= 65.0 && c.getOxigeno() >= 50.0
                && c.getTemperatura() >= 18.0 && c.getTemperatura() <= 32.0) {
            this.estado = ESTADO_OPTIMO;
            this.respuestaCondiciones = "Ambiente acuoso oxigenado ideal: el protozoo incrementa su movilidad y reproduccion.";

        } else {
            this.estado = ESTADO_REDUCIDO;
            this.respuestaCondiciones = "El protozoo reduce su actividad locomotora por condiciones suboptimas.";
        }

        return this.respuestaCondiciones;
    }

    /**
     * Ejecuta un ciclo de simulacion afectando la poblacion y actividad del protozoo
     * en funcion de su sensibilidad al oxigeno y humedad.
     */
    @Override
    public CicloSimulacion ejecutarCiclo(int numeroCiclo, CondicionAmbiental c) {
        String descripcion = reaccionarACondiciones(c);
        int reproduccionCiclo = 0;

        if (ESTADO_OPTIMO.equals(this.estado)) {
            this.nivelActividad += 10.0;
            this.crecimiento = 18.0;
            reproduccionCiclo = (int) Math.round(this.cantidadOrganismos * 0.30);
            this.cantidadOrganismos += reproduccionCiclo;

        } else if (ESTADO_REDUCIDO.equals(this.estado)) {
            this.nivelActividad -= 10.0 * this.sensibilidadOxigenoHumedad;
            this.crecimiento = -4.0;
            reproduccionCiclo = (int) Math.round(this.cantidadOrganismos * 0.05);
            this.cantidadOrganismos += reproduccionCiclo;

        } else if (ESTADO_DETENIDO.equals(this.estado)) {
            this.nivelActividad -= 30.0 * this.sensibilidadOxigenoHumedad;
            this.crecimiento = -25.0;
            reproduccionCiclo = 0;
            this.cantidadOrganismos -= (int) Math.round(this.cantidadOrganismos * 0.35);
        }

        // Ajustar los valores finales para no exceder limites biologicos
        limitarValoresSimulacion();

        return new CicloSimulacion(
                numeroCiclo,
                c,
                this.cantidadOrganismos,
                this.nivelActividad,
                this.crecimiento,
                reproduccionCiclo,
                this.estado,
                descripcion
        );
    }

    // ==================== GETTERS Y SETTERS ====================

    public double getSensibilidadOxigenoHumedad() {
        return sensibilidadOxigenoHumedad;
    }

    public void setSensibilidadOxigenoHumedad(double sensibilidadOxigenoHumedad) {
        this.sensibilidadOxigenoHumedad = sensibilidadOxigenoHumedad;
    }

}