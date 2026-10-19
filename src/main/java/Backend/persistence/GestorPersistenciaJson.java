package Backend.persistence;

import Backend.Bacteria;
import Backend.Experimento;
import Backend.Extremofilo;
import Backend.HongoMicroscopico;
import Backend.Microorganismo;
import Backend.Muestra;
import Backend.Observacion;
import Backend.Protozoo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GestorPersistenciaJson {

    private static final String RUTA_DEFECTO = "src/main/java/data/laboratorio_datos.json";
    private final String rutaArchivo;
    private final Gson gson;

    public GestorPersistenciaJson() {
        this(RUTA_DEFECTO);
    }

    public GestorPersistenciaJson(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Microorganismo.class, new MicroorganismoAdapter())
                .setPrettyPrinting()
                .create();
    }

    public void guardarDatos(List<Muestra> muestras, List<Observacion> observaciones,
                             List<Experimento> experimentos, List<String> historialGeneral) {
        try {
            File archivo = new File(rutaArchivo);
            if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
                archivo.getParentFile().mkdirs();
            }

            ContenedorDatos contenedor = new ContenedorDatos(
                    muestras, observaciones, experimentos, historialGeneral
            );

            try (FileWriter writer = new FileWriter(archivo)) {
                gson.toJson(contenedor, writer);
            }
        } catch (IOException e) {
            System.err.println("Error al guardar los datos en JSON: " + e.getMessage());
        }
    }

    public ContenedorDatos cargarDatos() {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return new ContenedorDatos();
        }

        try (FileReader reader = new FileReader(archivo)) {
            ContenedorDatos datos = gson.fromJson(reader, ContenedorDatos.class);
            return (datos != null) ? datos : new ContenedorDatos();
        } catch ( Exception e) {
            System.err.println("Error al cargar los datos desde JSON: " + e.getMessage());
            return new ContenedorDatos();
        }
    }

    // Estructura contenedora para serializar todo el estado del laboratorio
    public static class ContenedorDatos {
        private List<Muestra> muestras;
        private List<Observacion> observaciones;
        private List<Experimento> experimentos;
        private List<String> historialGeneral;

        public ContenedorDatos() {
            this.muestras = new ArrayList<>();
            this.observaciones = new ArrayList<>();
            this.experimentos = new ArrayList<>();
            this.historialGeneral = new ArrayList<>();
        }

        public ContenedorDatos(List<Muestra> muestras, List<Observacion> observaciones,
                               List<Experimento> experimentos, List<String> historialGeneral) {
            this.muestras = muestras;
            this.observaciones = observaciones;
            this.experimentos = experimentos;
            this.historialGeneral = historialGeneral;
        }

        public List<Muestra> getMuestras() {
            return muestras != null ? muestras : new ArrayList<>();
        }

        public List<Observacion> getObservaciones() {
            return observaciones != null ? observaciones : new ArrayList<>();
        }

        public List<Experimento> getExperimentos() {
            return experimentos != null ? experimentos : new ArrayList<>();
        }

        public List<String> getHistorialGeneral() {
            return historialGeneral != null ? historialGeneral : new ArrayList<>();
        }
    }

    // Adaptador para serializar y deserializar las subclases de la clase abstracta Microorganismo
    private static class MicroorganismoAdapter implements JsonSerializer<Microorganismo>, JsonDeserializer<Microorganismo> {

        @Override
        public JsonElement serialize(Microorganismo src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src, src.getClass());
        }

        @Override
        public Microorganismo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement tipoElement = jsonObject.get("tipo");

            if (tipoElement == null) {
                throw new JsonParseException("No se encontro el atributo 'tipo' en el microorganismo.");
            }

            String tipo = tipoElement.getAsString();
            if (Microorganismo.TIPO_BACTERIA.equalsIgnoreCase(tipo)) {
                return context.deserialize(jsonObject, Bacteria.class);
            } else if (Microorganismo.TIPO_PROTOZOO.equalsIgnoreCase(tipo)) {
                return context.deserialize(jsonObject, Protozoo.class);
            } else if (Microorganismo.TIPO_HONGO.equalsIgnoreCase(tipo)) {
                return context.deserialize(jsonObject, HongoMicroscopico.class);
            } else if (Microorganismo.TIPO_EXTREMOFILO.equalsIgnoreCase(tipo)) {
                return context.deserialize(jsonObject, Extremofilo.class);
            } else {
                throw new JsonParseException("Tipo de microorganismo desconocido: " + tipo);
            }
        }
    }
}