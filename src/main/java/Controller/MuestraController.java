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

public class MuestraController {

    private List<Muestra> muestras;
    private List<Observacion> observaciones;
    private List<Experimento> experimentos;
    private List<String> historialGeneral;
    private final GestorPersistenciaJson gestorPersistencia;

    public MuestraController() {
        this.gestorPersistencia = new GestorPersistenciaJson();
        cargarDatos();
    }

    public final void cargarDatos() {
        ContenedorDatos datos = gestorPersistencia.cargarDatos();
        this.muestras = datos.getMuestras();
        this.observaciones = datos.getObservaciones();
        this.experimentos = datos.getExperimentos();
        this.historialGeneral = datos.getHistorialGeneral();
    }

    public void guardarDatos() {
        gestorPersistencia.guardarDatos(muestras, observaciones, experimentos, historialGeneral);
    }

    public void registrarEnHistorialGeneral(String accion) {
        this.historialGeneral.add(accion);
    }

    public void validarTextoObligatorio(String valor, String campo) throws DatosInvalidosException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException("El campo '" + campo + "' es obligatorio.");
        }
    }

    public void validarCondicionAmbiental(CondicionAmbiental c) throws LaboratorioException, DatosInvalidosException {
        if (c == null) {
            throw new DatosInvalidosException("Las condiciones ambientales no pueden ser nulas.");
        }
        if (c.getTemperatura() < -50.0 || c.getTemperatura() > 150.0) {
            throw new LaboratorioException("Situacion especial: Temperatura fuera de rango (-50°C a 150°C).");
        }
        if (c.getPh() < 0.0 || c.getPh() > 14.0) {
            throw new LaboratorioException("Situacion especial: El pH debe estar entre 0.0 y 14.0.");
        }
        if (c.getOxigeno() < 0.0 || c.getOxigeno() > 100.0 || c.getHumedad() < 0.0 || c.getHumedad() > 100.0
                || c.getSalinidad() < 0.0 || c.getSalinidad() > 100.0
                || c.getDisponibilidadNutrientes() < 0.0 || c.getDisponibilidadNutrientes() > 100.0) {
            throw new LaboratorioException("Situacion especial: Oxigeno, humedad, salinidad y nutrientes deben estar entre 0% y 100%.");
        }
        if (c.getTemperatura() > 100.0 && c.getHumedad() > 90.0 && c.getSalinidad() == 0.0) {
            throw new LaboratorioException("Situacion incompatible: No se puede mantener humedad > 90% a mas de 100°C sin presurizacion.");
        }
    }

    public void registrarMuestra(Muestra nuevaMuestra) throws DatosInvalidosException, LaboratorioException {
        if (nuevaMuestra == null) {
            throw new DatosInvalidosException("La muestra no es valida.");
        }
        validarTextoObligatorio(nuevaMuestra.getId(), "ID de muestra");
        validarTextoObligatorio(nuevaMuestra.getOrigen(), "Origen");
        validarTextoObligatorio(nuevaMuestra.getFechaRecoleccion(), "Fecha de recoleccion");
        validarTextoObligatorio(nuevaMuestra.getResponsableAnalisis(), "Responsable");
        validarCondicionAmbiental(nuevaMuestra.getCondicionInicial());

        if (buscarMuestraPorId(nuevaMuestra.getId()) != null) {
            throw new DatosInvalidosException("Ya existe una muestra con el ID: " + nuevaMuestra.getId());
        }

        muestras.add(nuevaMuestra);
        registrarEnHistorialGeneral("[MUESTRA] Registrada ID: " + nuevaMuestra.getId() + " (" + nuevaMuestra.getOrigen() + ")");
        guardarDatos();
    }

    public void actualizarMuestra(String idMuestra, String nuevoOrigen, String nuevaFecha,
                                  String nuevoResponsable, CondicionAmbiental nuevaCondicion,
                                  String nuevasObservaciones) throws DatosInvalidosException, LaboratorioException {
        Muestra muestra = buscarMuestraPorId(idMuestra);
        if (muestra == null) {
            throw new LaboratorioException("No existe la muestra con ID: " + idMuestra);
        }
        validarTextoObligatorio(nuevoOrigen, "Origen");
        validarTextoObligatorio(nuevaFecha, "Fecha");
        validarTextoObligatorio(nuevoResponsable, "Responsable");
        validarCondicionAmbiental(nuevaCondicion);

        muestra.setOrigen(nuevoOrigen);
        muestra.setFechaRecoleccion(nuevaFecha);
        muestra.setResponsableAnalisis(nuevoResponsable);
        muestra.setCondicionInicial(nuevaCondicion);
        muestra.setObservacionesGenerales(nuevasObservaciones);
        muestra.registrarEventoHistorial("Muestra actualizada el " + nuevaFecha);

        registrarEnHistorialGeneral("[ACTUALIZACION] Muestra ID: " + idMuestra);
        guardarDatos();
    }

    public void registrarMicroorganismoEnMuestra(String idMuestra, Microorganismo micro)
            throws DatosInvalidosException, LaboratorioException {
        Muestra muestra = buscarMuestraPorId(idMuestra);
        if (muestra == null) {
            throw new LaboratorioException("Debe seleccionar una muestra registrada para asociar el microorganismo.");
        }
        if (micro == null) {
            throw new DatosInvalidosException("El microorganismo no es valido.");
        }
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

        if (buscarMicroorganismoGlobalPorId(micro.getId()) != null) {
            throw new DatosInvalidosException("Ya existe un microorganismo con el ID: " + micro.getId());
        }

        micro.setIdMuestra(muestra.getId());
        muestra.agregarMicroorganismo(micro);
        registrarEnHistorialGeneral("[MICROORGANISMO] " + micro.getNombre() + " (" + micro.getTipo() + ") en Muestra " + muestra.getId());
        guardarDatos();
    }

    public Muestra buscarMuestraPorId(String idMuestra) {
        if (idMuestra == null) return null;
        for (Muestra m : muestras) {
            if (m.getId().equalsIgnoreCase(idMuestra.trim())) return m;
        }
        return null;
    }

    public Microorganismo buscarMicroorganismoGlobalPorId(String idMicro) {
        if (idMicro == null) return null;
        for (Muestra m : muestras) {
            Microorganismo micro = m.buscarMicroorganismoPorId(idMicro.trim());
            if (micro != null) return micro;
        }
        return null;
    }

    public List<Microorganismo> obtenerTodosLosMicroorganismos() {
        List<Microorganismo> todos = new ArrayList<>();
        for (Muestra m : muestras) {
            todos.addAll(m.getMicroorganismos());
        }
        return todos;
    }

    public List<Muestra> getMuestras() { return muestras; }
    public List<Observacion> getObservaciones() { return observaciones; }
    public List<Experimento> getExperimentos() { return experimentos; }
    public List<String> getHistorialGeneral() { return historialGeneral; }
}