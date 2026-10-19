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

public class ExperimentoController {

    private final MuestraController muestraCtrl;

    public ExperimentoController(MuestraController muestraCtrl) {
        this.muestraCtrl = muestraCtrl;
    }

    public void registrarObservacion(Observacion nuevaObs) throws DatosInvalidosException, LaboratorioException {
        if (nuevaObs == null) {
            throw new DatosInvalidosException("La observacion no puede ser nula.");
        }
        muestraCtrl.validarTextoObligatorio(nuevaObs.getId(), "ID de observacion");
        muestraCtrl.validarTextoObligatorio(nuevaObs.getIdMuestra(), "Muestra asociada");
        muestraCtrl.validarTextoObligatorio(nuevaObs.getIdMicroorganismo(), "Microorganismo asociado");
        muestraCtrl.validarTextoObligatorio(nuevaObs.getFecha(), "Fecha");
        muestraCtrl.validarTextoObligatorio(nuevaObs.getDescripcionResultados(), "Descripcion de resultados");
        muestraCtrl.validarCondicionAmbiental(nuevaObs.getCondicionAnalisis());

        for (Observacion o : muestraCtrl.getObservaciones()) {
            if (o.getId().equalsIgnoreCase(nuevaObs.getId().trim())) {
                throw new DatosInvalidosException("Ya existe una observacion con el ID: " + nuevaObs.getId());
            }
        }

        Muestra muestra = muestraCtrl.buscarMuestraPorId(nuevaObs.getIdMuestra());
        if (muestra == null) {
            throw new LaboratorioException("La muestra '" + nuevaObs.getIdMuestra() + "' no existe.");
        }
        Microorganismo micro = muestra.buscarMicroorganismoPorId(nuevaObs.getIdMicroorganismo());
        if (micro == null) {
            throw new LaboratorioException("Situacion especial: El microorganismo '" + nuevaObs.getIdMicroorganismo()
                    + "' no esta registrado en la muestra '" + muestra.getId() + "'.");
        }

        muestraCtrl.getObservaciones().add(nuevaObs);
        muestra.registrarEventoHistorial("Observacion " + nuevaObs.getId() + " en " + micro.getNombre() + ": " + nuevaObs.getDescripcionResultados());
        muestraCtrl.registrarEnHistorialGeneral("[OBSERVACION] ID: " + nuevaObs.getId() + " | Microorganismo: " + micro.getNombre());
        muestraCtrl.guardarDatos();
    }

    public List<Observacion> obtenerObservacionesPorMicroorganismo(String idMuestra, String idMicroorganismo) {
        List<Observacion> resultado = new ArrayList<>();
        for (Observacion obs : muestraCtrl.getObservaciones()) {
            boolean okMuestra = (idMuestra == null || idMuestra.isEmpty()) || obs.getIdMuestra().equalsIgnoreCase(idMuestra);
            boolean okMicro = (idMicroorganismo == null || idMicroorganismo.isEmpty()) || obs.getIdMicroorganismo().equalsIgnoreCase(idMicroorganismo);
            if (okMuestra && okMicro) {
                resultado.add(obs);
            }
        }
        return resultado;
    }

    public Experimento iniciarExperimento(String idExperimento, String fecha, String idMuestra,
                                          String idMicroorganismo, CondicionAmbiental condicionInicial)
            throws DatosInvalidosException, LaboratorioException {

        if (idMuestra == null || idMuestra.trim().isEmpty()) {
            throw new LaboratorioException("Situacion especial: No puede iniciar un experimento sin seleccionar una muestra.");
        }
        Muestra muestra = muestraCtrl.buscarMuestraPorId(idMuestra);
        if (muestra == null) {
            throw new LaboratorioException("Situacion especial: La muestra '" + idMuestra + "' no esta registrada.");
        }
        if (idMicroorganismo == null || idMicroorganismo.trim().isEmpty()) {
            throw new LaboratorioException("Situacion especial: Debe seleccionar un microorganismo para el experimento.");
        }
        Microorganismo micro = muestra.buscarMicroorganismoPorId(idMicroorganismo);
        if (micro == null) {
            throw new LaboratorioException("Situacion especial: El microorganismo '" + idMicroorganismo + "' no pertenece a la muestra.");
        }
        if (micro.getCantidadOrganismos() <= 0) {
            throw new LaboratorioException("Situacion incompatible: El microorganismo '" + micro.getNombre() + "' no tiene poblacion viva.");
        }

        muestraCtrl.validarTextoObligatorio(idExperimento, "ID del Experimento");
        muestraCtrl.validarTextoObligatorio(fecha, "Fecha");
        muestraCtrl.validarCondicionAmbiental(condicionInicial);

        if (buscarExperimentoPorId(idExperimento) != null) {
            throw new DatosInvalidosException("Ya existe un experimento con el ID: " + idExperimento);
        }

        Experimento nuevoExp = new Experimento(idExperimento, fecha, muestra.getId(), micro, condicionInicial);
        muestraCtrl.getExperimentos().add(nuevoExp);
        muestra.registrarEventoHistorial("Experimento " + idExperimento + " iniciado sobre " + micro.getNombre());
        muestraCtrl.registrarEnHistorialGeneral("[EXPERIMENTO] Iniciado ID: " + idExperimento + " sobre " + micro.getNombre());
        muestraCtrl.guardarDatos();
        return nuevoExp;
    }

    public CicloSimulacion ejecutarCicloExperimento(String idExperimento, CondicionAmbiental nuevaCondicion)
            throws DatosInvalidosException, LaboratorioException {

        Experimento exp = buscarExperimentoPorId(idExperimento);
        if (exp == null) {
            throw new LaboratorioException("Situacion especial: Debe seleccionar un experimento activo para correr el ciclo.");
        }
        muestraCtrl.validarCondicionAmbiental(nuevaCondicion);

        Muestra muestra = muestraCtrl.buscarMuestraPorId(exp.getIdMuestra());
        Microorganismo micro = (muestra != null) ? muestra.buscarMicroorganismoPorId(exp.getIdMicroorganismo()) : null;
        if (micro == null) {
            throw new LaboratorioException("Situacion especial: El microorganismo del experimento no fue encontrado.");
        }

        if (nuevaCondicion.getDisponibilidadNutrientes() == 0.0 && nuevaCondicion.getOxigeno() == 0.0
                && !Microorganismo.TIPO_EXTREMOFILO.equalsIgnoreCase(micro.getTipo())) {
            throw new LaboratorioException("Condicion incompatible con el estudio: 0% de oxigeno y 0% de nutrientes simultaneamente en organismo no extremofilo.");
        }

        CicloSimulacion ciclo = exp.ejecutarNuevoCiclo(nuevaCondicion, micro);
        muestra.registrarEventoHistorial("Exp " + exp.getId() + " - Ciclo " + ciclo.getNumeroCiclo() + ": " + ciclo.getEstadoGeneral());
        muestraCtrl.registrarEnHistorialGeneral("[SIMULACION] Exp " + exp.getId() + " Ciclo " + ciclo.getNumeroCiclo() + " -> Pob: " + ciclo.getCantidadOrganismos());
        muestraCtrl.guardarDatos();
        return ciclo;
    }

    public Experimento buscarExperimentoPorId(String idExperimento) {
        if (idExperimento == null) return null;
        for (Experimento e : muestraCtrl.getExperimentos()) {
            if (e.getId().equalsIgnoreCase(idExperimento.trim())) return e;
        }
        return null;
    }
}