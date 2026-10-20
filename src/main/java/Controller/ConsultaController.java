package Controller;

import Backend.Experimento;
import Backend.Microorganismo;
import Backend.Muestra;
import Backend.Observacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador encargado de realizar busquedas filtradas de muestras y microorganismos,
 * asi como de generar reportes detallados del historial de cada muestra.
 */
public class ConsultaController {

    // Referencia al controlador principal de muestras
    private final MuestraController muestraCtrl;

    /**
     * Constructor que recibe el controlador de muestras para consultar sus datos.
     *
     * @param muestraCtrl Instancia de MuestraController.
     */
    public ConsultaController(MuestraController muestraCtrl) {
        this.muestraCtrl = muestraCtrl;
    }

    // ==================== FILTROS DE BUSQUEDA ====================

    /**
     * Filtra la lista de muestras segun un criterio especifico (ID, Origen, Fecha, Responsable).
     *
     * @param criterio      Campo por el cual se desea filtrar.
     * @param valorBusqueda Texto a buscar dentro del campo seleccionado.
     * @return Lista de muestras que coinciden con el filtro.
     */
    public List<Muestra> filtrarMuestras(String criterio, String valorBusqueda) {
        if (valorBusqueda == null || valorBusqueda.trim().isEmpty()) {
            return new ArrayList<>(muestraCtrl.getMuestras());
        }

        String filtro = valorBusqueda.trim().toLowerCase();
        List<Muestra> resultado = new ArrayList<>();

        for (Muestra m : muestraCtrl.getMuestras()) {
            boolean coincide = false;

            String id = (m.getId() != null) ? m.getId().toLowerCase() : "";
            String origen = (m.getOrigen() != null) ? m.getOrigen().toLowerCase() : "";
            String fecha = (m.getFechaRecoleccion() != null) ? m.getFechaRecoleccion().toLowerCase() : "";
            String responsable = (m.getResponsableAnalisis() != null) ? m.getResponsableAnalisis().toLowerCase() : "";

            if ("ID".equalsIgnoreCase(criterio)) {
                coincide = id.contains(filtro);

            } else if ("Origen".equalsIgnoreCase(criterio)) {
                coincide = origen.contains(filtro);

            } else if ("Fecha".equalsIgnoreCase(criterio)) {
                coincide = fecha.contains(filtro);

            } else if ("Responsable".equalsIgnoreCase(criterio)) {
                coincide = responsable.contains(filtro);

            } else {
                coincide = id.contains(filtro) || origen.contains(filtro);
            }

            if (coincide) {
                resultado.add(m);
            }
        }

        return resultado;
    }

    /**
     * Filtra los microorganismos registrados en todas las muestras segun el criterio indicado
     * (ID, Nombre, Tipo, Origen, Fecha, Estado).
     *
     * @param criterio      Campo por el cual se desea filtrar.
     * @param valorBusqueda Texto a buscar.
     * @return Lista de microorganismos que cumplen con el criterio de busqueda.
     */
    public List<Microorganismo> filtrarMicroorganismos(String criterio, String valorBusqueda) {
        if (valorBusqueda == null || valorBusqueda.trim().isEmpty()) {
            return muestraCtrl.obtenerTodosLosMicroorganismos();
        }

        String filtro = valorBusqueda.trim().toLowerCase();
        List<Microorganismo> resultado = new ArrayList<>();

        for (Muestra muestra : muestraCtrl.getMuestras()) {
            String origenMuestra = (muestra.getOrigen() != null) ? muestra.getOrigen().toLowerCase() : "";
            String fechaMuestra = (muestra.getFechaRecoleccion() != null) ? muestra.getFechaRecoleccion().toLowerCase() : "";

            for (Microorganismo micro : muestra.getMicroorganismos()) {
                boolean coincide = false;

                String id = (micro.getId() != null) ? micro.getId().toLowerCase() : "";
                String nombre = (micro.getNombre() != null) ? micro.getNombre().toLowerCase() : "";
                String tipo = (micro.getTipo() != null) ? micro.getTipo().toLowerCase() : "";
                String estado = (micro.getEstado() != null) ? micro.getEstado().toLowerCase() : "";

                if ("ID".equalsIgnoreCase(criterio)) {
                    coincide = id.contains(filtro);

                } else if ("Nombre".equalsIgnoreCase(criterio)) {
                    coincide = nombre.contains(filtro);

                } else if ("Tipo".equalsIgnoreCase(criterio)) {
                    coincide = tipo.contains(filtro);

                } else if ("Origen".equalsIgnoreCase(criterio)) {
                    coincide = origenMuestra.contains(filtro);

                } else if ("Fecha".equalsIgnoreCase(criterio)) {
                    coincide = fechaMuestra.contains(filtro);

                } else if ("Estado".equalsIgnoreCase(criterio)) {
                    coincide = estado.contains(filtro);

                } else {
                    coincide = nombre.contains(filtro) || tipo.contains(filtro);
                }

                if (coincide) {
                    resultado.add(micro);
                }
            }
        }

        return resultado;
    }

    /**
     * Obtiene todos los experimentos asociados a una muestra especifica.
     *
     * @param idMuestra ID de la muestra a consultar (o vacio para obtener todos).
     * @return Lista de experimentos filtrados por muestra.
     */
    public List<Experimento> obtenerExperimentosPorMuestra(String idMuestra) {
        if (idMuestra == null || idMuestra.trim().isEmpty()) {
            return new ArrayList<>(muestraCtrl.getExperimentos());
        }

        List<Experimento> resultado = new ArrayList<>();

        for (Experimento e : muestraCtrl.getExperimentos()) {
            if (e.getIdMuestra() != null && e.getIdMuestra().equalsIgnoreCase(idMuestra.trim())) {
                resultado.add(e);
            }
        }

        return resultado;
    }

    // ==================== GENERACION DE REPORTES ====================

    /**
     * Construye un reporte completo en texto con los datos de una muestra,
     * sus microorganismos, observaciones, experimentos y eventos registrados.
     *
     * @param idMuestra ID de la muestra a consultar.
     * @return Cadena formateada con el historial detallado de la muestra.
     */
    public String obtenerReporteHistorialMuestra(String idMuestra) {
        Muestra m = muestraCtrl.buscarMuestraPorId(idMuestra);
        if (m == null) {
            return "Seleccione una muestra valida para consultar su historial.";
        }

        StringBuilder sb = new StringBuilder();

        // Encabezado con datos generales de la muestra
        sb.append("=== HISTORIAL DE LA MUESTRA: ").append(m.getId()).append(" ===\n");
        sb.append("Origen: ").append(m.getOrigen())
          .append(" | Fecha: ").append(m.getFechaRecoleccion())
          .append(" | Responsable: ").append(m.getResponsableAnalisis()).append("\n");

        String condInicialTexto = (m.getCondicionInicial() != null)
                ? m.getCondicionInicial().toString()
                : "Sin condiciones registradas";
        sb.append("Condicion Inicial: ").append(condInicialTexto).append("\n\n");

        // Listado de microorganismos identificados en la muestra
        sb.append("--- MICROORGANISMOS ENCONTRADOS (").append(m.getMicroorganismos().size()).append(") ---\n");
        for (Microorganismo micro : m.getMicroorganismos()) {
            sb.append(" • [").append(micro.getId()).append("] ")
              .append(micro.getNombre()).append(" (").append(micro.getTipo()).append(")")
              .append(" | Estado: ").append(micro.getEstado())
              .append(" | Poblacion: ").append(micro.getCantidadOrganismos()).append("\n");
        }

        // Listado de observaciones asociadas a la muestra
        sb.append("\n--- OBSERVACIONES REGISTRADAS ---\n");
        for (Observacion o : muestraCtrl.getObservaciones()) {
            if (o.getIdMuestra() != null && o.getIdMuestra().equalsIgnoreCase(m.getId())) {
                sb.append(" • [").append(o.getFecha()).append(" - ").append(o.getId()).append("] ")
                  .append("Micro ").append(o.getIdMicroorganismo()).append(": ")
                  .append(o.getDescripcionResultados()).append("\n");
            }
        }

        // Resumen de experimentos realizados sobre la muestra
        sb.append("\n--- EXPERIMENTOS Y RESULTADOS ---\n");
        for (Experimento e : obtenerExperimentosPorMuestra(m.getId())) {
            sb.append(e.obtenerComparacionInicialFinal()).append("\n\n");
        }

        // Bitacora cronologica de eventos de la muestra
        sb.append("--- EVENTOS DE LA MUESTRA ---\n");
        for (String ev : m.getHistorialAnalisis()) {
            sb.append(" -> ").append(ev).append("\n");
        }

        return sb.toString();
    }

}