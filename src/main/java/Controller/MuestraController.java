package Controller;

import Backend.CondicionAmbiental;
import Backend.Experimento;
import Backend.Microorganismo;
import Backend.Muestra;
import Backend.Observacion;
import Backend.exceptions.DatosInvalidosException;
import Backend.exceptions.LaboratorioException;
import Backend.persistence.GestorPersistenciaJson;
import Backend.persistence.GestorPersistenciaJson.ContenedorDatos;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal encargado de la gestion de muestras, microorganismos,
 * validaciones de negocio y coordinacion con la capa de persistencia en JSON.
 */
public class MuestraController {

    // Colecciones en memoria con el estado general del laboratorio
    private List<Muestra> muestras;
    private List<Observacion> observaciones;
    private List<Experimento> experimentos;
    private List<String> historialGeneral;

    // Gestor encargado de guardar y cargar los datos en archivo JSON
    private final GestorPersistenciaJson gestorPersistencia;

    /**
     * Constructor que inicializa el gestor de persistencia y carga los datos previos.
     */
    public MuestraController() {
        this.gestorPersistencia = new GestorPersistenciaJson();
        cargarDatos();
    }

    // ==================== PERSISTENCIA E HISTORIAL ====================

    /**
     * Carga todas las listas de datos desde el archivo JSON.
     */
    public final void cargarDatos() {
        ContenedorDatos datos = gestorPersistencia.cargarDatos();
        this.muestras = datos.getMuestras();
        this.observaciones = datos.getObservaciones();
        this.experimentos = datos.getExperimentos();
        this.historialGeneral = datos.getHistorialGeneral();
    }

    /**
     * Guarda el estado actual de todas las listas en el archivo JSON.
     */
    public void guardarDatos() {
        gestorPersistencia.guardarDatos(muestras, observaciones, experimentos, historialGeneral);
    }

    /**
     * Registra una accion global en la bitacora general del laboratorio.
     *
     * @param accion Descripcion del evento ocurrido.
     */
    public void registrarEnHistorialGeneral(String accion) {
        if (this.historialGeneral == null) {
            this.historialGeneral = new ArrayList<>();
        }
        this.historialGeneral.add(accion);
    }

    // ==================== METODOS DE VALIDACION ====================

    /**
     * Verifica que un campo de texto no sea nulo ni este en blanco.
     *
     * @param valor Texto a validar.
     * @param campo Nombre descriptivo del campo para el mensaje de error.
     * @throws DatosInvalidosException Si el texto es nulo o vacio.
     */
    public void validarTextoObligatorio(String valor, String campo) throws DatosInvalidosException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException("El campo '" + campo + "' es obligatorio.");
        }
    }

    /**
     * Verifica que los parametros de una condicion ambiental se encuentren dentro
     * de rangos fisicos y quimicos validos y coherentes entre si.
     *
     * @param c Condicion ambiental a evaluar.
     * @throws LaboratorioException    Si existe un rango fuera de limite o condicion incompatible.
     * @throws DatosInvalidosException Si el objeto de condiciones es nulo.
     */
    public void validarCondicionAmbiental(CondicionAmbiental c)
            throws LaboratorioException, DatosInvalidosException {

        if (c == null) {
            throw new DatosInvalidosException("Las condiciones ambientales no pueden ser nulas.");
        }

        if (c.getTemperatura() < -50.0 || c.getTemperatura() > 150.0) {
            throw new LaboratorioException(
                    "Situacion especial: Temperatura fuera de rango (-50°C a 150°C)."
            );
        }

        if (c.getPh() < 0.0 || c.getPh() > 14.0) {
            throw new LaboratorioException(
                    "Situacion especial: El pH debe estar entre 0.0 y 14.0."
            );
        }

        if (c.getOxigeno() < 0.0 || c.getOxigeno() > 100.0
                || c.getHumedad() < 0.0 || c.getHumedad() > 100.0
                || c.getSalinidad() < 0.0 || c.getSalinidad() > 100.0
                || c.getDisponibilidadNutrientes() < 0.0 || c.getDisponibilidadNutrientes() > 100.0) {
            throw new LaboratorioException(
                    "Situacion especial: Oxigeno, humedad, salinidad y nutrientes deben estar entre 0% y 100%."
            );
        }

        if (c.getTemperatura() > 100.0 && c.getHumedad() > 90.0 && c.getSalinidad() == 0.0) {
            throw new LaboratorioException(
                    "Situacion incompatible: No se puede mantener humedad > 90% a mas de 100°C sin presurizacion."
            );
        }
    }

    // ==================== GESTION DE MUESTRAS Y MICROORGANISMOS ====================

    /**
     * Valida y registra una nueva muestra en el sistema.
     *
     * @param nuevaMuestra Objeto Muestra a registrar.
     * @throws DatosInvalidosException Si faltan datos obligatorios o el ID ya existe.
     * @throws LaboratorioException    Si las condiciones ambientales son invalidas.
     */
    public void registrarMuestra(Muestra nuevaMuestra)
            throws DatosInvalidosException, LaboratorioException {

        if (nuevaMuestra == null) {
            throw new DatosInvalidosException("La muestra no es valida.");
        }

        // Validacion de campos obligatorios y condiciones iniciales
        validarTextoObligatorio(nuevaMuestra.getId(), "ID de muestra");
        validarTextoObligatorio(nuevaMuestra.getOrigen(), "Origen");
        validarTextoObligatorio(nuevaMuestra.getFechaRecoleccion(), "Fecha de recoleccion");
        validarTextoObligatorio(nuevaMuestra.getResponsableAnalisis(), "Responsable");
        validarCondicionAmbiental(nuevaMuestra.getCondicionInicial());

        // Verificar que no exista otra muestra con el mismo ID
        if (buscarMuestraPorId(nuevaMuestra.getId()) != null) {
            throw new DatosInvalidosException("Ya existe una muestra con el ID: " + nuevaMuestra.getId());
        }

        muestras.add(nuevaMuestra);
        registrarEnHistorialGeneral(
                "[MUESTRA] Registrada ID: " + nuevaMuestra.getId() + " (" + nuevaMuestra.getOrigen() + ")"
        );
        guardarDatos();
    }

    /**
     * Actualiza la informacion de una muestra existente identificada por su ID.
     */
    public void actualizarMuestra(
            String idMuestra,
            String nuevoOrigen,
            String nuevaFecha,
            String nuevoResponsable,
            CondicionAmbiental nuevaCondicion,
            String nuevasObservaciones
    ) throws DatosInvalidosException, LaboratorioException {

        Muestra muestra = buscarMuestraPorId(idMuestra);
        if (muestra == null) {
            throw new LaboratorioException("No existe la muestra con ID: " + idMuestra);
        }

        // Validar los nuevos datos antes de aplicar los cambios
        validarTextoObligatorio(nuevoOrigen, "Origen");
        validarTextoObligatorio(nuevaFecha, "Fecha");
        validarTextoObligatorio(nuevoResponsable, "Responsable");
        validarCondicionAmbiental(nuevaCondicion);

        // Actualizar atributos de la muestra
        muestra.setOrigen(nuevoOrigen);
        muestra.setFechaRecoleccion(nuevaFecha);
        muestra.setResponsableAnalisis(nuevoResponsable);
        muestra.setCondicionInicial(nuevaCondicion);
        muestra.setObservacionesGenerales(nuevasObservaciones);
        muestra.registrarEventoHistorial("Muestra actualizada el " + nuevaFecha);

        registrarEnHistorialGeneral("[ACTUALIZACION] Muestra ID: " + idMuestra);
        guardarDatos();
    }

    /**
     * Valida y asocia un nuevo microorganismo a una muestra previamente registrada.
     *
     * @param idMuestra ID de la muestra donde se encontro el microorganismo.
     * @param micro     Instancia del microorganismo a registrar.
     * @throws DatosInvalidosException Si hay datos vacios, cantidad invalida o ID duplicado.
     * @throws LaboratorioException    Si la muestra no existe o las condiciones son invalidas.
     */
    public void registrarMicroorganismoEnMuestra(String idMuestra, Microorganismo micro)
            throws DatosInvalidosException, LaboratorioException {

        Muestra muestra = buscarMuestraPorId(idMuestra);
        if (muestra == null) {
            throw new LaboratorioException(
                    "Debe seleccionar una muestra registrada para asociar el microorganismo."
            );
        }

        if (micro == null) {
            throw new DatosInvalidosException("El microorganismo no es valido.");
        }

        // Validacion de campos obligatorios del microorganismo
        validarTextoObligatorio(micro.getId(), "ID del microorganismo");
        validarTextoObligatorio(micro.getNombre(), "Nombre");
        validarTextoObligatorio(micro.getTipo(), "Tipo");
        validarTextoObligatorio(micro.getCapacidadReproductiva(), "Capacidad reproductiva");
        validarTextoObligatorio(micro.getAlimentacion(), "Alimentacion");
        validarTextoObligatorio(micro.getLocomocion(), "Locomocion");

        if (micro.getCantidadOrganismos() <= 0) {
            throw new DatosInvalidosException("La cantidad inicial de organismos debe ser mayor a cero.");
        }

        validarCondicionAmbiental(micro.getCondicionCrecimiento());

        // Verificar unicidad del ID de microorganismo en todo el laboratorio
        if (buscarMicroorganismoGlobalPorId(micro.getId()) != null) {
            throw new DatosInvalidosException("Ya existe un microorganismo con el ID: " + micro.getId());
        }

        micro.setIdMuestra(muestra.getId());
        muestra.agregarMicroorganismo(micro);

        registrarEnHistorialGeneral(
                "[MICROORGANISMO] " + micro.getNombre() + " (" + micro.getTipo() + ") en Muestra " + muestra.getId()
        );
        guardarDatos();
    }

    // ==================== METODOS DE BUSQUEDA Y ACCESO ====================

    /**
     * Busca una muestra por su identificador unico.
     *
     * @param idMuestra ID de la muestra a buscar.
     * @return La Muestra encontrada, o null si no existe.
     */
    public Muestra buscarMuestraPorId(String idMuestra) {
        if (idMuestra == null || muestras == null) {
            return null;
        }

        for (Muestra m : muestras) {
            if (m != null && m.getId() != null && m.getId().equalsIgnoreCase(idMuestra.trim())) {
                return m;
            }
        }

        return null;
    }

    /**
     * Busca un microorganismo por su ID recorriendo todas las muestras registradas.
     *
     * @param idMicro ID del microorganismo a buscar.
     * @return El Microorganismo encontrado, o null si no existe.
     */
    public Microorganismo buscarMicroorganismoGlobalPorId(String idMicro) {
        if (idMicro == null || muestras == null) {
            return null;
        }

        for (Muestra m : muestras) {
            Microorganismo micro = m.buscarMicroorganismoPorId(idMicro.trim());
            if (micro != null) {
                return micro;
            }
        }

        return null;
    }

    /**
     * Obtiene una lista consolidada con todos los microorganismos de todas las muestras.
     */
    public List<Microorganismo> obtenerTodosLosMicroorganismos() {
        List<Microorganismo> todos = new ArrayList<>();
        if (muestras != null) {
            for (Muestra m : muestras) {
                todos.addAll(m.getMicroorganismos());
            }
        }
        return todos;
    }

    public List<Muestra> getMuestras() {
        return muestras;
    }

    public List<Observacion> getObservaciones() {
        return observaciones;
    }

    public List<Experimento> getExperimentos() {
        return experimentos;
    }

    public List<String> getHistorialGeneral() {
        return historialGeneral;
    }

}