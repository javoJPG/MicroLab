package Backend;

/**
 * Interfaz que define el contrato de simulacion para cualquier microorganismo
 * dentro del laboratorio. Permite evaluar su reaccion ante condiciones
 * ambientales y ejecutar ciclos de simulacion.
 */
public interface ISimulable {

    /**
     * Evalua y actualiza el estado del microorganismo segun las condiciones
     * ambientales proporcionadas.
     *
     * @param condicion Condiciones ambientales aplicadas al microorganismo.
     * @return Descripcion textual de la reaccion del microorganismo.
     */
    String reaccionarACondiciones(CondicionAmbiental condicion);

    /**
     * Ejecuta un ciclo de simulacion actualizando la poblacion, actividad
     * y crecimiento del microorganismo.
     *
     * @param numeroCiclo Numero secuencial del ciclo ejecutado.
     * @param condicion   Condiciones ambientales durante el ciclo.
     * @return Objeto CicloSimulacion con los resultados registrados en el ciclo.
     */
    CicloSimulacion ejecutarCiclo(int numeroCiclo, CondicionAmbiental condicion);

}