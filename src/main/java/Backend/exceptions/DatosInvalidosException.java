package Backend.exceptions;

/**
 * Excepcion que se lanza cuando los datos ingresados por el usuario
 * son nulos, vacios, duplicados o tienen un formato invalido.
 */
public class DatosInvalidosException extends Exception {

    /**
     * Constructor que recibe el detalle del error de validacion.
     *
     * @param mensaje Descripcion del error en los datos ingresados.
     */
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

}