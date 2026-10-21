package Frontend;

import Backend.Microorganismo;
import Backend.Muestra;
import Controller.ConsultaController;
import Controller.MuestraController;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;

/**
 * Panel dedicado a la consulta filtrada de muestras y microorganismos, asi como
 * a la visualizacion del reporte detallado por muestra y la bitacora general del laboratorio.
 *
 * @author javo
 */
public class PanelConsultasHistorial extends javax.swing.JPanel {

    // Controladores de negocio compartidos
    private MuestraController muestraCtrl;
    private ConsultaController consultaCtrl;

    // Modelo de datos para la tabla de resultados de busqueda
    private DefaultTableModel modeloTablaResultados;

    /**
     * Constructor por defecto requerido por el diseñador de NetBeans.
     */
    public PanelConsultasHistorial() {
        this(null, null);
    }

    /**
     * Constructor que conecta el panel con los controladores de muestras y consultas.
     *
     * @param muestraCtrl  Instancia de MuestraController.
     * @param consultaCtrl Instancia de ConsultaController.
     */
    public PanelConsultasHistorial(MuestraController muestraCtrl, ConsultaController consultaCtrl) {
        this.muestraCtrl = muestraCtrl;
        this.consultaCtrl = consultaCtrl;
        initComponents();
        configurarTablaYEventos();
        refrescarDatos();
    }

    // ==================== CONFIGURACION Y EVENTOS ====================

    /**
     * Inicializa la tabla de resultados y enlaza los eventos de selectores y botones.
     */
    private void configurarTablaYEventos() {
        modeloTablaResultados = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblResultadosFiltro.setModel(modeloTablaResultados);

        // Actualizar criterios disponibles al cambiar entre Microorganismos y Muestras
        cmbEntidadFiltro.addActionListener(e -> {
            actualizarCriteriosSegunEntidad();
            ejecutarFiltroBusqueda(true);
        });

        // Eventos de botones de busqueda
        btnBuscarFiltro.addActionListener(e -> ejecutarFiltroBusqueda(false));
        btnMostrarTodoFiltro.addActionListener(e -> {
            txtValorBusqueda.setText("");
            ejecutarFiltroBusqueda(true);
        });

        // Eventos de botones de historial
        btnConsultarHistorialMuestra.addActionListener(e -> mostrarHistorialDeMuestraSeleccionada());
        btnVerBitacoraGeneral.addActionListener(e -> mostrarBitacoraGeneral());

        actualizarCriteriosSegunEntidad();
    }

    /**
     * Ajusta los criterios del selector dependiendo de si se filtran Microorganismos o Muestras.
     */
    private void actualizarCriteriosSegunEntidad() {
        String entidad = (String) cmbEntidadFiltro.getSelectedItem();

        if ("Muestras".equalsIgnoreCase(entidad)) {
            cmbCriterioFiltro.setModel(new DefaultComboBoxModel<>(
                    new String[]{"ID", "Origen", "Fecha", "Responsable"}
            ));
        } else {
            cmbCriterioFiltro.setModel(new DefaultComboBoxModel<>(
                    new String[]{"Nombre", "Tipo", "Origen", "Fecha", "Estado", "ID"}
            ));
        }
    }

    /**
     * Recarga el selector de muestras para historial y actualiza la tabla de resultados.
     */
    public final void refrescarDatos() {
        if (muestraCtrl == null || modeloTablaResultados == null) {
            return;
        }

        String muestraPrevia = (String) cmbMuestraHistorial.getSelectedItem();
        cmbMuestraHistorial.removeAllItems();

        for (Muestra m : muestraCtrl.getMuestras()) {
            cmbMuestraHistorial.addItem(m.getId() + " - " + m.getOrigen());
        }

        if (muestraPrevia != null) {
            cmbMuestraHistorial.setSelectedItem(muestraPrevia);
        }

        ejecutarFiltroBusqueda(false);
    }

    // ==================== LOGICA DE BUSQUEDA Y REPORTES ====================

    /**
     * Ejecuta el filtro seleccionado y actualiza las columnas y filas de la tabla.
     *
     * @param mostrarTodo Si es true, ignora el texto de busqueda y muestra todos los registros.
     */
    private void ejecutarFiltroBusqueda(boolean mostrarTodo) {
        if (consultaCtrl == null || modeloTablaResultados == null) {
            return;
        }

        String entidad = (String) cmbEntidadFiltro.getSelectedItem();
        String criterio = (String) cmbCriterioFiltro.getSelectedItem();
        String valor = mostrarTodo ? "" : txtValorBusqueda.getText().trim();

        if ("Muestras".equalsIgnoreCase(entidad)) {
            List<Muestra> muestras = consultaCtrl.filtrarMuestras(criterio, valor);
            modeloTablaResultados.setColumnIdentifiers(new Object[]{
                "ID Muestra", "Origen", "Fecha", "Responsable", "Microorganismos", "Condición Inicial"
            });
            modeloTablaResultados.setRowCount(0);

            for (Muestra m : muestras) {
                String cond = (m.getCondicionInicial() != null)
                        ? m.getCondicionInicial().toString()
                        : "-";

                modeloTablaResultados.addRow(new Object[]{
                    m.getId(),
                    m.getOrigen(),
                    m.getFechaRecoleccion(),
                    m.getResponsableAnalisis(),
                    m.getMicroorganismos().size(),
                    cond
                });
            }

        } else {
            List<Microorganismo> microorganismos = consultaCtrl.filtrarMicroorganismos(criterio, valor);
            modeloTablaResultados.setColumnIdentifiers(new Object[]{
                "ID Micro.", "Muestra", "Nombre", "Tipo", "Estado", "Población", "Actividad (%)"
            });
            modeloTablaResultados.setRowCount(0);

            for (Microorganismo micro : microorganismos) {
                modeloTablaResultados.addRow(new Object[]{
                    micro.getId(),
                    micro.getIdMuestra(),
                    micro.getNombre(),
                    micro.getTipo(),
                    micro.getEstado(),
                    micro.getCantidadOrganismos(),
                    String.format("%.1f%%", micro.getNivelActividad())
                });
            }
        }
    }

    /**
     * Genera y muestra en el area de texto el historial detallado de la muestra seleccionada.
     */
    private void mostrarHistorialDeMuestraSeleccionada() {
        if (consultaCtrl == null) {
            return;
        }

        String idMuestra = obtenerIdSeleccionado(cmbMuestraHistorial);
        String reporte = consultaCtrl.obtenerReporteHistorialMuestra(idMuestra);
        txtAreaHistorial.setText(reporte);
        txtAreaHistorial.setCaretPosition(0);
    }

    /**
     * Muestra en el area de texto todas las entradas de la bitacora general del laboratorio.
     */
    private void mostrarBitacoraGeneral() {
        if (muestraCtrl == null) {
            return;
        }

        List<String> bitacora = muestraCtrl.getHistorialGeneral();
        StringBuilder sb = new StringBuilder();
        sb.append("=== BITÁCORA GENERAL DEL LABORATORIO ===\n\n");

        if (bitacora == null || bitacora.isEmpty()) {
            sb.append("Aún no se han registrado eventos en el laboratorio.\n");
        } else {
            for (int i = 0; i < bitacora.size(); i++) {
                sb.append(i + 1).append(". ").append(bitacora.get(i)).append("\n");
            }
        }

        txtAreaHistorial.setText(sb.toString());
        txtAreaHistorial.setCaretPosition(0);
    }

    /**
     * Extrae el identificador antes del guion ("ID - Descripcion") de un JComboBox.
     */
    private String obtenerIdSeleccionado(javax.swing.JComboBox<String> combo) {
        Object item = combo.getSelectedItem();
        if (item == null) {
            return "";
        }
        String texto = item.toString().trim();
        int idx = texto.indexOf(" - ");
        return (idx > 0) ? texto.substring(0, idx).trim() : texto;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbEntidadFiltro = new javax.swing.JComboBox<>();
        cmbCriterioFiltro = new javax.swing.JComboBox<>();
        txtValorBusqueda = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnBuscarFiltro = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnMostrarTodoFiltro = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblResultadosFiltro = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cmbMuestraHistorial = new javax.swing.JComboBox<>();
        btnConsultarHistorialMuestra = new javax.swing.JButton();
        btnVerBitacoraGeneral = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAreaHistorial = new javax.swing.JTextArea();

        cmbEntidadFiltro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Microorganismos", "Muestras" }));

        cmbCriterioFiltro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nombre", "Tipo", "Origen", "Fecha", "Estado", "Responsable" }));

        jLabel1.setText("Texto o criterio a buscar");

        btnBuscarFiltro.setText("Filtrar");

        jLabel2.setText("Filtros");

        btnMostrarTodoFiltro.setText("Mostrar todo");

        tblResultadosFiltro.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblResultadosFiltro);

        jLabel3.setText("Resultados");

        jLabel4.setText("Historial muestra");

        cmbMuestraHistorial.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnConsultarHistorialMuestra.setText("Historial de la muestra");

        btnVerBitacoraGeneral.setText("Historial general");

        txtAreaHistorial.setEditable(false);
        txtAreaHistorial.setColumns(20);
        txtAreaHistorial.setRows(5);
        jScrollPane2.setViewportView(txtAreaHistorial);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnBuscarFiltro)
                        .addGap(90, 90, 90)
                        .addComponent(btnMostrarTodoFiltro))
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cmbEntidadFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(57, 57, 57)
                        .addComponent(cmbCriterioFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtValorBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnConsultarHistorialMuestra)
                                .addGap(18, 18, 18)
                                .addComponent(btnVerBitacoraGeneral))
                            .addComponent(jLabel4)
                            .addComponent(cmbMuestraHistorial, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbEntidadFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbCriterioFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(cmbMuestraHistorial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtValorBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnBuscarFiltro)
                            .addComponent(btnMostrarTodoFiltro)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnConsultarHistorialMuestra)
                            .addComponent(btnVerBitacoraGeneral))))
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(136, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarFiltro;
    private javax.swing.JButton btnConsultarHistorialMuestra;
    private javax.swing.JButton btnMostrarTodoFiltro;
    private javax.swing.JButton btnVerBitacoraGeneral;
    private javax.swing.JComboBox<String> cmbCriterioFiltro;
    private javax.swing.JComboBox<String> cmbEntidadFiltro;
    private javax.swing.JComboBox<String> cmbMuestraHistorial;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblResultadosFiltro;
    private javax.swing.JTextArea txtAreaHistorial;
    private javax.swing.JTextField txtValorBusqueda;
    // End of variables declaration//GEN-END:variables
}
