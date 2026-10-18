package Backend;

public class Bacteria extends Microorganismo {
    private double tasaFisionBinaria;

    public Bacteria() {
        super();
        this.tipo = TIPO_BACTERIA;
    }
    public Bacteria(String id, String idMuestra, String nombre, String estado,
                    CondicionAmbiental condicionCrecimiento, String capacidadReproductiva,
                    String alimentacion, String locomocion, int cantidadInicial,
                    double tasaFisionBinaria){

        super(id, idMuestra, nombre, TIPO_BACTERIA, estado, condicionCrecimiento,
                capacidadReproductiva, alimentacion, locomocion, cantidadInicial);
        this.tasaFisionBinaria = tasaFisionBinaria;
    }

    @Override
    public String reaccionarACondiciones(CondicionAmbiental c) {
        if (cantidadOrganismos == 0) {
            this.respuestaCondiciones = "Poblacion bacteriana sin sobrevivientes.";
            return this.respuestaCondiciones;
        }

        boolean tempFavorable = c.getTemperatura() >= 25.0 && c.getTemperatura() <= 42.0;
        boolean phFavorable = c.getPh() >= 6.0 && c.getPh() <= 8.0;
        boolean nutrientesSuficientes = c.getDisponibilidadNutrientes() >= 40.0;

        if (c.getTemperatura() > 65.0 || c.getPh() < 3.0 || c.getPh() > 10.5) {
            this.estado = ESTADO_DETENIDO;
            this.respuestaCondiciones = "Condiciones letales de temperatura o pH para la bacteria.";

        } else if (tempFavorable && phFavorable && nutrientesSuficientes) {
            this.estado = ESTADO_OPTIMO;
            this.respuestaCondiciones = "La bacteria prospera y acelera su fision binaria.";

        } else if (!nutrientesSuficientes) {
            this.estado = ESTADO_REDUCIDO;
            this.respuestaCondiciones = "Escasez de nutrientes reduce la division bacteriana.";

        } else {
            this.estado = ESTADO_ACTIVO;
            this.respuestaCondiciones = "La bacteria mantiene actividad moderada.";
        }
        return this.respuestaCondiciones;
    }

    @Override
    public CicloSimulacion ejecutarCiclo(int numeroCiclo, CondicionAmbiental c) {
        String descripcion = reaccionarACondiciones(c);
        int reproduccionCiclo = 0;

        if (ESTADO_OPTIMO.equals(this.estado)) {
            this.nivelActividad += 12.0;
            this.crecimiento = 25.0 * this.tasaFisionBinaria;
            reproduccionCiclo = (int) Math.round(this.cantidadOrganismos * 0.45 * this.tasaFisionBinaria);
            this.cantidadOrganismos += reproduccionCiclo;

        } else if (ESTADO_ACTIVO.equals(this.estado)) {
            this.nivelActividad += 2.0;
            this.crecimiento = 10.0;
            reproduccionCiclo = (int) Math.round(this.cantidadOrganismos * 0.15);
            this.cantidadOrganismos += reproduccionCiclo;
            
        } else if (ESTADO_REDUCIDO.equals(this.estado)) {
            this.nivelActividad -= 15.0;
            this.crecimiento = -5.0;
            reproduccionCiclo = 0;
            this.cantidadOrganismos -= (int) Math.round(this.cantidadOrganismos * 0.10);

        } else if (ESTADO_DETENIDO.equals(this.estado)) {
            this.nivelActividad -= 35.0;
            this.crecimiento = -30.0;
            reproduccionCiclo = 0;
            this.cantidadOrganismos -= (int) Math.round(this.cantidadOrganismos * 0.40);
        }

        limitarValoresSimulacion();
        return new CicloSimulacion(numeroCiclo, c, this.cantidadOrganismos,
                this.nivelActividad, this.crecimiento,
                reproduccionCiclo, this.estado, descripcion);
    }

    public double getTasaFisionBinaria() { return tasaFisionBinaria; }
    public void setTasaFisionBinaria(double tasaFisionBinaria) {
        this.tasaFisionBinaria = tasaFisionBinaria;
    }
}