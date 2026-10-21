package Frontend;

import Backend.CicloSimulacion;
import Backend.CondicionAmbiental;
import Backend.Experimento;
import Backend.Microorganismo;
import Backend.Muestra;
import Backend.exceptions.DatosInvalidosException;
import Backend.exceptions.LaboratorioException;
import Controller.ExperimentoController;
import Controller.MuestraController;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Panel encargado de la creacion de experimentos de simulacion y de la ejecucion
 * ciclo a ciclo del comportamiento de los microorganismos ante cambios ambientales.
 *
 * @author javo
 */
public class PanelExperimentoSimulacion extends javax.swing.JPanel {

    // Controladores de negocio compartidos
    private MuestraController muestraCtrl;
    private ExperimentoController experimentoCtrl;

    // Modelo de datos para la tabla de ciclos de simulacion
    private DefaultTableModel modeloTablaCiclos;

    /**
     * Constructor por defecto requerido por el diseñador de NetBeans.
     */
    public PanelExperimentoSimulacion() {
        this(null, null);
    }

    /**
     * Constructor que conecta el panel con los controladores de muestras y experimentos.
     *
     * @param muestraCtrl     Instancia de MuestraController.
     * @param experimentoCtrl Instancia de ExperimentoController.
     */
    public PanelExperimentoSimulacion(MuestraController muestraCtrl, ExperimentoController experimentoCtrl) {
        this.muestraCtrl = muestraCtrl;
        this.experimentoCtrl = experimentoCtrl;
        initComponents();
        configurarTablaYEventos();
        refrescarDatos();
    }

    // ==================== CONFIGURACION Y EVENTOS ====================

    /**
     * Configura las columnas de la tabla de ciclos y enlaza los selectores y botones.
     */
    private void configurarTablaYEventos() {
        modeloTablaCiclos = new DefaultTableModel(
                new Object[]{
                    "Ciclo #", "Población", "Actividad (%)", "Crecimiento (%)",
                    "Reproducción", "Estado General", "Condición Aplicada", "Respuesta"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCiclosSimulacion.setModel(modeloTablaCiclos);
        txtComparacionResultados.setEditable(false);

        // Eventos de selectores desplegables
        cmbMuestraExp.addActionListener(e -> actualizarComboMicroorganismos());
        cmbExperimentoActivo.addActionListener(e -> cargarDetalleExperimentoActivo());

        // Eventos de botones
        btnRefrescarListasExp.addActionListener(e -> refrescarDatos());
        btnIniciarExperimento.addActionListener(e -> iniciarNuevoExperimento());
        btnEjecutarCiclo.addActionListener(e -> ejecutarCicloEnExperimentoActivo());
    }

    /**
     * Recarga las listas de muestras, microorganismos y experimentos activos.
     */
    public final void refrescarDatos() {
        if (muestraCtrl == null || modeloTablaCiclos == null) {
            return;
        }

        // Actualizar selector de muestras
        String muestraPrevia = (String) cmbMuestraExp.getSelectedItem();
        cmbMuestraExp.removeAllItems();
        for (Muestra m : muestraCtrl.getMuestras()) {
            cmbMuestraExp.addItem(m.getId() + " - " + m.getOrigen());
        }
        if (muestraPrevia != null) {
            cmbMuestraExp.setSelectedItem(muestraPrevia);
        }

        actualizarComboMicroorganismos();

        // Actualizar selector de experimentos activos
        String expPrevio = (String) cmbExperimentoActivo.getSelectedItem();
        cmbExperimentoActivo.removeAllItems();
        for (Experimento exp : muestraCtrl.getExperimentos()) {
            cmbExperimentoActivo.addItem(
                    exp.getId() + " - " + exp.getNombreMicroorganismo() + " (Muestra " + exp.getIdMuestra() + ")"
            );
        }
        if (expPrevio != null) {
            cmbExperimentoActivo.setSelectedItem(expPrevio);
        }

        cargarDetalleExperimentoActivo();
    }

    /**
     * Actualiza el selector de microorganismos segun la muestra seleccionada.
     */
    private void actualizarComboMicroorganismos() {
        if (muestraCtrl == null) {
            return;
        }

        cmbMicroExp.removeAllItems();
        String idMuestra = obtenerIdSeleccionado(cmbMuestraExp);
        Muestra muestra = muestraCtrl.buscarMuestraPorId(idMuestra);

        if (muestra != null) {
            for (Microorganismo micro : muestra.getMicroorganismos()) {
                cmbMicroExp.addItem(
                        micro.getId() + " - " + micro.getNombre()
                        + " (Pob: " + micro.getCantidadOrganismos() + ")"
                );
            }

            // Prellenar condiciones ambientales iniciales si los campos estan vacios
            if (muestra.getCondicionInicial() != null && txtTempExp.getText().trim().isEmpty()) {
                mostrarCondicionEnCampos(muestra.getCondicionInicial());
            }
        }
    }

    /**
     * Muestra en la tabla los ciclos ejecutados y en el area de texto la comparativa
     * inicial vs final del experimento seleccionado.
     */
    private void cargarDetalleExperimentoActivo() {
        if (experimentoCtrl == null || modeloTablaCiclos == null) {
            return;
        }

        modeloTablaCiclos.setRowCount(0);
        String idExp = obtenerIdSeleccionado(cmbExperimentoActivo);
        Experimento exp = experimentoCtrl.buscarExperimentoPorId(idExp);

        if (exp == null) {
            txtComparacionResultados.setText("Seleccione o inicie un experimento para visualizar sus resultados.");
            return;
        }

        for (CicloSimulacion ciclo : exp.getCiclos()) {
            String condTexto = (ciclo.getCondicionAplicada() != null)
                    ? ciclo.getCondicionAplicada().toString()
                    : "-";

            modeloTablaCiclos.addRow(new Object[]{
                ciclo.getNumeroCiclo(),
                ciclo.getCantidadOrganismos(),
                String.format("%.1f%%", ciclo.getNivelActividad()),
                String.format("%.1f%%", ciclo.getCrecimiento()),
                ciclo.getReproduccion(),
                ciclo.getEstadoGeneral(),
                condTexto,
                ciclo.getDescripcionRespuesta()
            });
        }

        txtComparacionResultados.setText(exp.obtenerComparacionInicialFinal());

        if (exp.getCondicionActual() != null) {
            mostrarCondicionEnCampos(exp.getCondicionActual());
        }
    }

    // ==================== LOGICA DE EXPERIMENTOS Y CICLOS ====================

    /**
     * Inicia un nuevo experimento con el microorganismo y condiciones iniciales indicadas.
     */
    private void iniciarNuevoExperimento() {
        if (experimentoCtrl == null) {
            return;
        }

        try {
            String idExp = txtIdExperimento.getText().trim();
            String fecha = txtFechaExperimento.getText().trim();
            String idMuestra = obtenerIdSeleccionado(cmbMuestraExp);
            String idMicro = obtenerIdSeleccionado(cmbMicroExp);
            CondicionAmbiental condicionInicial = leerCondicionAmbiental();

            Experimento nuevo = experimentoCtrl.iniciarExperimento(
                    idExp, fecha, idMuestra, idMicro, condicionInicial
            );

            refrescarDatos();

            // Seleccionar automaticamente el experimento recien creado
            String etiquetaExp = nuevo.getId() + " - " + nuevo.getNombreMicroorganismo()
                    + " (Muestra " + nuevo.getIdMuestra() + ")";
            cmbExperimentoActivo.setSelectedItem(etiquetaExp);

            txtIdExperimento.setText("");
            txtFechaExperimento.setText("");

            JOptionPane.showMessageDialog(
                    this,
                    "Experimento '" + nuevo.getId() + "' iniciado correctamente.",
                    "Experimento iniciado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (DatosInvalidosException | LaboratorioException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "No se pudo iniciar el experimento",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Ejecuta un ciclo de simulacion sobre el experimento activo aplicando las variables
     * ambientales ingresadas en los campos de texto.
     */
    private void ejecutarCicloEnExperimentoActivo() {
        if (experimentoCtrl == null) {
            return;
        }

        try {
            String idExp = obtenerIdSeleccionado(cmbExperimentoActivo);
            CondicionAmbiental nuevaCondicion = leerCondicionAmbiental();

            CicloSimulacion ciclo = experimentoCtrl.ejecutarCicloExperimento(idExp, nuevaCondicion);

            actualizarComboMicroorganismos();
            cargarDetalleExperimentoActivo();

            JOptionPane.showMessageDialog(
                    this,
                    "Ciclo #" + ciclo.getNumeroCiclo() + " ejecutado.\n"
                    + "Estado: " + ciclo.getEstadoGeneral() + " | Población: " + ciclo.getCantidadOrganismos(),
                    "Ciclo completado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (DatosInvalidosException | LaboratorioException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error en ciclo de simulación",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Lee y convierte las variables ambientales ingresadas en el panel.
     */
    private CondicionAmbiental leerCondicionAmbiental() throws DatosInvalidosException {
        try {
            double temp = Double.parseDouble(txtTempExp.getText().trim());
            double ph = Double.parseDouble(txtPhExp.getText().trim());
            double oxigeno = Double.parseDouble(txtOxigenoExp.getText().trim());
            double humedad = Double.parseDouble(txtHumedadExp.getText().trim());
            double salinidad = Double.parseDouble(txtSalinidadExp.getText().trim());
            double nutrientes = Double.parseDouble(txtNutrientesExp.getText().trim());

            return new CondicionAmbiental(temp, ph, oxigeno, humedad, salinidad, nutrientes);

        } catch (NumberFormatException ex) {
            throw new DatosInvalidosException(
                    "Todas las variables ambientales deben ser valores numéricos válidos."
            );
        }
    }

    /**
     * Vuelca los valores de una CondicionAmbiental en los campos de texto del panel.
     */
    private void mostrarCondicionEnCampos(CondicionAmbiental c) {
        txtTempExp.setText(String.valueOf(c.getTemperatura()));
        txtPhExp.setText(String.valueOf(c.getPh()));
        txtOxigenoExp.setText(String.valueOf(c.getOxigeno()));
        txtHumedadExp.setText(String.valueOf(c.getHumedad()));
        txtSalinidadExp.setText(String.valueOf(c.getSalinidad()));
        txtNutrientesExp.setText(String.valueOf(c.getDisponibilidadNutrientes()));
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

        jLabel1 = new javax.swing.JLabel();
        txtIdExperimento = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtFechaExperimento = new javax.swing.JTextField();
        cmbMuestraExp = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cmbMicroExp = new javax.swing.JComboBox<>();
        btnRefrescarListasExp = new javax.swing.JButton();
        btnIniciarExperimento = new javax.swing.JButton();
        cmbExperimentoActivo = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtTempExp = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtHumedadExp = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtPhExp = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtSalinidadExp = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtOxigenoExp = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtNutrientesExp = new javax.swing.JTextField();
        btnEjecutarCiclo = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCiclosSimulacion = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtComparacionResultados = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();

        jLabel1.setText("ID del nuevo experimento");

        jLabel2.setText("Fecha de inicio del experimento");

        txtFechaExperimento.addActionListener(this::txtFechaExperimentoActionPerformed);

        cmbMuestraExp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText("Muestra seleccionada para el experimento:");

        jLabel4.setText("Microorganismo seleccionado para el experimento");

        cmbMicroExp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnRefrescarListasExp.setText("Refrescar");

        btnIniciarExperimento.setText("Iniciar");

        cmbExperimentoActivo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel5.setText("Selecciona el experimento activo para ejecutar ciclos");

        jLabel6.setText("Variable ambiental: Temperatura (°C) (inicial o del ciclo)");

        jLabel7.setText("Variable ambiental: Humedad (%)");

        jLabel8.setText("Variable ambiental: pH (0.0 a 14.0)");

        jLabel9.setText("Variable ambiental: Salinidad (%)");

        jLabel10.setText("Variable ambiental: Oxígeno (%)");

        jLabel11.setText("Variable ambiental: Disponibilidad de nutrientes (%)");

        btnEjecutarCiclo.setText("Ejecutar ciclo");

        tblCiclosSimulacion.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblCiclosSimulacion);

        txtComparacionResultados.setColumns(20);
        txtComparacionResultados.setRows(5);
        jScrollPane2.setViewportView(txtComparacionResultados);

        jLabel12.setText("Ciclo por ciclo");

        jLabel13.setText("comparativa entre condiciones");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtIdExperimento))
                            .addComponent(jLabel2)
                            .addComponent(txtFechaExperimento, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbMicroExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(cmbMuestraExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnRefrescarListasExp)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnIniciarExperimento)
                                    .addComponent(jLabel12)))))
                    .addComponent(cmbExperimentoActivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtTempExp, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(286, 286, 286)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(txtSalinidadExp, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel11)
                            .addComponent(txtNutrientesExp, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtHumedadExp, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel6)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(txtPhExp, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtOxigenoExp, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEjecutarCiclo))
                .addContainerGap(158, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdExperimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbMuestraExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFechaExperimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbMicroExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefrescarListasExp)
                    .addComponent(btnIniciarExperimento))
                .addGap(36, 36, 36)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbExperimentoActivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(74, 74, 74)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTempExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPhExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtOxigenoExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHumedadExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSalinidadExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNutrientesExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(btnEjecutarCiclo)
                        .addGap(45, 45, 45)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(jLabel12)
                        .addGap(2, 2, 2)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtFechaExperimentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFechaExperimentoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFechaExperimentoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEjecutarCiclo;
    private javax.swing.JButton btnIniciarExperimento;
    private javax.swing.JButton btnRefrescarListasExp;
    private javax.swing.JComboBox<String> cmbExperimentoActivo;
    private javax.swing.JComboBox<String> cmbMicroExp;
    private javax.swing.JComboBox<String> cmbMuestraExp;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JTable tblCiclosSimulacion;
    private javax.swing.JTextArea txtComparacionResultados;
    private javax.swing.JTextField txtFechaExperimento;
    private javax.swing.JTextField txtHumedadExp;
    private javax.swing.JTextField txtIdExperimento;
    private javax.swing.JTextField txtNutrientesExp;
    private javax.swing.JTextField txtOxigenoExp;
    private javax.swing.JTextField txtPhExp;
    private javax.swing.JTextField txtSalinidadExp;
    private javax.swing.JTextField txtTempExp;
    // End of variables declaration//GEN-END:variables
}
