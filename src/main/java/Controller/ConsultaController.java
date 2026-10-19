package Controller;

import Backend.Experimento;
import Backend.Microorganismo;
import Backend.Muestra;
import Backend.Observacion;

import java.util.ArrayList;
import java.util.List;

public class ConsultaController {

    private final MuestraController muestraCtrl;

    public ConsultaController(MuestraController muestraCtrl) {
        this.muestraCtrl = muestraCtrl;
    }

    public List<Muestra> filtrarMuestras(String criterio, String valorBusqueda) {
        if (valorBusqueda == null || valorBusqueda.trim().isEmpty()) {
            return new ArrayList<>(muestraCtrl.getMuestras());
        }
        String filtro = valorBusqueda.trim().toLowerCase();
        List<Muestra> resultado = new ArrayList<>();

        for (Muestra m : muestraCtrl.getMuestras()) {
            boolean coincide = false;
            if ("ID".equalsIgnoreCase(criterio)) {
                coincide = m.getId().toLowerCase().contains(filtro);
            } else if ("Origen".equalsIgnoreCase(criterio)) {
                coincide = m.getOrigen().toLowerCase().contains(filtro);
            } else if ("Fecha".equalsIgnoreCase(criterio)) {
                coincide = m.getFechaRecoleccion().toLowerCase().contains(filtro);
            } else if ("Responsable".equalsIgnoreCase(criterio)) {
                coincide = m.getResponsableAnalisis().toLowerCase().contains(filtro);
            } else {
                coincide = m.getId().toLowerCase().contains(filtro) || m.getOrigen().toLowerCase().contains(filtro);
            }
            if (coincide) resultado.add(m);
        }
        return resultado;
    }

    public List<Microorganismo> filtrarMicroorganismos(String criterio, String valorBusqueda) {
        if (valorBusqueda == null || valorBusqueda.trim().isEmpty()) {
            return muestraCtrl.obtenerTodosLosMicroorganismos();
        }
        String filtro = valorBusqueda.trim().toLowerCase();
        List<Microorganismo> resultado = new ArrayList<>();

        for (Muestra muestra : muestraCtrl.getMuestras()) {
            for (Microorganismo micro : muestra.getMicroorganismos()) {
                boolean coincide = false;
                if ("ID".equalsIgnoreCase(criterio)) {
                    coincide = micro.getId().toLowerCase().contains(filtro);
                } else if ("Nombre".equalsIgnoreCase(criterio)) {
                    coincide = micro.getNombre().toLowerCase().contains(filtro);
                } else if ("Tipo".equalsIgnoreCase(criterio)) {
                    coincide = micro.getTipo().toLowerCase().contains(filtro);
                } else if ("Origen".equalsIgnoreCase(criterio)) {
                    coincide = muestra.getOrigen().toLowerCase().contains(filtro);
                } else if ("Fecha".equalsIgnoreCase(criterio)) {
                    coincide = muestra.getFechaRecoleccion().toLowerCase().contains(filtro);
                } else if ("Estado".equalsIgnoreCase(criterio)) {
                    coincide = micro.getEstado().toLowerCase().contains(filtro);
                } else {
                    coincide = micro.getNombre().toLowerCase().contains(filtro) || micro.getTipo().toLowerCase().contains(filtro);
                }
                if (coincide) resultado.add(micro);
            }
        }
        return resultado;
    }

    public List<Experimento> obtenerExperimentosPorMuestra(String idMuestra) {
        if (idMuestra == null || idMuestra.trim().isEmpty()) {
            return new ArrayList<>(muestraCtrl.getExperimentos());
        }
        List<Experimento> resultado = new ArrayList<>();
        for (Experimento e : muestraCtrl.getExperimentos()) {
            if (e.getIdMuestra().equalsIgnoreCase(idMuestra.trim())) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    public String obtenerReporteHistorialMuestra(String idMuestra) {
        Muestra m = muestraCtrl.buscarMuestraPorId(idMuestra);
        if (m == null) {
            return "Seleccione una muestra valida para consultar su historial.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== HISTORIAL DE LA MUESTRA: ").append(m.getId()).append(" ===\n");
        sb.append("Origen: ").append(m.getOrigen()).append(" | Fecha: ").append(m.getFechaRecoleccion())
          .append(" | Responsable: ").append(m.getResponsableAnalisis()).append("\n");
        sb.append("Condicion Inicial: ").append(m.getCondicionInicial().toString()).append("\n\n");

        sb.append("--- MICROORGANISMOS ENCONTRADOS (").append(m.getMicroorganismos().size()).append(") ---\n");
        for (Microorganismo micro : m.getMicroorganismos()) {
            sb.append(" • [").append(micro.getId()).append("] ").append(micro.getNombre())
              .append(" (").append(micro.getTipo()).append(") | Estado: ").append(micro.getEstado())
              .append(" | Poblacion: ").append(micro.getCantidadOrganismos()).append("\n");
        }

        sb.append("\n--- OBSERVACIONES REGISTRADAS ---\n");
        for (Observacion o : muestraCtrl.getObservaciones()) {
            if (o.getIdMuestra().equalsIgnoreCase(m.getId())) {
                sb.append(" • [").append(o.getFecha()).append(" - ").append(o.getId()).append("] ")
                  .append("Micro ").append(o.getIdMicroorganismo()).append(": ")
                  .append(o.getDescripcionResultados()).append("\n");
            }
        }

        sb.append("\n--- EXPERIMENTOS Y RESULTADOS ---\n");
        for (Experimento e : obtenerExperimentosPorMuestra(m.getId())) {
            sb.append(e.obtenerComparacionInicialFinal()).append("\n\n");
        }

        sb.append("--- EVENTOS DE LA MUESTRA ---\n");
        for (String ev : m.getHistorialAnalisis()) {
            sb.append(" -> ").append(ev).append("\n");
        }
        return sb.toString();
    }
}