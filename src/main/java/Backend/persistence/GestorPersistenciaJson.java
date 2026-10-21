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

/**
 * Clase encargada de gestionar el almacenamiento y carga de todos los datos
 * del laboratorio en formato JSON utilizando la libreria Gson.
 */
public class GestorPersistenciaJson {

    // Ruta por defecto donde se almacena el archivo JSON del laboratorio
    private static final String RUTA_DEFECTO = "src/main/java/data/laboratorio_datos.json";

    private final String rutaArchivo;
    private final Gson gson;

    /**
     * Constructor por defecto que utiliza la ruta estandar del proyecto.
     */
    public GestorPersistenciaJson() {
        this(RUTA_DEFECTO);
    }

    /**
     * Constructor que permite especificar una ruta personalizada para el archivo JSON
     * y configura el adaptador polimorfico para la jerarquia de Microorganismo.
     *
     * @param rutaArchivo Ruta del archivo JSON.
     */
    public GestorPersistenciaJson(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Microorganismo.class, new MicroorganismoAdapter())
                .setPrettyPrinting()
                .create();
    }

    /**
     * Guarda las listas de muestras, observaciones, experimentos e historial general
     * en el archivo JSON configurado.
     */
    public void guardarDatos(
            List<Muestra> muestras,
            List<Observacion> observaciones,
            List<Experimento> experimentos,
            List<String> historialGeneral
    ) {
        try {
            File archivo = new File(rutaArchivo);

            // Crear los directorios padre en caso de que no existan
            if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
                archivo.getParentFile().mkdirs();
            }

            ContenedorDatos contenedor = new ContenedorDatos(
                    muestras,
                    observaciones,
                    experimentos,
                    historialGeneral
            );

            try (FileWriter writer = new FileWriter(archivo)) {
                gson.toJson(contenedor, writer);
            }

        } catch (IOException e) {
            System.err.println("Error al guardar los datos en JSON: " + e.getMessage());
        }
    }

    /**
     * Carga y deserializa los datos almacenados en el archivo JSON.
     * Si el archivo no existe o esta vacio, devuelve un contenedor nuevo con listas vacias.
     *
     * @return Objeto ContenedorDatos con toda la informacion recuperada.
     */
    public ContenedorDatos cargarDatos() {
        File archivo = new File(rutaArchivo);

        if (!archivo.exists() || archivo.length() == 0) {
            return new ContenedorDatos();
        }

        try (FileReader reader = new FileReader(archivo)) {
            ContenedorDatos datos = gson.fromJson(reader, ContenedorDatos.class);
            return (datos != null) ? datos : new ContenedorDatos();

        } catch (Exception e) {
            System.err.println("Error al cargar los datos desde JSON: " + e.getMessage());
            return new ContenedorDatos();
        }
    }

    /**
     * Clase interna auxiliar que agrupa todas las listas del sistema
     * para serializarlas y deserializarlas en un unico objeto JSON.
     */
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

        public ContenedorDatos(
                List<Muestra> muestras,
                List<Observacion> observaciones,
                List<Experimento> experimentos,
                List<String> historialGeneral
        ) {
            this.muestras = muestras;
            this.observaciones = observaciones;
            this.experimentos = experimentos;
            this.historialGeneral = historialGeneral;
        }

        public List<Muestra> getMuestras() {
            if (muestras == null) {
                muestras = new ArrayList<>();
            }
            return muestras;
        }

        public List<Observacion> getObservaciones() {
            if (observaciones == null) {
                observaciones = new ArrayList<>();
            }
            return observaciones;
        }

        public List<Experimento> getExperimentos() {
            if (experimentos == null) {
                experimentos = new ArrayList<>();
            }
            return experimentos;
        }

        public List<String> getHistorialGeneral() {
            if (historialGeneral == null) {
                historialGeneral = new ArrayList<>();
            }
            return historialGeneral;
        }

    }

    /**
     * Adaptador personalizado de Gson para serializar y deserializar correctamente
     * las subclases concretas de la clase abstracta Microorganismo segun su campo 'tipo'.
     */
    private static class MicroorganismoAdapter
            implements JsonSerializer<Microorganismo>, JsonDeserializer<Microorganismo> {

        @Override
        public JsonElement serialize(Microorganismo src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src, src.getClass());
        }

        @Override
        public Microorganismo deserialize(
                JsonElement json,
                Type typeOfT,
                JsonDeserializationContext context
        ) throws JsonParseException {

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