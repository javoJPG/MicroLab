package Frontend;

import Backend.Bacteria;
import Backend.CondicionAmbiental;
import Backend.Extremofilo;
import Backend.HongoMicroscopico;
import Backend.Microorganismo;
import Backend.Muestra;
import Backend.Protozoo;
import Backend.exceptions.DatosInvalidosException;
import Backend.exceptions.LaboratorioException;
import Controller.MuestraController;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Panel encargado del registro polimorfico y visualizacion de los microorganismos
 * (Bacteria, Protozoo, Hongo Microscopico y Extremofilo) asociados a cada muestra.
 *
 * @author javo
 */
public class PanelMicroorganismos extends javax.swing.JPanel {

    // Controlador principal de muestras y microorganismos
    private MuestraController muestraCtrl;

    // Modelo de datos para la tabla de microorganismos
    private DefaultTableModel modeloTabla;

    /**
     * Constructor por defecto requerido por el diseñador de NetBeans.
     */
    public PanelMicroorganismos() {
        this(null);
    }

    /**
     * Constructor que conecta el panel con el controlador de muestras.
     *
     * @param muestraCtrl Instancia compartida de MuestraController.
     */
    public PanelMicroorganismos(MuestraController muestraCtrl) {
        this.muestraCtrl = muestraCtrl;
        initComponents();
        configurarTablaYEventos();
        refrescarDatos();
    }

    // ==================== CONFIGURACION Y EVENTOS ====================

    /**
     * Configura las columnas de la tabla y enlaza los eventos de botones y selectores.
     */
    private void configurarTablaYEventos() {
        modeloTabla = new DefaultTableModel(
                new Object[]{
                    "ID", "Muestra", "Nombre", "Tipo", "Estado",
                    "Población", "Actividad (%)", "Reproducción", "Alimentación", "Locomoción"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblMicroorganismos.setModel(modeloTabla);

        // Eventos de botones y selectores
        btnRefrescarMuestrasMicro.addActionListener(e -> refrescarDatos());
        btnRegistrarMicro.addActionListener(e -> registrarMicroorganismo());
        cmbTipoMicro.addActionListener(e -> actualizarEtiquetaParametroEspecial());
        cmbMuestraAsociada.addActionListener(e -> prellenarCondicionesDesdeMuestra());

        actualizarEtiquetaParametroEspecial();
    }

    /**
     * Actualiza la etiqueta descriptiva del parametro especifico segun la subclase seleccionada.
     */
    private void actualizarEtiquetaParametroEspecial() {
        String tipo = (String) cmbTipoMicro.getSelectedItem();
        if (tipo == null) {
            return;
        }

        if (Microorganismo.TIPO_BACTERIA.equalsIgnoreCase(tipo)) {
            lblParametroEspecial.setText("Tasa de Fisión Binaria (ej. 1.2):");
        } else if (Microorganismo.TIPO_PROTOZOO.equalsIgnoreCase(tipo)) {
            lblParametroEspecial.setText("Sensibilidad O2 / Humedad (ej. 1.0):");
        } else if (Microorganismo.TIPO_HONGO.equalsIgnoreCase(tipo)) {
            lblParametroEspecial.setText("Factor de Esporulación (entero, ej. 3):");
        } else if (Microorganismo.TIPO_EXTREMOFILO.equalsIgnoreCase(tipo)) {
            lblParametroEspecial.setText("Índice Resistencia Extrema (ej. 1.5):");
        }
    }

    /**
     * Si el usuario selecciona una muestra y los campos de condicion ambiental estan vacios,
     * prellena dichos campos con la condicion inicial de la muestra para agilizar el registro.
     */
    private void prellenarCondicionesDesdeMuestra() {
        if (muestraCtrl == null) {
            return;
        }

        String idMuestra = obtenerIdSeleccionado(cmbMuestraAsociada);
        Muestra m = muestraCtrl.buscarMuestraPorId(idMuestra);

        if (m != null && m.getCondicionInicial() != null && txtTempMicro.getText().trim().isEmpty()) {
            CondicionAmbiental c = m.getCondicionInicial();
            txtTempMicro.setText(String.valueOf(c.getTemperatura()));
            txtPhMicro.setText(String.valueOf(c.getPh()));
            txtOxigenoMicro.setText(String.valueOf(c.getOxigeno()));
            txtHumedadMicro.setText(String.valueOf(c.getHumedad()));
            txtSalinidadMicro.setText(String.valueOf(c.getSalinidad()));
            txtNutrientesMicro.setText(String.valueOf(c.getDisponibilidadNutrientes()));
        }
    }

    /**
     * Recarga el selector de muestras y la tabla de microorganismos registrados.
     */
    public final void refrescarDatos() {
        if (muestraCtrl == null || modeloTabla == null) {
            return;
        }

        String seleccionPrevia = (String) cmbMuestraAsociada.getSelectedItem();
        cmbMuestraAsociada.removeAllItems();

        for (Muestra m : muestraCtrl.getMuestras()) {
            cmbMuestraAsociada.addItem(m.getId() + " - " + m.getOrigen());
        }

        if (seleccionPrevia != null) {
            cmbMuestraAsociada.setSelectedItem(seleccionPrevia);
        }

        // Actualizar filas de la tabla con todos los microorganismos
        modeloTabla.setRowCount(0);
        for (Microorganismo micro : muestraCtrl.obtenerTodosLosMicroorganismos()) {
            modeloTabla.addRow(new Object[]{
                micro.getId(),
                micro.getIdMuestra(),
                micro.getNombre(),
                micro.getTipo(),
                micro.getEstado(),
                micro.getCantidadOrganismos(),
                String.format("%.1f%%", micro.getNivelActividad()),
                micro.getCapacidadReproductiva(),
                micro.getAlimentacion(),
                micro.getLocomocion()
            });
        }
    }

    // ==================== LOGICA DE REGISTRO ====================

    /**
     * Crea la instancia polimorfica del microorganismo segun su tipo y la asocia a la muestra.
     */
    private void registrarMicroorganismo() {
        if (muestraCtrl == null) {
            return;
        }

        try {
            String idMuestra = obtenerIdSeleccionado(cmbMuestraAsociada);
            if (idMuestra.isEmpty()) {
                throw new LaboratorioException(
                        "Debe registrar y seleccionar una muestra antes de agregar un microorganismo."
                );
            }

            String id = txtIdMicro.getText().trim();
            String nombre = txtNombreMicro.getText().trim();
            String tipo = (String) cmbTipoMicro.getSelectedItem();
            String estado = (String) cmbEstadoMicro.getSelectedItem();
            String reproduccion = txtReproduccionMicro.getText().trim();
            String alimentacion = txtAlimentacionMicro.getText().trim();
            String locomocion = txtLocomocionMicro.getText().trim();

            int cantidadInicial;
            try {
                cantidadInicial = Integer.parseInt(txtPoblacionInicialMicro.getText().trim());
            } catch (NumberFormatException ex) {
                throw new DatosInvalidosException(
                        "La cantidad inicial de organismos debe ser un numero entero valido."
                );
            }

            CondicionAmbiental condicion = leerCondicionCrecimiento();
            Microorganismo nuevoMicro = construirSegunTipo(
                    id, idMuestra, nombre, tipo, estado, condicion,
                    reproduccion, alimentacion, locomocion, cantidadInicial
            );

            muestraCtrl.registrarMicroorganismoEnMuestra(idMuestra, nuevoMicro);
            refrescarDatos();
            limpiarCampos();

            JOptionPane.showMessageDialog(
                    this,
                    "Microorganismo registrado exitosamente en la muestra " + idMuestra + ".",
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (DatosInvalidosException | LaboratorioException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error al registrar microorganismo",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Instancia la subclase correspondiente de Microorganismo validando su parametro especial.
     */
    private Microorganismo construirSegunTipo(
            String id,
            String idMuestra,
            String nombre,
            String tipo,
            String estado,
            CondicionAmbiental condicion,
            String reproduccion,
            String alimentacion,
            String locomocion,
            int cantidadInicial
    ) throws DatosInvalidosException {

        String textoParam = txtParametroEspecialMicro.getText().trim();

        try {
            if (Microorganismo.TIPO_BACTERIA.equalsIgnoreCase(tipo)) {
                double tasa = Double.parseDouble(textoParam);
                if (tasa <= 0) {
                    throw new DatosInvalidosException("La tasa de fision binaria debe ser mayor a cero.");
                }
                return new Bacteria(
                        id, idMuestra, nombre, estado, condicion,
                        reproduccion, alimentacion, locomocion, cantidadInicial, tasa
                );

            } else if (Microorganismo.TIPO_PROTOZOO.equalsIgnoreCase(tipo)) {
                double sensibilidad = Double.parseDouble(textoParam);
                if (sensibilidad <= 0) {
                    throw new DatosInvalidosException("La sensibilidad debe ser mayor a cero.");
                }
                return new Protozoo(
                        id, idMuestra, nombre, estado, condicion,
                        reproduccion, alimentacion, locomocion, cantidadInicial, sensibilidad
                );

            } else if (Microorganismo.TIPO_HONGO.equalsIgnoreCase(tipo)) {
                int factorEsporulacion = Integer.parseInt(textoParam);
                if (factorEsporulacion <= 0) {
                    throw new DatosInvalidosException("El factor de esporulacion debe ser un entero mayor a cero.");
                }
                return new HongoMicroscopico(
                        id, idMuestra, nombre, estado, condicion,
                        reproduccion, alimentacion, locomocion, cantidadInicial, factorEsporulacion
                );

            } else {
                double indiceResistencia = Double.parseDouble(textoParam);
                if (indiceResistencia <= 0) {
                    throw new DatosInvalidosException("El indice de resistencia extrema debe ser mayor a cero.");
                }
                return new Extremofilo(
                        id, idMuestra, nombre, estado, condicion,
                        reproduccion, alimentacion, locomocion, cantidadInicial, indiceResistencia
                );
            }
        } catch (NumberFormatException ex) {
            throw new DatosInvalidosException(
                    "El valor numerico de la caracteristica propia del grupo no tiene un formato valido."
            );
        }
    }

    /**
     * Lee y valida el formato numerico de las condiciones de crecimiento del microorganismo.
     */
    private CondicionAmbiental leerCondicionCrecimiento() throws DatosInvalidosException {
        try {
            double temp = Double.parseDouble(txtTempMicro.getText().trim());
            double ph = Double.parseDouble(txtPhMicro.getText().trim());
            double oxigeno = Double.parseDouble(txtOxigenoMicro.getText().trim());
            double humedad = Double.parseDouble(txtHumedadMicro.getText().trim());
            double salinidad = Double.parseDouble(txtSalinidadMicro.getText().trim());
            double nutrientes = Double.parseDouble(txtNutrientesMicro.getText().trim());

            return new CondicionAmbiental(temp, ph, oxigeno, humedad, salinidad, nutrientes);

        } catch (NumberFormatException ex) {
            throw new DatosInvalidosException(
                    "Todas las condiciones ambientales de crecimiento deben ser valores numericos."
            );
        }
    }

    /**
     * Extrae el ID antes del guion ("ID - Descripcion") de un JComboBox.
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
     * Limpia todos los campos de texto del formulario.
     */
    private void limpiarCampos() {
        txtIdMicro.setText("");
        txtNombreMicro.setText("");
        txtPoblacionInicialMicro.setText("");
        txtReproduccionMicro.setText("");
        txtAlimentacionMicro.setText("");
        txtLocomocionMicro.setText("");
        txtParametroEspecialMicro.setText("");
        txtTempMicro.setText("");
        txtPhMicro.setText("");
        txtOxigenoMicro.setText("");
        txtHumedadMicro.setText("");
        txtSalinidadMicro.setText("");
        txtNutrientesMicro.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbMuestraAsociada = new javax.swing.JComboBox<>();
        btnRefrescarMuestrasMicro = new javax.swing.JButton();
        txtIdMicro = new javax.swing.JTextField();
        txtNombreMicro = new javax.swing.JTextField();
        cmbTipoMicro = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cmbEstadoMicro = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtPoblacionInicialMicro = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtReproduccionMicro = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtAlimentacionMicro = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtLocomocionMicro = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtParametroEspecialMicro = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtTempMicro = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtPhMicro = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtOxigenoMicro = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtHumedadMicro = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtSalinidadMicro = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtNutrientesMicro = new javax.swing.JTextField();
        btnRegistrarMicro = new javax.swing.JButton();
        btnLimpiarMicro = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblMicroorganismos = new javax.swing.JTable();
        lblParametroEspecial = new javax.swing.JLabel();

        cmbMuestraAsociada.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnRefrescarMuestrasMicro.setText("Cargar Muestra");

        cmbTipoMicro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Bacteria", "Protozoo", "Hongo Microscopico", "Extremofilo" }));

        jLabel1.setText("Identificador del microorganismo");

        jLabel2.setText("Nombre del microorganismo");

        cmbEstadoMicro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Activo", "En desarrollo optimo", "Actividad reducida", "Desarrollo detenido" }));

        jLabel3.setText("Tipo microorganismo");

        jLabel4.setText("Estado microorganismo");

        jLabel5.setText("Cantidad inicial de organismos");

        jLabel6.setText("Capacidad reproductiva");

        jLabel7.setText("Tipo de alimentación");

        jLabel8.setText("Tipo de locomoción");

        jLabel9.setText("Valor numérico de la característica propia del grupo");

        jLabel10.setText("Condición de crecimiento: Temperatura (°C)");

        jLabel11.setText("Condición de crecimiento: pH");

        jLabel12.setText("Condición de crecimiento: Oxígeno (%)");

        jLabel13.setText("Condición de crecimiento: Humedad (%)");

        jLabel14.setText("Condición de crecimiento: Salinidad (%)");

        jLabel15.setText("Condición de crecimiento: Nutrientes (%)");

        btnRegistrarMicro.setText("Registrar microorganismo");

        btnLimpiarMicro.setText("Limpiar campos");
        btnLimpiarMicro.addActionListener(this::btnLimpiarMicroActionPerformed);

        tblMicroorganismos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblMicroorganismos);

        lblParametroEspecial.setText("Tasa Fisión / Factor Especial:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtIdMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(cmbMuestraAsociada, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(txtNombreMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(txtPoblacionInicialMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnRegistrarMicro)
                                    .addGap(30, 30, 30)
                                    .addComponent(btnLimpiarMicro))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnRefrescarMuestrasMicro)
                                    .addGap(179, 179, 179)))
                            .addComponent(jLabel15)
                            .addComponent(jLabel9)
                            .addComponent(txtParametroEspecialMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(txtLocomocionMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(txtAlimentacionMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(txtReproduccionMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNutrientesMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblParametroEspecial))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(txtTempMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(txtPhMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)
                            .addComponent(txtOxigenoMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)
                            .addComponent(txtHumedadMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel3)
                                .addComponent(cmbTipoMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbEstadoMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel4))
                            .addGap(96, 96, 96)))
                    .addComponent(txtSalinidadMicro, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(301, 301, 301))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(cmbMuestraAsociada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRefrescarMuestrasMicro)
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbTipoMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbEstadoMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTempMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPhMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtOxigenoMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHumedadMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSalinidadMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIdMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNombreMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPoblacionInicialMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtReproduccionMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAlimentacionMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtLocomocionMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblParametroEspecial)
                        .addGap(29, 29, 29)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtParametroEspecialMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNutrientesMicro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRegistrarMicro)
                            .addComponent(btnLimpiarMicro))))
                .addContainerGap(52, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimpiarMicroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarMicroActionPerformed
        limpiarCampos();
    }//GEN-LAST:event_btnLimpiarMicroActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLimpiarMicro;
    private javax.swing.JButton btnRefrescarMuestrasMicro;
    private javax.swing.JButton btnRegistrarMicro;
    private javax.swing.JComboBox<String> cmbEstadoMicro;
    private javax.swing.JComboBox<String> cmbMuestraAsociada;
    private javax.swing.JComboBox<String> cmbTipoMicro;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblParametroEspecial;
    private javax.swing.JTable tblMicroorganismos;
    private javax.swing.JTextField txtAlimentacionMicro;
    private javax.swing.JTextField txtHumedadMicro;
    private javax.swing.JTextField txtIdMicro;
    private javax.swing.JTextField txtLocomocionMicro;
    private javax.swing.JTextField txtNombreMicro;
    private javax.swing.JTextField txtNutrientesMicro;
    private javax.swing.JTextField txtOxigenoMicro;
    private javax.swing.JTextField txtParametroEspecialMicro;
    private javax.swing.JTextField txtPhMicro;
    private javax.swing.JTextField txtPoblacionInicialMicro;
    private javax.swing.JTextField txtReproduccionMicro;
    private javax.swing.JTextField txtSalinidadMicro;
    private javax.swing.JTextField txtTempMicro;
    // End of variables declaration//GEN-END:variables
}
