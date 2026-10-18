package Backend;

public class Protozoo extends Microorganismo {
    private double sensibilidadOxigenoHumedad;

    public Protozoo() {
        super();
        this.tipo = TIPO_PROTOZOO;
    }

    public Protozoo(String id, String idMuestra, String nombre, String estado,
                    CondicionAmbiental condicionCrecimiento, String capacidadReproductiva,
                    String alimentacion, String locomocion, int cantidadInicial,
                    double sensibilidadOxigenoHumedad) {
        super(id, idMuestra, nombre, TIPO_PROTOZOO, estado, condicionCrecimiento,
                capacidadReproductiva, alimentacion, locomocion, cantidadInicial);
        this.sensibilidadOxigenoHumedad = sensibilidadOxigenoHumedad;
    }

    @Override
    public String reaccionarACondiciones(CondicionAmbiental c) {
        if (cantidadOrganismos == 0) {
            this.respuestaCondiciones = "Poblacion de protozoos extinguida.";
            return this.respuestaCondiciones;
        }

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

        limitarValoresSimulacion();
        return new CicloSimulacion(numeroCiclo, c, this.cantidadOrganismos,
                this.nivelActividad, this.crecimiento,
                reproduccionCiclo, this.estado, descripcion);
    }

    public double getSensibilidadOxigenoHumedad() { return sensibilidadOxigenoHumedad; }
    public void setSensibilidadOxigenoHumedad(double sensibilidadOxigenoHumedad) {
        this.sensibilidadOxigenoHumedad = sensibilidadOxigenoHumedad;
    }
}