package Backend;

public class Extremofilo extends Microorganismo {
    private double indiceResistenciaExtrema;

    public Extremofilo() {
        super();
        this.tipo = TIPO_EXTREMOFILO;
    }

    public Extremofilo(String id, String idMuestra, String nombre, String estado,
                       CondicionAmbiental condicionCrecimiento, String capacidadReproductiva,
                       String alimentacion, String locomocion, int cantidadInicial,
                       double indiceResistenciaExtrema) {
        super(id, idMuestra, nombre, TIPO_EXTREMOFILO, estado, condicionCrecimiento,
                capacidadReproductiva, alimentacion, locomocion, cantidadInicial);
        this.indiceResistenciaExtrema = indiceResistenciaExtrema;
    }

    @Override
    public String reaccionarACondiciones(CondicionAmbiental c) {
        if (cantidadOrganismos == 0) {
            this.respuestaCondiciones = "Poblacion extremofila sin individuos activos.";
            return this.respuestaCondiciones;
        }

        boolean condicionExtrema = c.getTemperatura() > 50.0 || c.getTemperatura() < 5.0
                || c.getPh() < 3.5 || c.getPh() > 9.5
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

        limitarValoresSimulacion();
        return new CicloSimulacion(numeroCiclo, c, this.cantidadOrganismos,
                this.nivelActividad, this.crecimiento,
                reproduccionCiclo, this.estado, descripcion);
    }

    public double getIndiceResistenciaExtrema() { return indiceResistenciaExtrema; }
    public void setIndiceResistenciaExtrema(double indiceResistenciaExtrema) {
        this.indiceResistenciaExtrema = indiceResistenciaExtrema;
    }
}