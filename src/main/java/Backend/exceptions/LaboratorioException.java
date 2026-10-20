package Backend.exceptions;

/**
 * Excepcion que representa situaciones especiales o condiciones incompatibles
 * dentro de las reglas de negocio del laboratorio microbiologico.
 */
public class LaboratorioException extends Exception {

    /**
     * Constructor que recibe el mensaje descriptivo de la situacion del laboratorio.
     *
     * @param mensaje Explicacion del problema o incompatibilidad detectada.
     */
    public LaboratorioException(String mensaje) {
        super(mensaje);
    }

}