package Frontend;

import Backend.CondicionAmbiental;
import Backend.Microorganismo;
import Backend.Muestra;
import Backend.Observacion;
import Backend.exceptions.DatosInvalidosException;
import Backend.exceptions.LaboratorioException;
import Controller.ExperimentoController;
import Controller.MuestraController;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Panel destinado al registro y consulta de observaciones cientificas realizadas
 * sobre los microorganismos de una muestra bajo condiciones ambientales especificas.
 *
 * @author javo
 */
public class PanelObservaciones extends javax.swing.JPanel {

    // Controladores de negocio compartidos
    private MuestraController muestraCtrl;
    private ExperimentoController experimentoCtrl;

    // Modelo de datos para la tabla de observaciones
    private DefaultTableModel modeloTabla;

    /**
     * Constructor por defecto requerido por el diseñador de NetBeans.
     */
    public PanelObservaciones() {
        this(null, null);
    }

    /**
     * Constructor que conecta el panel con los controladores de muestras y experimentos.
     *
     * @param muestraCtrl     Instancia de MuestraController.
     * @param experimentoCtrl Instancia de ExperimentoController.
     */
    public PanelObservaciones(MuestraController muestraCtrl, ExperimentoController experimentoCtrl) {
        this.muestraCtrl = muestraCtrl;
        this.experimentoCtrl = experimentoCtrl;
        initComponents();
        configurarTablaYEventos();
        refrescarDatos();
    }

    // ==================== CONFIGURACION Y EVENTOS ====================

    /**
     * Configura las columnas de la tabla de observaciones y enlaza los botones y listas.
     */
    private void configurarTablaYEventos() {
        modeloTabla = new DefaultTableModel(
                new Object[]{
                    "ID Obs.", "Fecha", "Muestra", "Microorganismo",
                    "Condición de Análisis", "Descripción / Resultados"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblObservaciones.setModel(modeloTabla);

        // Al cambiar la muestra seleccionada, actualizar los microorganismos disponibles
        cmbMuestraObs.addActionListener(e -> actualizarComboMicroorganismos());

        // Acciones de los botones
        btnRegistrarObs.addActionListener(e -> registrarObservacion());
        btnFiltrarObsPorMicro.addActionListener(e -> filtrarObservacionesPorSeleccion());
        btnMostrarTodasObs.addActionListener(e -> mostrarTodasLasObservaciones());
    }

    /**
     * Recarga los selectores de muestras y microorganismos y actualiza la tabla de observaciones.
     */
    public final void refrescarDatos() {
        if (muestraCtrl == null || modeloTabla == null) {
            return;
        }

        String seleccionPrevia = (String) cmbMuestraObs.getSelectedItem();
        cmbMuestraObs.removeAllItems();

        for (Muestra m : muestraCtrl.getMuestras()) {
            cmbMuestraObs.addItem(m.getId() + " - " + m.getOrigen());
        }

        if (seleccionPrevia != null) {
            cmbMuestraObs.setSelectedItem(seleccionPrevia);
        }

        actualizarComboMicroorganismos();
        mostrarTodasLasObservaciones();
    }

    /**
     * Actualiza el selector de microorganismos segun la muestra seleccionada actualmente.
     */
    private void actualizarComboMicroorganismos() {
        if (muestraCtrl == null) {
            return;
        }

        cmbMicroorganismoObs.removeAllItems();
        String idMuestra = obtenerIdSeleccionado(cmbMuestraObs);
        Muestra muestra = muestraCtrl.buscarMuestraPorId(idMuestra);

        if (muestra != null) {
            for (Microorganismo micro : muestra.getMicroorganismos()) {
                cmbMicroorganismoObs.addItem(
                        micro.getId() + " - " + micro.getNombre() + " (" + micro.getTipo() + ")"
                );
            }

            // Prellenar condiciones ambientales de la muestra si los campos estan vacios
            if (muestra.getCondicionInicial() != null && txtTempObs.getText().trim().isEmpty()) {
                CondicionAmbiental c = muestra.getCondicionInicial();
                txtTempObs.setText(String.valueOf(c.getTemperatura()));
                txtPhObs.setText(String.valueOf(c.getPh()));
                txtOxigenoObs.setText(String.valueOf(c.getOxigeno()));
                txtHumedadObs.setText(String.valueOf(c.getHumedad()));
                txtSalinidadObs.setText(String.valueOf(c.getSalinidad()));
                txtNutrientesObs.setText(String.valueOf(c.getDisponibilidadNutrientes()));
            }
        }
    }

    // ==================== LOGICA DE NEGOCIO ====================

    /**
     * Registra una nueva observacion validando los datos ingresados en el formulario.
     */
    private void registrarObservacion() {
        if (experimentoCtrl == null || muestraCtrl == null) {
            return;
        }

        try {
            String idMuestra = obtenerIdSeleccionado(cmbMuestraObs);
            String idMicro = obtenerIdSeleccionado(cmbMicroorganismoObs);

            if (idMuestra.isEmpty()) {
                throw new LaboratorioException("Debe seleccionar una muestra estudiada.");
            }
            if (idMicro.isEmpty()) {
                throw new LaboratorioException("Debe seleccionar un microorganismo dentro de la muestra.");
            }

            String idObs = txtIdObs.getText().trim();
            String fecha = txtFechaObs.getText().trim();
            String descripcion = txtDescripcionObs.getText().trim();
            CondicionAmbiental condicion = leerCondicionAnalisis();

            Observacion nuevaObs = new Observacion(
                    idObs, idMuestra, idMicro, fecha, condicion, descripcion
            );

            experimentoCtrl.registrarObservacion(nuevaObs);
            mostrarTodasLasObservaciones();
            limpiarCampos();

            JOptionPane.showMessageDialog(
                    this,
                    "Observación registrada correctamente.",
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (DatosInvalidosException | LaboratorioException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error al registrar observación",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Filtra la tabla de observaciones segun la muestra y el microorganismo seleccionados.
     */
    private void filtrarObservacionesPorSeleccion() {
        if (experimentoCtrl == null) {
            return;
        }

        String idMuestra = obtenerIdSeleccionado(cmbMuestraObs);
        String idMicro = obtenerIdSeleccionado(cmbMicroorganismoObs);

        List<Observacion> filtradas = experimentoCtrl.obtenerObservacionesPorMicroorganismo(
                idMuestra, idMicro
        );
        cargarTablaObservaciones(filtradas);
    }

    /**
     * Muestra todas las observaciones registradas en el laboratorio.
     */
    private void mostrarTodasLasObservaciones() {
        if (muestraCtrl == null) {
            return;
        }
        cargarTablaObservaciones(muestraCtrl.getObservaciones());
    }

    /**
     * Llena la tabla con la lista de observaciones proporcionada.
     */
    private void cargarTablaObservaciones(List<Observacion> lista) {
        if (modeloTabla == null) {
            return;
        }

        modeloTabla.setRowCount(0);
        if (lista == null) {
            return;
        }

        for (Observacion obs : lista) {
            String condTexto = (obs.getCondicionAnalisis() != null)
                    ? obs.getCondicionAnalisis().toString()
                    : "-";

            modeloTabla.addRow(new Object[]{
                obs.getId(),
                obs.getFecha(),
                obs.getIdMuestra(),
                obs.getIdMicroorganismo(),
                condTexto,
                obs.getDescripcionResultados()
            });
        }
    }

    /**
     * Lee y convierte los campos numericos de las condiciones durante el analisis.
     */
    private CondicionAmbiental leerCondicionAnalisis() throws DatosInvalidosException {
        try {
            double temp = Double.parseDouble(txtTempObs.getText().trim());
            double ph = Double.parseDouble(txtPhObs.getText().trim());
            double oxigeno = Double.parseDouble(txtOxigenoObs.getText().trim());
            double humedad = Double.parseDouble(txtHumedadObs.getText().trim());
            double salinidad = Double.parseDouble(txtSalinidadObs.getText().trim());
            double nutrientes = Double.parseDouble(txtNutrientesObs.getText().trim());

            return new CondicionAmbiental(temp, ph, oxigeno, humedad, salinidad, nutrientes);

        } catch (NumberFormatException ex) {
            throw new DatosInvalidosException(
                    "Todos los parámetros ambientales del análisis deben ser valores numéricos."
            );
        }
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
     * Limpia los campos de entrada de la observacion tras un registro exitoso.
     */
    private void limpiarCampos() {
        txtIdObs.setText("");
        txtFechaObs.setText("");
        txtDescripcionObs.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbMuestraObs = new javax.swing.JComboBox<>();
        cmbMicroorganismoObs = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnActualizarCombosObs = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtIdObs = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtFechaObs = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtTempObs = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtPhObs = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtOxigenoObs = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtHumedadObs = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtSalinidadObs = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtNutrientesObs = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDescripcionObs = new javax.swing.JTextArea();
        jLabel11 = new javax.swing.JLabel();
        btnRegistrarObs = new javax.swing.JButton();
        btnFiltrarObsPorMicro = new javax.swing.JButton();
        btnMostrarTodasObs = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblObservaciones = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();

        cmbMuestraObs.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmbMicroorganismoObs.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("Selecciona la muestra estudiada");

        jLabel2.setText("Selecciona el microorganismo dentro de esa muestra");

        btnActualizarCombosObs.setText("Actualizar lista");
        btnActualizarCombosObs.addActionListener(this::btnActualizarCombosObsActionPerformed);

        jLabel3.setText("Identificador de la observacion");

        jLabel4.setText("Fecha observacion");

        jLabel5.setText("Temperatura durante los analisis");

        jLabel6.setText("PH durante los analisis");

        jLabel7.setText("Oxigeneno durante los analisis");

        jLabel8.setText("Humedad durante los analisis");

        jLabel9.setText("Salinidad durante los analisis");

        jLabel10.setText("Nutrientes durante los analisis");

        txtDescripcionObs.setColumns(20);
        txtDescripcionObs.setRows(5);
        jScrollPane1.setViewportView(txtDescripcionObs);

        jLabel11.setText("Descripcion de los analisis");

        btnRegistrarObs.setText("Registrar Analisis");

        btnFiltrarObsPorMicro.setText("Ver analisis");

        btnMostrarTodasObs.setText("Ver todos los analisis");

        tblObservaciones.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblObservaciones);

        jLabel12.setText("Historial de observaciones");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnActualizarCombosObs)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(cmbMuestraObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbMicroorganismoObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(txtIdObs, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(txtFechaObs, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(txtPhObs, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTempObs, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(btnMostrarTodasObs)
                            .addComponent(btnFiltrarObsPorMicro)
                            .addComponent(jLabel11)
                            .addComponent(btnRegistrarObs)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(143, 143, 143)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)
                            .addComponent(txtNutrientesObs, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(txtSalinidadObs, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(txtHumedadObs, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(txtOxigenoObs, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))))
                .addContainerGap(203, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbMuestraObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGap(4, 4, 4)
                .addComponent(cmbMicroorganismoObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnActualizarCombosObs)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIdObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtOxigenoObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFechaObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHumedadObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTempObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSalinidadObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPhObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNutrientesObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnRegistrarObs)
                                .addGap(18, 18, 18)
                                .addComponent(btnFiltrarObsPorMicro)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnMostrarTodasObs)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnActualizarCombosObsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarCombosObsActionPerformed
        refrescarDatos();
    }//GEN-LAST:event_btnActualizarCombosObsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizarCombosObs;
    private javax.swing.JButton btnFiltrarObsPorMicro;
    private javax.swing.JButton btnMostrarTodasObs;
    private javax.swing.JButton btnRegistrarObs;
    private javax.swing.JComboBox<String> cmbMicroorganismoObs;
    private javax.swing.JComboBox<String> cmbMuestraObs;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblObservaciones;
    private javax.swing.JTextArea txtDescripcionObs;
    private javax.swing.JTextField txtFechaObs;
    private javax.swing.JTextField txtHumedadObs;
    private javax.swing.JTextField txtIdObs;
    private javax.swing.JTextField txtNutrientesObs;
    private javax.swing.JTextField txtOxigenoObs;
    private javax.swing.JTextField txtPhObs;
    private javax.swing.JTextField txtSalinidadObs;
    private javax.swing.JTextField txtTempObs;
    // End of variables declaration//GEN-END:variables
}
