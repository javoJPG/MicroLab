package Backend;

public class HongoMicroscopico extends Microorganismo {
    private int factorEsporulacion;

    public HongoMicroscopico() {
        super();
        this.tipo = TIPO_HONGO;
    }

    public HongoMicroscopico(String id, String idMuestra, String nombre, String estado,
                             CondicionAmbiental condicionCrecimiento, String capacidadReproductiva,
                             String alimentacion, String locomocion, int cantidadInicial,
                             int factorEsporulacion) {
        super(id, idMuestra, nombre, TIPO_HONGO, estado, condicionCrecimiento,
                capacidadReproductiva, alimentacion, locomocion, cantidadInicial);
        this.factorEsporulacion = factorEsporulacion;
    }

    @Override
    public String reaccionarACondiciones(CondicionAmbiental c) {
        if (cantidadOrganismos == 0) {
            this.respuestaCondiciones = "Colonia micotica sin actividad vital.";
            return this.respuestaCondiciones;
        }

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

        limitarValoresSimulacion();
        return new CicloSimulacion(numeroCiclo, c, this.cantidadOrganismos,
                this.nivelActividad, this.crecimiento,
                reproduccionCiclo, this.estado, descripcion);
    }

    public int getFactorEsporulacion() { return factorEsporulacion; }
    public void setFactorEsporulacion(int factorEsporulacion) {
        this.factorEsporulacion = factorEsporulacion;
    }
}