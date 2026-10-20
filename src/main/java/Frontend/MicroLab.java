package Frontend;

/**
 * Clase principal de arranque de la aplicacion MicroLab.
 * Despliega la ventana principal utilizando el tema original por defecto de Swing.
 *
 * @author javo
 */
public class MicroLab {

    /**
     * Metodo principal de ejecucion del programa.
     *
     * @param args Argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        // Iniciar la interfaz grafica en el hilo de eventos de Swing con el estilo original
        java.awt.EventQueue.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }

}
