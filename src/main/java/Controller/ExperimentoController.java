package Controller;

import Backend.CicloSimulacion;
import Backend.CondicionAmbiental;
import Backend.Experimento;
import Backend.Microorganismo;
import Backend.Muestra;
import Backend.Observacion;
import Backend.exceptions.DatosInvalidosException;
import Backend.exceptions.LaboratorioException;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador encargado de gestionar el registro de observaciones cientificas
 * y la ejecucion de experimentos y ciclos de simulacion sobre los microorganismos.
 */
public class ExperimentoController {

    // Referencia al controlador de muestras para acceder a los datos y persistencia
    private final MuestraController muestraCtrl;

    /**
     * Constructor que recibe el controlador base de muestras.
     *
     * @param muestraCtrl Instancia de MuestraController.
     */
    public ExperimentoController(MuestraController muestraCtrl) {
        this.muestraCtrl = muestraCtrl;
    }

    // ==================== GESTION DE OBSERVACIONES ====================

    /**
     * Valida y registra una nueva observacion asociada a una muestra y un microorganismo.
     *
     * @param nuevaObs Objeto Observacion a registrar.
     * @throws DatosInvalidosException Si faltan datos obligatorios o el ID esta duplicado.
     * @throws LaboratorioException    Si la muestra o el microorganismo no existen.
     */
    public void registrarObservacion(Observacion nuevaObs)
            throws DatosInvalidosException, LaboratorioException {

        if (nuevaObs == null) {
            throw new DatosInvalidosException("La observacion no puede ser nula.");
        }

        // Validar campos obligatorios y condiciones de analisis
        muestraCtrl.validarTextoObligatorio(nuevaObs.getId(), "ID de observacion");
        muestraCtrl.validarTextoObligatorio(nuevaObs.getIdMuestra(), "Muestra asociada");
        muestraCtrl.validarTextoObligatorio(nuevaObs.getIdMicroorganismo(), "Microorganismo asociado");
        muestraCtrl.validarTextoObligatorio(nuevaObs.getFecha(), "Fecha");
        muestraCtrl.validarTextoObligatorio(nuevaObs.getDescripcionResultados(), "Descripcion de resultados");
        muestraCtrl.validarCondicionAmbiental(nuevaObs.getCondicionAnalisis());

        // Verificar que no exista otra observacion con el mismo ID
        for (Observacion o : muestraCtrl.getObservaciones()) {
            if (o.getId().equalsIgnoreCase(nuevaObs.getId().trim())) {
                throw new DatosInvalidosException("Ya existe una observacion con el ID: " + nuevaObs.getId());
            }
        }

        // Verificar existencia de la muestra y del microorganismo dentro de ella
        Muestra muestra = muestraCtrl.buscarMuestraPorId(nuevaObs.getIdMuestra());
        if (muestra == null) {
            throw new LaboratorioException("La muestra '" + nuevaObs.getIdMuestra() + "' no existe.");
        }

        Microorganismo micro = muestra.buscarMicroorganismoPorId(nuevaObs.getIdMicroorganismo());
        if (micro == null) {
            throw new LaboratorioException(
                    "Situacion especial: El microorganismo '" + nuevaObs.getIdMicroorganismo()
                    + "' no esta registrado en la muestra '" + muestra.getId() + "'."
            );
        }

        // Guardar la observacion y actualizar historiales
        muestraCtrl.getObservaciones().add(nuevaObs);
        muestra.registrarEventoHistorial(
                "Observacion " + nuevaObs.getId() + " en " + micro.getNombre() + ": " + nuevaObs.getDescripcionResultados()
        );
        muestraCtrl.registrarEnHistorialGeneral(
                "[OBSERVACION] ID: " + nuevaObs.getId() + " | Microorganismo: " + micro.getNombre()
        );
        muestraCtrl.guardarDatos();
    }

    /**
     * Filtra y devuelve las observaciones correspondientes a una muestra y/o microorganismo.
     */
    public List<Observacion> obtenerObservacionesPorMicroorganismo(String idMuestra, String idMicroorganismo) {
        List<Observacion> resultado = new ArrayList<>();

        for (Observacion obs : muestraCtrl.getObservaciones()) {
            boolean okMuestra = (idMuestra == null || idMuestra.isEmpty())
                    || obs.getIdMuestra().equalsIgnoreCase(idMuestra);

            boolean okMicro = (idMicroorganismo == null || idMicroorganismo.isEmpty())
                    || obs.getIdMicroorganismo().equalsIgnoreCase(idMicroorganismo);

            if (okMuestra && okMicro) {
                resultado.add(obs);
            }
        }

        return resultado;
    }

    // ==================== GESTION DE EXPERIMENTOS Y SIMULACION ====================

    /**
     * Inicia un nuevo experimento de simulacion sobre un microorganismo de una muestra.
     *
     * @param idExperimento    Identificador unico del experimento.
     * @param fecha            Fecha de inicio del experimento.
     * @param idMuestra        ID de la muestra seleccionada.
     * @param idMicroorganismo ID del microorganismo seleccionado.
     * @param condicionInicial Condiciones ambientales iniciales.
     * @return El objeto Experimento creado.
     * @throws DatosInvalidosException Si hay campos invalidos o ID repetido.
     * @throws LaboratorioException    Si la muestra/microorganismo no existen o no hay poblacion viva.
     */
    public Experimento iniciarExperimento(
            String idExperimento,
            String fecha,
            String idMuestra,
            String idMicroorganismo,
            CondicionAmbiental condicionInicial
    ) throws DatosInvalidosException, LaboratorioException {

        // Validar seleccion de muestra
        if (idMuestra == null || idMuestra.trim().isEmpty()) {
            throw new LaboratorioException(
                    "Situacion especial: No puede iniciar un experimento sin seleccionar una muestra."
            );
        }

        Muestra muestra = muestraCtrl.buscarMuestraPorId(idMuestra);
        if (muestra == null) {
            throw new LaboratorioException(
                    "Situacion especial: La muestra '" + idMuestra + "' no esta registrada."
            );
        }

        // Validar seleccion y estado vital del microorganismo
        if (idMicroorganismo == null || idMicroorganismo.trim().isEmpty()) {
            throw new LaboratorioException(
                    "Situacion especial: Debe seleccionar un microorganismo para el experimento."
            );
        }

        Microorganismo micro = muestra.buscarMicroorganismoPorId(idMicroorganismo);
        if (micro == null) {
            throw new LaboratorioException(
                    "Situacion especial: El microorganismo '" + idMicroorganismo + "' no pertenece a la muestra."
            );
        }

        if (micro.getCantidadOrganismos() <= 0) {
            throw new LaboratorioException(
                    "Situacion incompatible: El microorganismo '" + micro.getNombre() + "' no tiene poblacion viva."
            );
        }

        // Validar datos del experimento
        muestraCtrl.validarTextoObligatorio(idExperimento, "ID del Experimento");
        muestraCtrl.validarTextoObligatorio(fecha, "Fecha");
        muestraCtrl.validarCondicionAmbiental(condicionInicial);

        if (buscarExperimentoPorId(idExperimento) != null) {
            throw new DatosInvalidosException("Ya existe un experimento con el ID: " + idExperimento);
        }

        // Crear y registrar el experimento
        Experimento nuevoExp = new Experimento(
                idExperimento,
                fecha,
                muestra.getId(),
                micro,
                condicionInicial
        );

        muestraCtrl.getExperimentos().add(nuevoExp);
        muestra.registrarEventoHistorial("Experimento " + idExperimento + " iniciado sobre " + micro.getNombre());
        muestraCtrl.registrarEnHistorialGeneral(
                "[EXPERIMENTO] Iniciado ID: " + idExperimento + " sobre " + micro.getNombre()
        );
        muestraCtrl.guardarDatos();

        return nuevoExp;
    }

    /**
     * Ejecuta un nuevo ciclo de simulacion sobre un experimento activo aplicando
     * nuevas condiciones ambientales.
     *
     * @param idExperimento  ID del experimento en curso.
     * @param nuevaCondicion Condiciones ambientales para el ciclo.
     * @return El CicloSimulacion con los resultados obtenidos.
     * @throws DatosInvalidosException Si las condiciones son nulas.
     * @throws LaboratorioException    Si el experimento no existe o las condiciones son incompatibles.
     */
    public CicloSimulacion ejecutarCicloExperimento(
            String idExperimento,
            CondicionAmbiental nuevaCondicion
    ) throws DatosInvalidosException, LaboratorioException {

        Experimento exp = buscarExperimentoPorId(idExperimento);
        if (exp == null) {
            throw new LaboratorioException(
                    "Situacion especial: Debe seleccionar un experimento activo para correr el ciclo."
            );
        }

        muestraCtrl.validarCondicionAmbiental(nuevaCondicion);

        Muestra muestra = muestraCtrl.buscarMuestraPorId(exp.getIdMuestra());
        Microorganismo micro = (muestra != null)
                ? muestra.buscarMicroorganismoPorId(exp.getIdMicroorganismo())
                : null;

        if (micro == null) {
            throw new LaboratorioException(
                    "Situacion especial: El microorganismo del experimento no fue encontrado."
            );
        }

        // Regla de incompatibilidad: 0% oxigeno y 0% nutrientes para organismos no extremofilos
        if (nuevaCondicion.getDisponibilidadNutrientes() == 0.0
                && nuevaCondicion.getOxigeno() == 0.0
                && !Microorganismo.TIPO_EXTREMOFILO.equalsIgnoreCase(micro.getTipo())) {
            throw new LaboratorioException(
                    "Condicion incompatible con el estudio: 0% de oxigeno y 0% de nutrientes simultaneamente en organismo no extremofilo."
            );
        }

        // Ejecutar ciclo y persistir resultados
        CicloSimulacion ciclo = exp.ejecutarNuevoCiclo(nuevaCondicion, micro);

        muestra.registrarEventoHistorial(
                "Exp " + exp.getId() + " - Ciclo " + ciclo.getNumeroCiclo() + ": " + ciclo.getEstadoGeneral()
        );
        muestraCtrl.registrarEnHistorialGeneral(
                "[SIMULACION] Exp " + exp.getId() + " Ciclo " + ciclo.getNumeroCiclo() + " -> Pob: " + ciclo.getCantidadOrganismos()
        );
        muestraCtrl.guardarDatos();

        return ciclo;
    }

    /**
     * Busca un experimento registrado por su identificador unico.
     *
     * @param idExperimento ID del experimento.
     * @return El Experimento encontrado, o null si no existe.
     */
    public Experimento buscarExperimentoPorId(String idExperimento) {
        if (idExperimento == null) {
            return null;
        }

        for (Experimento e : muestraCtrl.getExperimentos()) {
            if (e != null && e.getId() != null && e.getId().equalsIgnoreCase(idExperimento.trim())) {
                return e;
            }
        }

        return null;
    }

}