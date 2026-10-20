package Backend;

/**
 * Subclase que representa un microorganismo Extremofilo en el laboratorio.
 * A diferencia de otros organismos, prospera en condiciones ambientales extremas
 * de temperatura, pH o salinidad, y reduce su metabolismo en condiciones normales.
 */
public class Extremofilo extends Microorganismo {

    // Indice que potencia su crecimiento y reproduccion en ambientes extremos
    private double indiceResistenciaExtrema;

    /**
     * Constructor vacio requerido para la persistencia en JSON.
     */
    public Extremofilo() {
        super();
        this.tipo = TIPO_EXTREMOFILO;
    }

    /**
     * Constructor completo para inicializar un Extremofilo.
     */
    public Extremofilo(
            String id,
            String idMuestra,
            String nombre,
            String estado,
            CondicionAmbiental condicionCrecimiento,
            String capacidadReproductiva,
            String alimentacion,
            String locomocion,
            int cantidadInicial,
            double indiceResistenciaExtrema
    ) {
        super(
                id, idMuestra, nombre, TIPO_EXTREMOFILO, estado, condicionCrecimiento,
                capacidadReproductiva, alimentacion, locomocion, cantidadInicial
        );
        this.indiceResistenciaExtrema = indiceResistenciaExtrema;
    }

    /**
     * Evalua si el ambiente presenta condiciones extremas favorables para el extremofilo.
     */
    @Override
    public String reaccionarACondiciones(CondicionAmbiental c) {
        if (cantidadOrganismos <= 0) {
            this.cantidadOrganismos = 0;
            this.estado = ESTADO_INACTIVO;
            this.respuestaCondiciones = "Poblacion extremofila sin individuos activos.";
            return this.respuestaCondiciones;
        }

        if (c == null) {
            this.respuestaCondiciones = "No se proporcionaron condiciones ambientales.";
            return this.respuestaCondiciones;
        }

        // Determina si existe al menos un parametro en rango extremo
        boolean condicionExtrema = c.getTemperatura() > 50.0
                || c.getTemperatura() < 5.0
                || c.getPh() < 3.5
                || c.getPh() > 9.5
                || c.getSalinidad() > 50.0;

        if (condicionExtrema && c.getDisponibilidadNutrientes() >= 15.0) {
            this.estado = ESTADO_OPTIMO;
            this.respuestaCondiciones = "El extremofilo prospera en condiciones extremas desfavorables para otros organismos.";

        } else if (!condicionExtrema) {
            this.estado = ESTADO_REDUCIDO;
            this.respuestaCondiciones = "En condiciones ambientales normales el extremofilo reduce su metabolismo.";

        } else {
            this.estado = ESTADO_ACTIVO;
            this.respuestaCondiciones = "El extremofilo resiste el entorno hostil pese a los bajos nutrientes.";
        }

        return this.respuestaCondiciones;
    }

    /**
     * Ejecuta un ciclo de simulacion aplicando el indice de resistencia extrema
     * al crecimiento y reproduccion del organismo.
     */
    @Override
    public CicloSimulacion ejecutarCiclo(int numeroCiclo, CondicionAmbiental c) {
        String descripcion = reaccionarACondiciones(c);
        int reproduccionCiclo = 0;

        if (ESTADO_OPTIMO.equals(this.estado)) {
            this.nivelActividad += 15.0;
            this.crecimiento = 22.0 * this.indiceResistenciaExtrema;
            reproduccionCiclo = (int) Math.round(this.cantidadOrganismos * 0.35 * this.indiceResistenciaExtrema);
            this.cantidadOrganismos += reproduccionCiclo;

        } else if (ESTADO_ACTIVO.equals(this.estado)) {
            this.nivelActividad += 3.0;
            this.crecimiento = 5.0;
            reproduccionCiclo = (int) Math.round(this.cantidadOrganismos * 0.08);
            this.cantidadOrganismos += reproduccionCiclo;

        } else if (ESTADO_REDUCIDO.equals(this.estado)) {
            this.nivelActividad -= 10.0;
            this.crecimiento = 0.0;
            reproduccionCiclo = 0;
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

    public double getIndiceResistenciaExtrema() {
        return indiceResistenciaExtrema;
    }

    public void setIndiceResistenciaExtrema(double indiceResistenciaExtrema) {
        this.indiceResistenciaExtrema = indiceResistenciaExtrema;
    }

}