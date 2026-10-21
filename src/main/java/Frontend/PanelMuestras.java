package Frontend;

import Backend.CondicionAmbiental;
import Backend.Microorganismo;
import Backend.Muestra;
import Backend.exceptions.DatosInvalidosException;
import Backend.exceptions.LaboratorioException;
import Controller.MuestraController;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Panel encargado del registro, actualizacion, listado y visualizacion
 * detallada de las muestras recolectadas en el laboratorio.
 *
 * @author javo
 */
public class PanelMuestras extends javax.swing.JPanel {

    // Controlador principal de muestras
    private MuestraController muestraCtrl;

    // Modelo de datos para la tabla de muestras
    private DefaultTableModel modeloTabla;

    /**
     * Constructor por defecto requerido por el diseñador de NetBeans.
     */
    public PanelMuestras() {
        this(null);
    }

    /**
     * Constructor que conecta el panel con el controlador de muestras.
     *
     * @param muestraCtrl Instancia compartida de MuestraController.
     */
    public PanelMuestras(MuestraController muestraCtrl) {
        this.muestraCtrl = muestraCtrl;
        initComponents();
        configurarTablaYEventos();
        refrescarDatos();
    }

    // ==================== CONFIGURACION Y EVENTOS ====================

    /**
     * Configura las columnas de la tabla de muestras y enlaza los eventos
     * de los botones y de la seleccion en la tabla.
     */
    private void configurarTablaYEventos() {
        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Origen", "Fecha", "Responsable", "Temp (°C)", "pH", "Micros"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblMuestras.setModel(modeloTabla);

        // Eventos de botones
        btnRegistrarMuestra.addActionListener(e -> registrarMuestra());
        btnActualizarMuestra.addActionListener(e -> actualizarMuestra());

        // Evento al seleccionar una fila de la tabla para cargar sus datos en el formulario
        tblMuestras.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarMuestraSeleccionada();
            }
        });
    }

    /**
     * Recarga la tabla con todas las muestras almacenadas en el controlador.
     */
    public final void refrescarDatos() {
        if (muestraCtrl == null || modeloTabla == null) {
            return;
        }

        modeloTabla.setRowCount(0);

        for (Muestra m : muestraCtrl.getMuestras()) {
            CondicionAmbiental c = m.getCondicionInicial();
            double temp = (c != null) ? c.getTemperatura() : 0.0;
            double ph = (c != null) ? c.getPh() : 0.0;

            modeloTabla.addRow(new Object[]{
                m.getId(),
                m.getOrigen(),
                m.getFechaRecoleccion(),
                m.getResponsableAnalisis(),
                String.format("%.1f", temp),
                String.format("%.1f", ph),
                m.getMicroorganismos().size()
            });
        }
    }

    // ==================== LOGICA DE NEGOCIO DEL PANEL ====================

    /**
     * Registra una nueva muestra leyendo y validando los datos ingresados.
     */
    private void registrarMuestra() {
        if (muestraCtrl == null) {
            return;
        }

        try {
            CondicionAmbiental condicion = leerCondicionAmbientalFormulario();

            Muestra nueva = new Muestra(
                    txtIdMuestra.getText().trim(),
                    txtOrigenMuestra.getText().trim(),
                    txtFechaMuestra.getText().trim(),
                    txtResponsableMuestra.getText().trim(),
                    condicion,
                    txtObservacionesMuestra.getText().trim()
            );

            muestraCtrl.registrarMuestra(nueva);
            refrescarDatos();
            limpiarCampos();

            JOptionPane.showMessageDialog(
                    this,
                    "Muestra registrada correctamente.",
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (DatosInvalidosException | LaboratorioException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error de validacion",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Actualiza los datos de una muestra existente seleccionada por su ID.
     */
    private void actualizarMuestra() {
        if (muestraCtrl == null) {
            return;
        }

        try {
            String id = txtIdMuestra.getText().trim();
            CondicionAmbiental nuevaCondicion = leerCondicionAmbientalFormulario();

            muestraCtrl.actualizarMuestra(
                    id,
                    txtOrigenMuestra.getText().trim(),
                    txtFechaMuestra.getText().trim(),
                    txtResponsableMuestra.getText().trim(),
                    nuevaCondicion,
                    txtObservacionesMuestra.getText().trim()
            );

            refrescarDatos();
            mostrarDetalleMuestra(muestraCtrl.buscarMuestraPorId(id));

            JOptionPane.showMessageDialog(
                    this,
                    "Muestra actualizada correctamente.",
                    "Actualizacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (DatosInvalidosException | LaboratorioException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error al actualizar",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Carga en los campos de texto y en el area de detalle la muestra seleccionada en la tabla.
     */
    private void cargarMuestraSeleccionada() {
        int fila = tblMuestras.getSelectedRow();
        if (fila < 0 || muestraCtrl == null) {
            return;
        }

        String idMuestra = String.valueOf(modeloTabla.getValueAt(fila, 0));
        Muestra m = muestraCtrl.buscarMuestraPorId(idMuestra);
        if (m == null) {
            return;
        }

        txtIdMuestra.setText(m.getId());
        txtOrigenMuestra.setText(m.getOrigen());
        txtFechaMuestra.setText(m.getFechaRecoleccion());
        txtResponsableMuestra.setText(m.getResponsableAnalisis());
        txtObservacionesMuestra.setText(m.getObservacionesGenerales());

        CondicionAmbiental c = m.getCondicionInicial();
        if (c != null) {
            txtTempMuestra.setText(String.valueOf(c.getTemperatura()));
            txtPhMuestra.setText(String.valueOf(c.getPh()));
            txtOxigenoMuestra.setText(String.valueOf(c.getOxigeno()));
            txtHumedadMuestra.setText(String.valueOf(c.getHumedad()));
            txtSalinidadMuestra.setText(String.valueOf(c.getSalinidad()));
            txtNutrientesMuestra.setText(String.valueOf(c.getDisponibilidadNutrientes()));
        }

        mostrarDetalleMuestra(m);
    }

    /**
     * Muestra en el area de texto lateral toda la informacion detallada de una muestra.
     */
    private void mostrarDetalleMuestra(Muestra m) {
        if (m == null) {
            txtDetalleMuestra.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== DETALLE DE MUESTRA ").append(m.getId()).append(" ===\n");
        sb.append("Origen: ").append(m.getOrigen()).append("\n");
        sb.append("Fecha: ").append(m.getFechaRecoleccion()).append("\n");
        sb.append("Responsable: ").append(m.getResponsableAnalisis()).append("\n");
        sb.append("Condicion Inicial:\n  ").append(m.getCondicionInicial()).append("\n");
        sb.append("Observaciones: ").append(m.getObservacionesGenerales()).append("\n\n");

        sb.append("Microorganismos asociados (").append(m.getMicroorganismos().size()).append("):\n");
        for (Microorganismo micro : m.getMicroorganismos()) {
            sb.append(" - [").append(micro.getId()).append("] ")
              .append(micro.getNombre()).append(" (").append(micro.getTipo()).append(")\n");
        }

        txtDetalleMuestra.setText(sb.toString());
    }

    /**
     * Lee y convierte los valores numericos de las condiciones ambientales del formulario.
     */
    private CondicionAmbiental leerCondicionAmbientalFormulario() throws DatosInvalidosException {
        try {
            double temp = Double.parseDouble(txtTempMuestra.getText().trim());
            double ph = Double.parseDouble(txtPhMuestra.getText().trim());
            double oxigeno = Double.parseDouble(txtOxigenoMuestra.getText().trim());
            double humedad = Double.parseDouble(txtHumedadMuestra.getText().trim());
            double salinidad = Double.parseDouble(txtSalinidadMuestra.getText().trim());
            double nutrientes = Double.parseDouble(txtNutrientesMuestra.getText().trim());

            return new CondicionAmbiental(temp, ph, oxigeno, humedad, salinidad, nutrientes);

        } catch (NumberFormatException ex) {
            throw new DatosInvalidosException(
                    "Todos los campos de condiciones ambientales (temperatura, pH, oxigeno, humedad, salinidad y nutrientes) deben ser valores numericos validos."
            );
        }
    }

    /**
     * Limpia todos los campos del formulario de muestras.
     */
    private void limpiarCampos() {
        txtIdMuestra.setText("");
        txtOrigenMuestra.setText("");
        txtFechaMuestra.setText("");
        txtResponsableMuestra.setText("");
        txtTempMuestra.setText("");
        txtPhMuestra.setText("");
        txtOxigenoMuestra.setText("");
        txtHumedadMuestra.setText("");
        txtSalinidadMuestra.setText("");
        txtNutrientesMuestra.setText("");
        txtObservacionesMuestra.setText("");
        txtDetalleMuestra.setText("");
        tblMuestras.clearSelection();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtIdMuestra = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtOrigenMuestra = new javax.swing.JTextField();
        txtFechaMuestra = new javax.swing.JTextField();
        txtResponsableMuestra = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtTempMuestra = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtPhMuestra = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtOxigenoMuestra = new javax.swing.JTextField();
        txtHumedadMuestra = new javax.swing.JTextField();
        txtSalinidadMuestra = new javax.swing.JTextField();
        txtNutrientesMuestra = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtObservacionesMuestra = new javax.swing.JTextArea();
        btnRegistrarMuestra = new javax.swing.JButton();
        btnActualizarMuestra = new javax.swing.JButton();
        btnLimpiarMuestra = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblMuestras = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDetalleMuestra = new javax.swing.JTextArea();

        txtIdMuestra.addActionListener(this::txtIdMuestraActionPerformed);

        jLabel1.setText("Identificador de la muestra:");

        jLabel2.setText("Origen de la muestra:");

        jLabel3.setText("Fecha de recolección");

        jLabel4.setText("Persona responsable del análisis");

        jLabel5.setText("Temperatura inicial (°C)");

        txtTempMuestra.addActionListener(this::txtTempMuestraActionPerformed);

        jLabel6.setText("pH inicial (0.0 a 14.0):");

        jLabel7.setText("Oxígeno inicial (%)");

        jLabel8.setText("Humedad inicial (%)");

        jLabel9.setText("Salinidad inicial (%)");

        txtOxigenoMuestra.addActionListener(this::txtOxigenoMuestraActionPerformed);

        jLabel10.setText("Disponibilidad de nutrientes inicial (%)");

        txtObservacionesMuestra.setColumns(20);
        txtObservacionesMuestra.setRows(5);
        jScrollPane1.setViewportView(txtObservacionesMuestra);

        btnRegistrarMuestra.setText("Registrar Muestra");

        btnActualizarMuestra.setText("Actualizar Muestra");

        btnLimpiarMuestra.setText("Limpiar campos");
        btnLimpiarMuestra.addActionListener(this::btnLimpiarMuestraActionPerformed);

        tblMuestras.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(tblMuestras);

        txtDetalleMuestra.setEditable(false);
        txtDetalleMuestra.setColumns(20);
        txtDetalleMuestra.setRows(5);
        jScrollPane2.setViewportView(txtDetalleMuestra);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(txtIdMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtOrigenMuestra)
                                .addComponent(jLabel3)
                                .addComponent(txtFechaMuestra))
                            .addComponent(jLabel4)
                            .addComponent(txtResponsableMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(txtTempMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(145, 145, 145)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(txtPhMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtOxigenoMuestra, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtSalinidadMuestra, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtHumedadMuestra, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(txtNutrientesMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnRegistrarMuestra)
                            .addComponent(btnActualizarMuestra)
                            .addComponent(btnLimpiarMuestra)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 445, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(49, 49, 49)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPhMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtOrigenMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtOxigenoMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFechaMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHumedadMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtResponsableMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSalinidadMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTempMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNutrientesMuestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRegistrarMuestra)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnActualizarMuestra)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnLimpiarMuestra)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtIdMuestraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdMuestraActionPerformed
    }//GEN-LAST:event_txtIdMuestraActionPerformed

    private void txtTempMuestraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTempMuestraActionPerformed
    }//GEN-LAST:event_txtTempMuestraActionPerformed

    private void txtOxigenoMuestraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOxigenoMuestraActionPerformed
    }//GEN-LAST:event_txtOxigenoMuestraActionPerformed

    private void btnLimpiarMuestraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarMuestraActionPerformed
        limpiarCampos();
    }//GEN-LAST:event_btnLimpiarMuestraActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizarMuestra;
    private javax.swing.JButton btnLimpiarMuestra;
    private javax.swing.JButton btnRegistrarMuestra;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable tblMuestras;
    private javax.swing.JTextArea txtDetalleMuestra;
    private javax.swing.JTextField txtFechaMuestra;
    private javax.swing.JTextField txtHumedadMuestra;
    private javax.swing.JTextField txtIdMuestra;
    private javax.swing.JTextField txtNutrientesMuestra;
    private javax.swing.JTextArea txtObservacionesMuestra;
    private javax.swing.JTextField txtOrigenMuestra;
    private javax.swing.JTextField txtOxigenoMuestra;
    private javax.swing.JTextField txtPhMuestra;
    private javax.swing.JTextField txtResponsableMuestra;
    private javax.swing.JTextField txtSalinidadMuestra;
    private javax.swing.JTextField txtTempMuestra;
    // End of variables declaration//GEN-END:variables
}
