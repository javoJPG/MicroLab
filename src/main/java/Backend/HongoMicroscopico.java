package Backend;

/**
 * Subclase que representa un Hongo Microscopico en el laboratorio.
 * Se desarrolla de forma optima en ambientes humedos, con temperaturas moderadas
 * y un pH ligeramente acido, reproduciendose mediante esporulacion.
 */
public class HongoMicroscopico extends Microorganismo {

    // Factor numerico que representa la capacidad de produccion de esporas
    private int factorEsporulacion;

    /**
     * Constructor vacio requerido para la persistencia en JSON.
     */
    public HongoMicroscopico() {
        super();
        this.tipo = TIPO_HONGO;
    }

    /**
     * Constructor completo para inicializar un Hongo Microscopico.
     */
    public HongoMicroscopico(
            String id,
            String idMuestra,
            String nombre,
            String estado,
            CondicionAmbiental condicionCrecimiento,
            String capacidadReproductiva,
            String alimentacion,
            String locomocion,
            int cantidadInicial,
            int factorEsporulacion
    ) {
        super(
                id, idMuestra, nombre, TIPO_HONGO, estado, condicionCrecimiento,
                capacidadReproductiva, alimentacion, locomocion, cantidadInicial
        );
        this.factorEsporulacion = factorEsporulacion;
    }

    /**
     * Evalua la reaccion del hongo microscopico ante el pH, humedad y temperatura.
     */
    @Override
    public String reaccionarACondiciones(CondicionAmbiental c) {
        if (cantidadOrganismos <= 0) {
            this.cantidadOrganismos = 0;
            this.estado = ESTADO_INACTIVO;
            this.respuestaCondiciones = "Colonia micotica sin actividad vital.";
            return this.respuestaCondiciones;
        }

        if (c == null) {
            this.respuestaCondiciones = "No se proporcionaron condiciones ambientales.";
            return this.respuestaCondiciones;
        }

        // Condiciones ideales para el desarrollo micotico
        boolean phAcidoModerado = c.getPh() >= 4.0 && c.getPh() <= 6.8;
        boolean humedadAlta = c.getHumedad() >= 60.0;
        boolean tempModerada = c.getTemperatura() >= 15.0 && c.getTemperatura() <= 35.0;

        if (c.getTemperatura() > 55.0 || c.getHumedad() < 20.0) {
            this.estado = ESTADO_DETENIDO;
            this.respuestaCondiciones = "Sequedad extrema o calor excesivo detienen el desarrollo del hongo.";

        } else if (phAcidoModerado && humedadAlta && tempModerada && c.getDisponibilidadNutrientes() >= 35.0) {
            this.estado = ESTADO_OPTIMO;
            this.respuestaCondiciones = "Humedad y pH ligeramente acido favorecen la esporulacion del hongo.";

        } else {
            this.estado = ESTADO_REDUCIDO;
            this.respuestaCondiciones = "El hongo mantiene crecimiento micelial lento con esporulacion reducida.";
        }

        return this.respuestaCondiciones;
    }

    /**
     * Ejecuta un ciclo de simulacion calculando la reproduccion por esporas
     * y la variacion en la colonia del hongo.
     */
    @Override
    public CicloSimulacion ejecutarCiclo(int numeroCiclo, CondicionAmbiental c) {
        String descripcion = reaccionarACondiciones(c);
        int reproduccionCiclo = 0;

        if (ESTADO_OPTIMO.equals(this.estado)) {
            this.nivelActividad += 8.0;
            this.crecimiento = 20.0 + (this.factorEsporulacion * 2.0);
            reproduccionCiclo = (int) Math.round(this.cantidadOrganismos * 0.25) + (this.factorEsporulacion * 5);
            this.cantidadOrganismos += reproduccionCiclo;

        } else if (ESTADO_REDUCIDO.equals(this.estado)) {
            this.nivelActividad -= 8.0;
            this.crecimiento = 2.0;
            reproduccionCiclo = this.factorEsporulacion;
            this.cantidadOrganismos += reproduccionCiclo;

        } else if (ESTADO_DETENIDO.equals(this.estado)) {
            this.nivelActividad -= 20.0;
            this.crecimiento = -15.0;
            reproduccionCiclo = 0;
            this.cantidadOrganismos -= (int) Math.round(this.cantidadOrganismos * 0.20);
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

    public int getFactorEsporulacion() {
        return factorEsporulacion;
    }

    public void setFactorEsporulacion(int factorEsporulacion) {
        this.factorEsporulacion = factorEsporulacion;
    }

}