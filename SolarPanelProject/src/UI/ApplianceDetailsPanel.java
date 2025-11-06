package UI;

import Model.Appliance;
import Model.CountryConfig;
import Model.SolarCalculator;
import Utils.ProjectManager;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;

/**
 * ApplianceDetailsPanel - A comprehensive UI component for managing appliance details,
 * solar parameters, and system calculations.
 *
 * Structure:
 * 1. Constants & Fields
 * 2. Constructor & Initialization
 * 3. UI Creation Methods
 * 4. Data Management Methods
 * 5. Calculation & Analysis Methods
 * 6. Export Methods
 * 7. Helper & Utility Methods
 */
public class ApplianceDetailsPanel extends JPanel {

    // ============================================================================
    // SECTION 1: CONSTANTS & FIELDS
    // ============================================================================

    private static final long serialVersionUID = 1L;

    // Color Palette
    private final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private final Color CARD_BACKGROUND = Color.WHITE;
    private final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    // Analysis Colors
    private final Color CARD_HEADER_BG = new Color(59, 130, 246, 15);
    private final Color ANALYSIS_RED_BG = new Color(220, 53, 69);
    private final Color ANALYSIS_YELLOW_BG = new Color(255, 193, 7);
    private final Color ANALYSIS_GREEN_BG = new Color(40, 167, 69);
    private final Color TEXT_WHITE = Color.WHITE;

    // Core Components
    private Appliance appliance;
    private MainPage mainPage;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    // Form Fields
    private JTextField nameField, wattsField, qtyField, hoursField;
    private JTextField pshField, dodField, daysField;
    private JComboBox<String> voltageCombo;
    private JTabbedPane tabbedPane;

    // Results Display
    private JPanel resultsCard;
    private JPanel resultsContentPanel;

    // State Management
    private boolean hasUnsavedChanges = false;
    private String originalBasicData = "";
    private String originalSolarData = "";

    // ============================================================================
    // SECTION 2: CONSTRUCTOR & INITIALIZATION
    // ============================================================================

    public ApplianceDetailsPanel(Appliance appliance, MainPage mainPage) {
        this.appliance = appliance;
        this.mainPage = mainPage;
        initializeUI();
        storeOriginalData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Toolbar
        add(createToolbar(), BorderLayout.NORTH);

        // Tabbed Content
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("📝 Basic Details", createBasicDetailsPanel());
        tabbedPane.addTab("☀️ Solar Parameters", createSolarParametersPanel());
        tabbedPane.addTab("📊 Results", createResultsPanel());
        tabbedPane.addChangeListener(e -> {
            if (hasUnsavedChanges) {
                promptSaveChanges();
            }
        });
        add(tabbedPane, BorderLayout.CENTER);

        // Navigation Buttons
        add(createNavigationPanel(), BorderLayout.SOUTH);

        // Load data and setup listeners
        loadApplianceData();
        addChangeListeners();
    }

    // ============================================================================
    // SECTION 3: UI CREATION METHODS
    // ============================================================================

    // ---------- Main Layout Components ----------

    private JLabel createHeader() {
        JLabel header = new JLabel("⚙️ Appliance Details & Solar Parameters", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        return header;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(new Color(245, 245, 245));

        JButton saveBtn = new JButton("💾 Save Project");
        saveBtn.addActionListener(e -> saveProjectToFile());

        JButton countryBtn = new JButton("🌏 Change Country/Region");
        countryBtn.addActionListener(e -> showCountrySelectionDialog());

        toolbar.add(saveBtn);
        toolbar.add(countryBtn);
        return toolbar;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton saveBtn = createStyledButton("💾 Save Changes", SUCCESS_COLOR, new Color(22, 163, 74));
        JButton calculateBtn = createStyledButton("🔢 Calculate This Appliance", PRIMARY_COLOR, new Color(37, 99, 235));
        JButton backBtn = createStyledButton("← Back to List", new Color(100, 116, 139), new Color(71, 85, 105));

        saveBtn.addActionListener(e -> saveChanges());
        calculateBtn.addActionListener(e -> calculateThisAppliance());
        backBtn.addActionListener(e -> handleBackNavigation());

        navPanel.add(saveBtn);
        navPanel.add(calculateBtn);
        navPanel.add(backBtn);

        return navPanel;
    }

    // ---------- Tab Panels ----------

    private JPanel createBasicDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel card = createFormCard();
        card.add(createFormField("🔌 Appliance Name", nameField = new JTextField(), "e.g., LED Light"));
        card.add(Box.createVerticalStrut(20));
        card.add(createFormField("⚡ Power (Watts)", wattsField = new JTextField(), "e.g., 10"));
        card.add(Box.createVerticalStrut(20));
        card.add(createFormField("🔢 Quantity", qtyField = new JTextField(), "e.g., 2"));
        card.add(Box.createVerticalStrut(20));
        card.add(createFormField("⏰ Hours per Day", hoursField = new JTextField(), "e.g., 5.0"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(card, gbc);

        return panel;
    }

    private JPanel createSolarParametersPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel card = createFormCard();
        card.add(createFormField("☀️ Peak Sun Hours", pshField = new JTextField(), "e.g., 5.0"));
        card.add(Box.createVerticalStrut(20));
        card.add(createFormField("🔋 Depth of Discharge (%)", dodField = new JTextField(), "e.g., 50"));
        card.add(Box.createVerticalStrut(20));
        card.add(createFormField("📅 Days of Autonomy", daysField = new JTextField(), "e.g., 2"));
        card.add(Box.createVerticalStrut(20));
        card.add(createFormField("⚡ System Voltage", voltageCombo = new JComboBox<>(new String[]{"12", "24", "48"}), null));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(card, gbc);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Export buttons
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportPanel.setBackground(BACKGROUND_COLOR);
        exportPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JButton exportPdfBtn = createStyledButton("📄 Export to PDF", new Color(220, 53, 69), new Color(200, 35, 51));
        exportPdfBtn.setPreferredSize(new Dimension(150, 35));
        exportPdfBtn.addActionListener(e -> exportResultsToPDF());

        JButton exportCsvBtn = createStyledButton("📄 Export to CSV", new Color(0, 102, 204), new Color(0, 76, 153));
        exportCsvBtn.setPreferredSize(new Dimension(150, 35));
        exportCsvBtn.addActionListener(e -> handleCSVExport());

        exportPanel.add(exportPdfBtn);
        exportPanel.add(exportCsvBtn);

        // Results content
        resultsCard = new JPanel();
        resultsCard.setLayout(new BoxLayout(resultsCard, BoxLayout.Y_AXIS));
        resultsCard.setBackground(CARD_BACKGROUND);
        resultsCard.setBorder(BorderFactory.createCompoundBorder(
                createCardBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JScrollPane scrollPane = new JScrollPane(resultsCard);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        resultsContentPanel = new JPanel(new BorderLayout());
        resultsContentPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(exportPanel, BorderLayout.NORTH);
        panel.add(resultsContentPanel, BorderLayout.CENTER);

        displayWelcomeMessage();

        return panel;
    }

    // ---------- Form Components ----------

    private JPanel createFormCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        card.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        return card;
    }

    private JPanel createFormField(String labelText, JComponent inputComponent, String placeholder) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 8));
        fieldPanel.setOpaque(false);
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(51, 65, 85));

        configureInputComponent(inputComponent, placeholder);

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(inputComponent, BorderLayout.CENTER);

        return fieldPanel;
    }

    private void configureInputComponent(JComponent component, String placeholder) {
        if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            textField.setPreferredSize(new Dimension(0, 38));

            if (placeholder != null) {
                setupPlaceholder(textField, placeholder);
            }
        } else if (component instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>) component;
            comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            comboBox.setPreferredSize(new Dimension(0, 38));
            comboBox.setBackground(Color.WHITE);
            ((JLabel) comboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        }
    }

    private void setupPlaceholder(JTextField textField, String placeholder) {
        textField.setForeground(Color.GRAY);
        textField.setText(placeholder);
        textField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent evt) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(180, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        );
    }

    // ============================================================================
    // SECTION 4: DATA MANAGEMENT METHODS
    // ============================================================================

    private void loadApplianceData() {
        // Basic details
        nameField.setText(appliance.getName());
        wattsField.setText(String.valueOf(appliance.getWatts()));
        qtyField.setText(String.valueOf(appliance.getQuantity()));
        hoursField.setText(String.valueOf(appliance.getHoursPerDay()));

        // Solar parameters with placeholder handling
        loadFieldWithPlaceholder(pshField, appliance.getPeakSunHours(), 5.0, "e.g., 5.0");
        loadFieldWithPlaceholder(dodField, appliance.getDepthOfDischarge(), 50.0, "e.g., 50");
        loadFieldWithPlaceholder(daysField, appliance.getDaysOfAutonomy(), 2, "e.g., 2");

        if (appliance.getSystemVoltage() != 12) {
            voltageCombo.setSelectedItem(String.valueOf(appliance.getSystemVoltage()));
        }
    }

    private void loadFieldWithPlaceholder(JTextField field, double value, double defaultValue, String placeholder) {
        if (value != defaultValue) {
            field.setText(String.valueOf(value));
        } else {
            field.setText(placeholder);
            field.setForeground(Color.GRAY);
        }
    }

    private boolean saveChanges() {
        try {
            // Validate basic fields
            String name = getFieldValue(nameField);
            String wattsText = getFieldValue(wattsField);
            String qtyText = getFieldValue(qtyField);
            String hoursText = getFieldValue(hoursField);

            if (name.isEmpty() || wattsText.isEmpty() || qtyText.isEmpty() || hoursText.isEmpty()) {
                showWarning("Please fill in all basic appliance details.");
                return false;
            }

            // Save basic details
            appliance.setName(name);
            appliance.setWatts(Double.parseDouble(wattsText));
            appliance.setQuantity(Integer.parseInt(qtyText));
            appliance.setHoursPerDay(Double.parseDouble(hoursText));

            // Save solar parameters
            saveSolarParameters();

            // Update state
            storeOriginalData();
            hasUnsavedChanges = false;
            updateTabTitles();

            showSuccess("✓ Appliance details saved successfully!");
            return true;

        } catch (NumberFormatException ex) {
            showError("Please enter valid numeric values in all fields.");
            return false;
        } catch (Exception ex) {
            showError("Error saving changes: " + ex.getMessage());
            return false;
        }
    }

    private void saveSolarParameters() {
        String pshText = getFieldValue(pshField);
        String dodText = getFieldValue(dodField);
        String daysText = getFieldValue(daysField);

        if (!pshText.isEmpty()) {
            appliance.setPeakSunHours(Double.parseDouble(pshText));
        }
        if (!dodText.isEmpty()) {
            appliance.setDepthOfDischarge(Double.parseDouble(dodText));
        }
        if (!daysText.isEmpty()) {
            appliance.setDaysOfAutonomy(Integer.parseInt(daysText));
        }
        appliance.setSystemVoltage(Integer.parseInt((String) voltageCombo.getSelectedItem()));
    }

    private void saveProjectToFile() {
        // Check if there are unsaved changes in the form
        if (hasUnsavedChanges) {
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes to this appliance.\nSave changes before saving project?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                // Save changes first
                if (!saveChanges()) {
                    return; // If save failed, don't proceed with project save
                }
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return; // User cancelled
            }
            // If NO, continue with current values
        }

        // Sync current appliance's solar parameters to MainPage
        mainPage.setPeakSunHours(appliance.getPeakSunHours());
        mainPage.setDepthOfDischarge(appliance.getDepthOfDischarge());
        mainPage.setDaysOfAutonomy(appliance.getDaysOfAutonomy());
        mainPage.setSystemVoltage(appliance.getSystemVoltage());

        // Get solar parameters from MainPage
        double psh = mainPage.getPeakSunHours();
        double dod = mainPage.getDepthOfDischarge();
        int days = mainPage.getDaysOfAutonomy();
        int voltage = mainPage.getSystemVoltage();

        // Call ProjectManager with correct parameters
        Utils.ProjectManager.saveProject(this, mainPage.getAppliances(), psh, dod, days, voltage);
    }



    // ---------- Change Tracking ----------

    private void addChangeListeners() {
        DocumentListener changeListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { checkForChanges(); }
            public void removeUpdate(DocumentEvent e) { checkForChanges(); }
            public void insertUpdate(DocumentEvent e) { checkForChanges(); }
        };

        nameField.getDocument().addDocumentListener(changeListener);
        wattsField.getDocument().addDocumentListener(changeListener);
        qtyField.getDocument().addDocumentListener(changeListener);
        hoursField.getDocument().addDocumentListener(changeListener);
        pshField.getDocument().addDocumentListener(changeListener);
        dodField.getDocument().addDocumentListener(changeListener);
        daysField.getDocument().addDocumentListener(changeListener);

        voltageCombo.addActionListener(e -> checkForChanges());
    }

    private void storeOriginalData() {
        if (nameField != null && voltageCombo != null) {
            originalBasicData = nameField.getText() + wattsField.getText() +
                    qtyField.getText() + hoursField.getText();
            originalSolarData = pshField.getText() + dodField.getText() +
                    daysField.getText() + voltageCombo.getSelectedItem();
        }
    }

    private void checkForChanges() {
        if (nameField == null || voltageCombo == null) return;

        String currentBasicData = nameField.getText() + wattsField.getText() +
                qtyField.getText() + hoursField.getText();
        String currentSolarData = pshField.getText() + dodField.getText() +
                daysField.getText() + voltageCombo.getSelectedItem();

        hasUnsavedChanges = !currentBasicData.equals(originalBasicData) ||
                !currentSolarData.equals(originalSolarData);

        updateTabTitles();
    }

    private void updateTabTitles() {
        if (tabbedPane == null) return;

        String indicator = hasUnsavedChanges ? " ●" : "";
        tabbedPane.setTitleAt(0, "📝 Basic Details" + indicator);
        tabbedPane.setTitleAt(1, "☀️ Solar Parameters" + indicator);
    }

    private void promptSaveChanges() {
        int result = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Would you like to save them now?",
                "Unsaved Changes Detected",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            if (!saveChanges()) {
                tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() == 0 ? 0 : 1);
            }
        } else {
            hasUnsavedChanges = false;
            updateTabTitles();
        }
    }

    // ============================================================================
    // SECTION 5: CALCULATION & ANALYSIS METHODS
    // ============================================================================

    private void calculateThisAppliance() {
        try {
            // Auto-save if needed
            if (hasUnsavedChanges && !promptAndSave()) {
                return;
            }

            // Validate solar parameters
            if (!validateSolarParameters()) {
                return;
            }

            // Perform calculations
            SolarCalculator calc = mainPage.getCalculator();
            double totalWh = appliance.energyPerDayWh();
            double pvWatts = calc.requiredPvWatts(totalWh, appliance.getPeakSunHours());
            double batteryAh = calc.requiredBatteryAh(totalWh, appliance.getDaysOfAutonomy(),
                    appliance.getDepthOfDischarge(), appliance.getSystemVoltage());
            double inverterW = calc.recommendedInverterW(appliance.getWatts() * appliance.getQuantity());
            double controllerA = calc.recommendedControllerA(pvWatts, appliance.getSystemVoltage());

            // Generate analysis
            String analysis = generateSystemAnalysis(totalWh, pvWatts, batteryAh, inverterW, controllerA);

            // Format and display results
            String resultsText = formatCalculationResults(totalWh, pvWatts, batteryAh,
                    inverterW, controllerA, analysis);
            updateResultsDisplay(resultsText);
            tabbedPane.setSelectedIndex(2); // Switch to Results tab

        } catch (Exception ex) {
            showError("Error calculating: " + ex.getMessage());
        }
    }

    private boolean promptAndSave() {
        int result = JOptionPane.showConfirmDialog(this,
                "Save changes before calculating?",
                "Unsaved Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        return result != JOptionPane.YES_OPTION || saveChanges();
    }

    private boolean validateSolarParameters() {
        String pshText = getFieldValue(pshField);
        String dodText = getFieldValue(dodField);
        String daysText = getFieldValue(daysField);

        if (pshText.isEmpty() || dodText.isEmpty() || daysText.isEmpty()) {
            showWarning("Please set all solar parameters in the 'Solar Parameters' tab before calculating.");
            return false;
        }
        return true;
    }

    private String formatCalculationResults(double totalWh, double pvWatts, double batteryAh,
                                            double inverterW, double controllerA, String analysis) {
        return String.format(
                "Total Daily Energy: %.2f\n" +
                        "Required PV Array: %.2f\n" +
                        "Battery Capacity: %.2f\n" +
                        "Inverter Size: %.2f\n" +
                        "Charge Controller: %.2f\n" +
                        "System Voltage: %d\n" +
                        "Peak Sun Hours: %.1f\n" +
                        "Depth of Discharge: %.1f\n" +
                        "Days of Autonomy: %d\n" +
                        "=== SYSTEM ANALYSIS ===\n%s",
                totalWh, pvWatts, batteryAh, inverterW, controllerA,
                appliance.getSystemVoltage(), appliance.getPeakSunHours(),
                appliance.getDepthOfDischarge(), appliance.getDaysOfAutonomy(),
                analysis
        );
    }

    // ---------- Analysis Generation ----------

    private String generateSystemAnalysis(double totalWh, double pvWatts, double batteryAh,
                                          double inverterW, double controllerA) {
        StringBuilder analysis = new StringBuilder();
        int voltage = appliance.getSystemVoltage();
        double psh = appliance.getPeakSunHours();
        double dod = appliance.getDepthOfDischarge();
        int days = appliance.getDaysOfAutonomy();
        CountryConfig country = appliance.getCountry();

        // Helper for color formatting
        ColorFormatter format = new ColorFormatter();

        // Energy Demand Analysis
        appendEnergyDemandAnalysis(analysis, totalWh, country, format);

        // Solar Panel Analysis
        appendSolarPanelAnalysis(analysis, pvWatts, totalWh, psh, format);

        // Battery Storage Analysis
        appendBatteryAnalysis(analysis, batteryAh, voltage, dod, days, format);

        // Inverter Analysis
        appendInverterAnalysis(analysis, inverterW, appliance.getWatts() * appliance.getQuantity(), format);

        // Charge Controller Analysis
        appendChargeControllerAnalysis(analysis, controllerA, pvWatts, voltage, format);

        // Voltage Optimization
        appendVoltageAnalysis(analysis, voltage, totalWh, format);

        // System Summary
        analysis.append(generateSystemSummaryAnalysis(totalWh, pvWatts, batteryAh, inverterW, controllerA));

        return analysis.toString();
    }

    private void appendEnergyDemandAnalysis(StringBuilder analysis, double totalWh,
                                            CountryConfig country, ColorFormatter format) {
        analysis.append("--- 🔋 ENERGY DEMAND PROFILE ---\n");
        analysis.append(String.format("• **Daily Consumption:** %.0f Wh/day for %s\n",
                totalWh, appliance.getName()));

        double monthlyKwh = (totalWh * 30) / 1000;
        double monthlyBill = monthlyKwh * country.getElectricityRatePerKwh();
        String currencySymbol = country.getCurrencySymbol();

        analysis.append(String.format("• **Monthly Equivalent:** ~%.1f kWh (comparable to %s%.0f bill at %s%.2f/kWh)\n",
                monthlyKwh, currencySymbol, monthlyBill,
                currencySymbol, country.getElectricityRatePerKwh()));

        // ... rest of the method remains the same ...
    }

    private void appendSolarPanelAnalysis(StringBuilder analysis, double pvWatts,
                                          double totalWh, double psh, ColorFormatter format) {
        analysis.append("\n--- ☀️ PHOTOVOLTAIC ARRAY DESIGN ---\n");
        analysis.append(String.format("• **Required Capacity:** %.0f W peak power\n", pvWatts));

        // Panel configurations
        int panels300w = (int) Math.ceil(pvWatts / 300.0);
        int panels400w = (int) Math.ceil(pvWatts / 400.0);
        double roofArea = pvWatts / 150;

        analysis.append("• **Configuration Options:**\n");
        analysis.append(String.format("  - %d× 300W panels (~%.1f m²) OR\n", panels300w, roofArea));
        analysis.append(String.format("  - %d× 400W panels (~%.1f m²)\n", panels400w, roofArea * 0.75));

        // Daily production
        double dailyProduction = pvWatts * psh * 0.85;
        double productionRatio = dailyProduction / totalWh;

        analysis.append(String.format("• **Daily Production:** ~%.0f Wh with %.1f peak sun hours\n",
                dailyProduction, psh));

        if (productionRatio >= 1.5) {
            analysis.append(format.tag("• **Production Status:** EXCELLENT - 50%+ surplus! Great for winter/cloudy days.", "GREEN") + "\n");
        } else if (productionRatio >= 1.2) {
            analysis.append(format.tag("• **Production Status:** OPTIMAL - 20% safety margin for seasonal variation.", "GREEN") + "\n");
        } else if (productionRatio >= 1.0) {
            analysis.append(format.tag("• **Production Status:** ADEQUATE - Meets needs but minimal margin for cloudy days.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Production Status:** ⚠️ INSUFFICIENT - Will not meet daily demand!", "RED") + "\n");
        }
    }

    private void appendBatteryAnalysis(StringBuilder analysis, double batteryAh, int voltage,
                                       double dod, int days, ColorFormatter format) {
        analysis.append("\n--- ⚡ BATTERY BANK SPECIFICATIONS ---\n");
        analysis.append(String.format("• **Required Capacity:** %.0f Ah @ %dV = %.1f kWh usable\n",
                batteryAh, voltage, (batteryAh * voltage) / 1000.0));
        analysis.append(String.format("• **Autonomy Period:** %d day%s without solar input\n",
                days, days > 1 ? "s" : ""));
        analysis.append(String.format("• **Depth of Discharge:** %.0f%% (%s for battery longevity)\n",
                dod, dod <= 50 ? "Conservative - Excellent" : dod <= 80 ? "Moderate - Good" : "Aggressive - Acceptable for LiFePO4"));

        // Battery chemistry recommendations
        analysis.append("\n• **Recommended Chemistry:**\n");
        if (batteryAh < 100 && dod <= 50) {
            analysis.append(format.tag("  ✓ AGM Lead-Acid - Cost-effective, proven, maintenance-free", "GREEN") + "\n");
        } else if (batteryAh < 300) {
            analysis.append(format.tag("  ✓ LiFePO4 (Recommended) - Better value long-term despite higher upfront cost", "GREEN") + "\n");
        } else {
            analysis.append(format.tag("  ✓ LiFePO4 ONLY - Large lead-acid banks are impractical (weight, space, maintenance)", "YELLOW") + "\n");
        }
    }

    private void appendInverterAnalysis(StringBuilder analysis, double inverterW,
                                        double actualLoad, ColorFormatter format) {
        analysis.append("\n--- 🔌 POWER INVERTER SELECTION ---\n");
        analysis.append(String.format("• **Continuous Rating:** %.0f W (with 25%% safety margin)\n", inverterW));
        analysis.append(String.format("• **Your Peak Load:** %.0f W actual\n", actualLoad));

        double surgeCap = inverterW * 2;
        analysis.append(String.format("• **Surge Capacity:** ~%.0f W (for motor/compressor startup)\n", surgeCap));

        if (inverterW < 1000) {
            analysis.append(format.tag("• **Type:** Modified sine wave acceptable, but pure sine recommended for electronics.", "GREEN") + "\n");
        } else if (inverterW < 3000) {
            analysis.append(format.tag("• **Type:** Pure Sine Wave REQUIRED for sensitive electronics, appliances with motors.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Type:** High-quality Pure Sine Wave with low THD (<3%). Grid-tie capable recommended.", "RED") + "\n");
        }
    }

    private void appendChargeControllerAnalysis(StringBuilder analysis, double controllerA,
                                                double pvWatts, int voltage, ColorFormatter format) {
        analysis.append("\n--- 🎛️ SOLAR CHARGE CONTROLLER ---\n");
        analysis.append(String.format("• **Required Rating:** %.0f A MPPT controller\n", controllerA));
        analysis.append(String.format("• **System Voltage:** %dV (must match battery bank)\n", voltage));

        if (pvWatts < 400) {
            analysis.append("  ✓ MPPT Recommended - 20-30% more efficient, especially in cold weather\n");
        } else {
            analysis.append(format.tag("  ✓ MPPT REQUIRED - System too large for PWM, would waste significant power", "YELLOW") + "\n");
        }
    }

    private void appendVoltageAnalysis(StringBuilder analysis, int voltage,
                                       double totalWh, ColorFormatter format) {
        analysis.append("\n--- 🎯 VOLTAGE SELECTION ANALYSIS ---\n");
        analysis.append(String.format("• **Selected Voltage:** %dV DC System\n", voltage));

        boolean optimal = isVoltageOptimal(totalWh, voltage);
        if (optimal) {
            analysis.append(format.tag("• **Optimization:** Perfect choice for this load size.", "GREEN") + "\n");
        } else {
            String recommended = totalWh > 4000 ? "48V" : totalWh > 1500 ? "24V" : "12V";
            analysis.append(format.tag("• **Optimization:** Consider " + recommended + " for better efficiency.", "YELLOW") + "\n");
        }
    }

    private String generateSystemSummaryAnalysis(double totalWh, double pvWatts, double batteryAh,
                                                 double inverterW, double controllerA) {
        StringBuilder summary = new StringBuilder();
        int voltage = appliance.getSystemVoltage();
        double psh = appliance.getPeakSunHours();
        int days = appliance.getDaysOfAutonomy();
        double dod = appliance.getDepthOfDischarge();

        ColorFormatter format = new ColorFormatter();

        summary.append("\n═══════════════════════════════════════════════════════════════\n");
        summary.append("                    🎯 EXECUTIVE SUMMARY\n");
        summary.append("═══════════════════════════════════════════════════════════════\n\n");

        // System Classification
        appendSystemClassification(summary, totalWh, voltage, format);

        // Investment Overview
        appendInvestmentOverview(summary, pvWatts, batteryAh, inverterW, controllerA, format);

        // Performance Scorecard
        appendPerformanceScorecard(summary, totalWh, pvWatts, psh, batteryAh, voltage, dod, days, inverterW, format);

        // Critical Considerations
        appendCriticalConsiderations(summary, totalWh, pvWatts, psh, batteryAh, voltage, inverterW, controllerA, format);

        // Pre-Purchase Checklist
        appendPrePurchaseChecklist(summary, pvWatts, batteryAh, voltage, inverterW, controllerA, totalWh);

        // Installation Priorities
        appendInstallationPriorities(summary, totalWh, inverterW);

        // Maintenance Schedule
        appendMaintenanceSchedule(summary);

        // ROI Analysis
        appendROIAnalysis(summary, totalWh, pvWatts, batteryAh, inverterW, controllerA, format);

        // Final Recommendation
        appendFinalRecommendation(summary, totalWh, pvWatts, psh, batteryAh, voltage, days, dod, inverterW, format);

        return summary.toString();
    }

    private void appendSystemClassification(StringBuilder summary, double totalWh,
                                            int voltage, ColorFormatter format) {
        summary.append("--- 📊 SYSTEM PROFILE ---\n");

        String systemClass;
        String primaryUse;
        String colorTag;

        if (totalWh < 500 && voltage == 12) {
            systemClass = "Ultra-Portable / Emergency Backup";
            primaryUse = "Weekend camping, van life, emergency preparedness kits";
            colorTag = "GREEN";
        } else if (totalWh < 1500 && voltage <= 24) {
            systemClass = "Small Off-Grid / Remote Power";
            primaryUse = "Cabin weekends, remote monitoring, RV full-timing, shed workshop";
            colorTag = "GREEN";
        } else if (totalWh < 3000 && voltage <= 24) {
            systemClass = "Medium Residential / Home Office";
            primaryUse = "Work-from-home setup, tiny house, essential circuit backup";
            colorTag = "YELLOW";
        } else if (totalWh < 5000 && voltage >= 24) {
            systemClass = "Large Residential / Small Farm";
            primaryUse = "Full-time off-grid living, whole-home backup, small farm operations";
            colorTag = "YELLOW";
        } else {
            systemClass = "Heavy-Duty Residential / Light Commercial";
            primaryUse = "Large household, farm with equipment, small business, workshop";
            colorTag = "RED";
        }

        summary.append(format.tag("• **Classification:** " + systemClass, colorTag) + "\n");
        summary.append(format.tag("• **Ideal Application:** " + primaryUse, colorTag) + "\n");
        summary.append(String.format("• **Daily Energy:** %.0f Wh (~%.1f kWh/month)\n\n",
                totalWh, (totalWh * 30) / 1000));
    }

    private void appendInvestmentOverview(StringBuilder summary, double pvWatts, double batteryAh,
                                          double inverterW, double controllerA, ColorFormatter format) {
        summary.append("--- 💰 INVESTMENT OVERVIEW ---\n");

        CountryConfig country = appliance.getCountry();
        String currencySymbol = country.getCurrencySymbol();

        double estimatedCost = estimateSystemCost(pvWatts, batteryAh, inverterW, appliance.getSystemVoltage());
        String costRange;
        String colorTag;

        if (estimatedCost < 50000) {
            costRange = currencySymbol + "25,000 - " + currencySymbol + "50,000";
            colorTag = "GREEN";
        } else if (estimatedCost < 150000) {
            costRange = currencySymbol + "50,000 - " + currencySymbol + "150,000";
            colorTag = "YELLOW";
        } else if (estimatedCost < 350000) {
            costRange = currencySymbol + "150,000 - " + currencySymbol + "350,000";
            colorTag = "YELLOW";
        } else {
            costRange = currencySymbol + "350,000 - " + currencySymbol + "750,000+";
            colorTag = "RED";
        }

        summary.append(format.tag("• **Estimated Budget:** " + costRange + " (equipment only, no labor)", colorTag) + "\n\n");

        // Budget breakdown - FIXED: Use country-specific pricing
        summary.append("• **Budget Breakdown:**\n");
        summary.append(String.format("  - Solar Panels: ~%s%.0f (%.0fW @ %s%.1f/W)\n",
                currencySymbol, pvWatts * country.getSolarPanelPricePerWatt(), pvWatts,
                currencySymbol, country.getSolarPanelPricePerWatt()));
        summary.append(String.format("  - Battery Bank: ~%s%.0f (%.0fAh LiFePO4 @ %s%.0f/Ah)\n",
                currencySymbol, batteryAh * country.getBatteryPricePerAh(), batteryAh,
                currencySymbol, country.getBatteryPricePerAh()));
        summary.append(String.format("  - Inverter: ~%s%.0f (%.0fW @ %s%.1f/W)\n",
                currencySymbol, inverterW * country.getInverterPricePerWatt(), inverterW,
                currencySymbol, country.getInverterPricePerWatt()));
        summary.append(String.format("  - Charge Controller: ~%s%.0f (%.0fA MPPT)\n",
                currencySymbol, controllerA * country.getControllerPricePerAmp(), controllerA));
        summary.append(String.format("  - Wiring/Hardware: ~%s15,000-25,000\n\n", currencySymbol));
    }

    private void appendPerformanceScorecard(StringBuilder summary, double totalWh, double pvWatts,
                                            double psh, double batteryAh, int voltage, double dod,
                                            int days, double inverterW, ColorFormatter format) {
        summary.append("--- 📈 PERFORMANCE SCORECARD ---\n");

        // Energy Balance Score
        double dailyProduction = pvWatts * psh * 0.85;
        double productionRatio = dailyProduction / totalWh;
        String energyGrade;
        String colorTag;

        if (productionRatio >= 1.5) {
            energyGrade = "A+ (Excellent Surplus)";
            colorTag = "GREEN";
        } else if (productionRatio >= 1.2) {
            energyGrade = "A (Optimal Balance)";
            colorTag = "GREEN";
        } else if (productionRatio >= 1.0) {
            energyGrade = "B (Adequate)";
            colorTag = "YELLOW";
        } else {
            energyGrade = "F (Insufficient)";
            colorTag = "RED";
        }

        summary.append(format.tag(String.format("1. **Energy Balance:** %s", energyGrade), colorTag) + "\n");
        summary.append(String.format("   (Produces %.0f Wh/day vs %.0f Wh/day needed)\n\n", dailyProduction, totalWh));

        // Battery Resilience Score
        String resilienceGrade = days >= 3 ? "A (Strong Backup)" : days >= 2 ? "B (Good Backup)" : "C (Minimal Backup)";
        colorTag = days >= 2 ? "GREEN" : "YELLOW";
        summary.append(format.tag(String.format("2. **Battery Resilience:** %s", resilienceGrade), colorTag) + "\n");
        summary.append(String.format("   (%.0f Ah @ %dV with %.0f%% DoD)\n\n", batteryAh, voltage, dod));

        // Voltage Optimization
        boolean voltageOptimal = isVoltageOptimal(totalWh, voltage);
        String voltageGrade = voltageOptimal ? "A (Optimal Choice)" : "B (Acceptable)";
        colorTag = voltageOptimal ? "GREEN" : "YELLOW";
        summary.append(format.tag(String.format("3. **Voltage Selection:** %s", voltageGrade), colorTag) + "\n\n");
    }

    private void appendCriticalConsiderations(StringBuilder summary, double totalWh, double pvWatts,
                                              double psh, double batteryAh, int voltage, double inverterW,
                                              double controllerA, ColorFormatter format) {
        summary.append("--- ⚠️ CRITICAL CONSIDERATIONS ---\n");

        double dailyProduction = pvWatts * psh * 0.85;
        double productionRatio = dailyProduction / totalWh;
        boolean hasCriticalIssues = false;

        if (productionRatio < 1.0) {
            summary.append(format.tag("❌ ENERGY DEFICIT: Solar array undersized. System will drain batteries daily!", "RED") + "\n");
            summary.append(format.tag("   → ACTION: Increase PV array by " +
                    String.format("%.0f%%", (1/productionRatio - 1) * 100) +
                    " OR reduce load consumption.", "RED") + "\n");
            hasCriticalIssues = true;
        }

        if (batteryAh > 400) {
            summary.append(format.tag("❌ SAFETY: Large battery bank REQUIRES Battery Management System (BMS)!", "RED") + "\n");
            hasCriticalIssues = true;
        }

        if (!hasCriticalIssues) {
            summary.append(format.tag("✅ No critical issues found. System design is sound.", "GREEN") + "\n");
        }

        summary.append("\n");
    }

    private void appendPrePurchaseChecklist(StringBuilder summary, double pvWatts, double batteryAh,
                                            int voltage, double inverterW, double controllerA, double totalWh) {
        summary.append("--- ✅ PRE-PURCHASE CHECKLIST ---\n");
        summary.append("Before buying components, verify:\n\n");

        summary.append("**Solar Panels:**\n");
        summary.append(String.format("  ☐ Total wattage: %.0fW minimum\n", pvWatts));
        summary.append(String.format("  ☐ Panel voltage suitable for %dV system\n", voltage));
        summary.append("  ☐ 25-year warranty (standard)\n\n");

        summary.append("**Battery Bank:**\n");
        summary.append(String.format("  ☐ Capacity: %.0f Ah minimum @ %dV\n", batteryAh, voltage));
        summary.append("  ☐ LiFePO4 recommended (10+ year lifespan)\n");
        summary.append("  ☐ BMS included\n\n");

        summary.append("**Inverter:**\n");
        summary.append(String.format("  ☐ Continuous rating: %.0fW minimum\n", inverterW));
        summary.append("  ☐ Pure sine wave output\n");
        summary.append(String.format("  ☐ Input voltage: %dV DC\n\n", voltage));

        summary.append("**Charge Controller:**\n");
        summary.append(String.format("  ☐ Current rating: %.0fA minimum (MPPT)\n", controllerA));
        summary.append(String.format("  ☐ System voltage: %dV compatible\n\n", voltage));
    }

    private void appendInstallationPriorities(StringBuilder summary, double totalWh, double inverterW) {
        summary.append("--- 🔧 INSTALLATION PRIORITIES ---\n");
        summary.append("Complete these steps in order:\n\n");

        summary.append("**Phase 1: Planning**\n");
        if (totalWh > 3000 || inverterW > 3000) {
            summary.append("  1. Check local codes and permits (REQUIRED)\n");
        } else {
            summary.append("  1. Review local codes\n");
        }
        summary.append("  2. Measure mounting locations\n");
        summary.append("  3. Calculate wire runs\n\n");

        summary.append("**Phase 2: Installation**\n");
        summary.append("  1. Mount solar panels\n");
        summary.append("  2. Install battery bank\n");
        summary.append("  3. Wire DC side first\n");
        summary.append("  4. Wire AC side last\n\n");

        summary.append("**Phase 3: Testing**\n");
        summary.append("  1. Verify all voltages\n");
        summary.append("  2. Check polarity\n");
        summary.append("  3. Test with small load first\n\n");
    }

    private void appendMaintenanceSchedule(StringBuilder summary) {
        summary.append("--- 🛠️ MAINTENANCE SCHEDULE ---\n");
        summary.append("**Monthly:** Check battery voltage, inspect connections\n");
        summary.append("**Quarterly:** Clean solar panels\n");
        summary.append("**Annually:** Professional inspection\n\n");
    }

    private void appendROIAnalysis(StringBuilder summary, double totalWh, double pvWatts,
                                   double batteryAh, double inverterW, double controllerA,
                                   ColorFormatter format) {
        summary.append("--- 💵 RETURN ON INVESTMENT ---\n");

        CountryConfig country = appliance.getCountry();
        String currencySymbol = country.getCurrencySymbol();

        double monthlyKwh = (totalWh * 30) / 1000;
        double monthlySavings = monthlyKwh * country.getElectricityRatePerKwh();
        double annualSavings = monthlySavings * 12;
        double estimatedCost = estimateSystemCost(pvWatts, batteryAh, inverterW, appliance.getSystemVoltage());
        double paybackYears = estimatedCost / annualSavings;

        summary.append(String.format("• **Monthly Savings:** %s%.0f\n", currencySymbol, monthlySavings));
        summary.append(String.format("• **Annual Savings:** %s%.0f\n", currencySymbol, annualSavings));

        String colorTag = paybackYears < 6 ? "GREEN" : "YELLOW";
        summary.append(format.tag(String.format("• **Payback Period:** ~%.1f years", paybackYears), colorTag) + "\n\n");
    }

    private void appendFinalRecommendation(StringBuilder summary, double totalWh, double pvWatts,
                                           double psh, double batteryAh, int voltage, int days,
                                           double dod, double inverterW, ColorFormatter format) {
        summary.append("═══════════════════════════════════════════════════════════════\n");
        summary.append("                  🌟 FINAL RECOMMENDATION\n");
        summary.append("═══════════════════════════════════════════════════════════════\n\n");

        // Calculate overall score
        int score = calculateSystemScore(totalWh, pvWatts, psh, days, voltage, inverterW);

        String overallGrade;
        String verdict;
        String colorTag;

        if (score >= 90) {
            overallGrade = "A+ (Excellent System)";
            verdict = "EXCEPTIONALLY WELL-DESIGNED SYSTEM with optimal sizing.";
            colorTag = "GREEN";
        } else if (score >= 75) {
            overallGrade = "A (Very Good System)";
            verdict = "SOLID, WELL-BALANCED SYSTEM that will meet your needs.";
            colorTag = "GREEN";
        } else if (score >= 60) {
            overallGrade = "B (Good with Caveats)";
            verdict = "FUNCTIONAL SYSTEM with room for improvement.";
            colorTag = "YELLOW";
        } else {
            overallGrade = "C (Needs Improvement)";
            verdict = "System has DESIGN ISSUES that should be addressed.";
            colorTag = "YELLOW";
        }

        summary.append(format.tag("**Overall Grade:** " + overallGrade + " (Score: " + score + "/100)", colorTag) + "\n\n");
        summary.append(format.tag(verdict, colorTag) + "\n\n");

        summary.append("═══════════════════════════════════════════════════════════════\n");
    }

    private int calculateSystemScore(double totalWh, double pvWatts, double psh,
                                     int days, int voltage, double inverterW) {
        int score = 0;

        // Energy balance (25 points)
        double dailyProduction = pvWatts * psh * 0.85;
        double productionRatio = dailyProduction / totalWh;
        if (productionRatio >= 1.2) score += 25;
        else if (productionRatio >= 1.0) score += 15;

        // Autonomy (25 points)
        if (days >= 3) score += 25;
        else if (days >= 2) score += 20;
        else score += 10;

        // Voltage optimization (25 points)
        if (isVoltageOptimal(totalWh, voltage)) score += 25;
        else score += 15;

        // Component sizing (25 points)
        double actualLoad = appliance.getWatts() * appliance.getQuantity();
        double inverterUtilization = actualLoad / inverterW;
        if (inverterUtilization >= 0.5 && inverterUtilization <= 0.8) score += 25;
        else score += 15;

        return score;
    }

    // ============================================================================
    // SECTION 6: EXPORT METHODS
    // ============================================================================

    private void handleCSVExport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV");
        fileChooser.setSelectedFile(new File("SolarCalculator_Results.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File csvFile = fileChooser.getSelectedFile();
            exportToCSVHandler(csvFile);
        }
    }

    private void exportToCSVHandler(File file) {
        try {
            exportToCSV(file);
            showSuccess("✅ Results exported successfully to CSV!");
        } catch (IOException ex) {
            showError("❌ Error exporting CSV: " + ex.getMessage());
        }
    }

    private void exportToCSV(File file) throws IOException {
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        CountryConfig country = appliance.getCountry();
        String currencySymbol = country.getCurrencySymbol();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Pre-calculations
            double totalWh = appliance.energyPerDayWh();
            SolarCalculator calc = mainPage.getCalculator();
            double pvWatts = calc.requiredPvWatts(totalWh, appliance.getPeakSunHours());
            double batteryAh = calc.requiredBatteryAh(totalWh, appliance.getDaysOfAutonomy(),
                    appliance.getDepthOfDischarge(), appliance.getSystemVoltage());
            double inverterW = calc.recommendedInverterW(appliance.getWatts() * appliance.getQuantity());
            double controllerA = calc.recommendedControllerA(pvWatts, appliance.getSystemVoltage());

            double estimatedCost = estimateSystemCost(pvWatts, batteryAh, inverterW, appliance.getSystemVoltage());
            double monthlyKwh = (totalWh * 30) / 1000;
            double monthlySavings = monthlyKwh * country.getElectricityRatePerKwh();
            double annualSavings = monthlySavings * 12;
            double paybackYears = estimatedCost / annualSavings;
            double dailyProduction = pvWatts * appliance.getPeakSunHours() * 0.85;
            double productionRatio = dailyProduction / totalWh;

            // Write comprehensive header
            writer.println("SOLAR SYSTEM CALCULATION REPORT");
            writer.println("Appliance:," + appliance.getName());
            writer.println("Country:," + country.getDisplayName());
            writer.println("Currency:," + currencySymbol);
            writer.println("Export Date:," + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();

            // BASIC APPLIANCE DETAILS
            writer.println("BASIC APPLIANCE DETAILS");
            writer.println("Parameter,Value,Unit");
            writer.println("Appliance Name," + appliance.getName() + ",");
            writer.println("Power Rating," + appliance.getWatts() + ",Watts");
            writer.println("Quantity," + appliance.getQuantity() + ",");
            writer.println("Hours per Day," + appliance.getHoursPerDay() + ",hours");
            writer.println("Daily Energy Consumption," + String.format("%.2f", totalWh) + ",Wh/day");
            writer.println("Monthly Energy Equivalent," + String.format("%.2f", monthlyKwh) + ",kWh/month");
            writer.println();

            // SOLAR PARAMETERS
            writer.println("SOLAR PARAMETERS");
            writer.println("Parameter,Value,Unit");
            writer.println("Peak Sun Hours," + appliance.getPeakSunHours() + ",hours");
            writer.println("Depth of Discharge," + appliance.getDepthOfDischarge() + ",%");
            writer.println("Days of Autonomy," + appliance.getDaysOfAutonomy() + ",days");
            writer.println("System Voltage," + appliance.getSystemVoltage() + ",V DC");
            writer.println();

            // SYSTEM COMPONENTS
            writer.println("REQUIRED SYSTEM COMPONENTS");
            writer.println("Component,Specification,Value,Unit");
            writer.println("Solar PV Array,Total Capacity," + String.format("%.2f", pvWatts) + ",W");
            writer.println("Solar PV Array,300W Panels Required," + (int) Math.ceil(pvWatts / 300.0) + ",panels");
            writer.println("Solar PV Array,400W Panels Required," + (int) Math.ceil(pvWatts / 400.0) + ",panels");
            writer.println("Battery Bank,Capacity," + String.format("%.2f", batteryAh) + ",Ah");
            writer.println("Battery Bank,Usable Energy," + String.format("%.2f", (batteryAh * appliance.getSystemVoltage()) / 1000.0) + ",kWh");
            writer.println("Inverter,Continuous Rating," + String.format("%.2f", inverterW) + ",W");
            writer.println("Inverter,Recommended Type,Pure Sine Wave,");
            writer.println("Charge Controller,Current Rating," + String.format("%.2f", controllerA) + ",A");
            writer.println("Charge Controller,Recommended Type,MPPT,");
            writer.println();

            // PERFORMANCE METRICS
            writer.println("SYSTEM PERFORMANCE METRICS");
            writer.println("Metric,Value,Unit,Assessment");
            writer.println("Daily Energy Production," + String.format("%.2f", dailyProduction) + ",Wh," + getProductionAssessment(productionRatio));
            writer.println("Daily Energy Consumption," + String.format("%.2f", totalWh) + ",Wh,Base Load");
            writer.println("Production Ratio," + String.format("%.2f", productionRatio) + ",Ratio," + getRatioAssessment(productionRatio));
            writer.println("System Efficiency," + "85,%" + " Estimated");
            writer.println("Autonomy Period," + appliance.getDaysOfAutonomy() + ",days," + getAutonomyAssessment(appliance.getDaysOfAutonomy()));
            writer.println();

            // FINANCIAL ANALYSIS
            writer.println("FINANCIAL ANALYSIS");
            writer.println("Category,Amount,Currency,Period");
            writer.println("Estimated System Cost," + String.format("%.2f", estimatedCost) + "," + currencySymbol + ",One-time");
            writer.println("Solar Panels Cost," + String.format("%.2f", pvWatts * country.getSolarPanelPricePerWatt()) + "," + currencySymbol + ",One-time");
            writer.println("Battery Bank Cost," + String.format("%.2f", batteryAh * country.getBatteryPricePerAh()) + "," + currencySymbol + ",One-time");
            writer.println("Inverter Cost," + String.format("%.2f", inverterW * country.getInverterPricePerWatt()) + "," + currencySymbol + ",One-time");
            writer.println("Controller Cost," + String.format("%.2f", controllerA * country.getControllerPricePerAmp()) + "," + currencySymbol + ",One-time");
            writer.println("Monthly Energy Savings," + String.format("%.2f", monthlySavings) + "," + currencySymbol + ",Monthly");
            writer.println("Annual Energy Savings," + String.format("%.2f", annualSavings) + "," + currencySymbol + ",Annual");
            writer.println("Simple Payback Period," + String.format("%.2f", paybackYears) + ",Years,");
            writer.println("Electricity Rate," + String.format("%.2f", country.getElectricityRatePerKwh()) + "," + currencySymbol + ",per kWh");
            writer.println();

            // SYSTEM ASSESSMENT
            writer.println("SYSTEM ASSESSMENT");
            writer.println("Aspect,Rating,Score,Recommendation");
            writer.println("Energy Balance," + getEnergyBalanceRating(productionRatio) + "," + getEnergyBalanceScore(productionRatio) + "," + getEnergyBalanceRecommendation(productionRatio));
            writer.println("Battery Resilience," + getBatteryResilienceRating(appliance.getDaysOfAutonomy()) + "," + getBatteryResilienceScore(appliance.getDaysOfAutonomy()) + "," + getBatteryResilienceRecommendation(appliance.getDaysOfAutonomy()));
            writer.println("Voltage Optimization," + getVoltageRating(totalWh, appliance.getSystemVoltage()) + "," + getVoltageScore(totalWh, appliance.getSystemVoltage()) + "," + getVoltageRecommendation(totalWh, appliance.getSystemVoltage()));
            writer.println("Overall System Score," + calculateSystemScore(totalWh, pvWatts, appliance.getPeakSunHours(), appliance.getDaysOfAutonomy(), appliance.getSystemVoltage(), inverterW) + ",/100," + getOverallRecommendation(totalWh, pvWatts, appliance.getPeakSunHours(), appliance.getDaysOfAutonomy(), appliance.getSystemVoltage(), inverterW));
        }
    }

    // Helper methods for CSV assessments
    private String getProductionAssessment(double ratio) {
        if (ratio >= 1.5) return "Excellent Surplus";
        if (ratio >= 1.2) return "Good Margin";
        if (ratio >= 1.0) return "Adequate";
        return "Insufficient";
    }

    private String getRatioAssessment(double ratio) {
        if (ratio >= 1.5) return "50%+ Safety Margin";
        if (ratio >= 1.2) return "20% Safety Margin";
        if (ratio >= 1.0) return "Meets Requirements";
        return "Needs Improvement";
    }

    private String getAutonomyAssessment(int days) {
        if (days >= 3) return "Strong Backup";
        if (days >= 2) return "Good Backup";
        return "Minimal Backup";
    }

    private String getEnergyBalanceRating(double ratio) {
        if (ratio >= 1.5) return "A+";
        if (ratio >= 1.2) return "A";
        if (ratio >= 1.0) return "B";
        return "C";
    }

    private String getEnergyBalanceScore(double ratio) {
        if (ratio >= 1.5) return "25";
        if (ratio >= 1.2) return "20";
        if (ratio >= 1.0) return "15";
        return "5";
    }

    private String getEnergyBalanceRecommendation(double ratio) {
        if (ratio >= 1.5) return "Excellent energy balance";
        if (ratio >= 1.2) return "Good energy balance";
        if (ratio >= 1.0) return "Adequate energy balance";
        return "Increase PV capacity or reduce load";
    }

    private String getBatteryResilienceRating(int days) {
        if (days >= 3) return "A";
        if (days >= 2) return "B";
        return "C";
    }

    private String getBatteryResilienceScore(int days) {
        if (days >= 3) return "25";
        if (days >= 2) return "20";
        return "10";
    }

    private String getBatteryResilienceRecommendation(int days) {
        if (days >= 3) return "Strong backup capability";
        if (days >= 2) return "Adequate backup capability";
        return "Consider increasing battery capacity";
    }

    private String getVoltageRating(double totalWh, int voltage) {
        return isVoltageOptimal(totalWh, voltage) ? "A" : "B";
    }

    private String getVoltageScore(double totalWh, int voltage) {
        return isVoltageOptimal(totalWh, voltage) ? "25" : "15";
    }

    private String getVoltageRecommendation(double totalWh, int voltage) {
        if (isVoltageOptimal(totalWh, voltage)) {
            return "Optimal voltage selection";
        } else {
            String recommended = totalWh > 4000 ? "48V" : totalWh > 1500 ? "24V" : "12V";
            return "Consider " + recommended + " for better efficiency";
        }
    }

    private String getOverallRecommendation(double totalWh, double pvWatts, double psh, int days, int voltage, double inverterW) {
        int score = calculateSystemScore(totalWh, pvWatts, psh, days, voltage, inverterW);
        if (score >= 90) return "Excellent system design";
        if (score >= 75) return "Very good system design";
        if (score >= 60) return "Good system with minor improvements possible";
        return "System needs significant improvements";
    }

    private void exportResultsToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF");
        fileChooser.setSelectedFile(new File("SolarCalculator_Results.pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File pdfFile = fileChooser.getSelectedFile();
            try {
                generatePDF(pdfFile);
                showSuccess("✅ Results exported successfully to PDF!\n" + pdfFile.getAbsolutePath());
            } catch (Exception ex) {
                showError("❌ Error exporting PDF: " + ex.getMessage());
            }
        }
    }

    private void generatePDF(File pdfFile) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        // Define fonts
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 20, com.itextpdf.text.Font.BOLD,
                new com.itextpdf.text.BaseColor(30, 41, 59));

        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD,
                new com.itextpdf.text.BaseColor(37, 99, 235));

        com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD,
                new com.itextpdf.text.BaseColor(55, 65, 81));

        com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD,
                new com.itextpdf.text.BaseColor(31, 41, 55));

        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL,
                new com.itextpdf.text.BaseColor(75, 85, 99));

        com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL,
                new com.itextpdf.text.BaseColor(107, 114, 128));

        // Add title and header
        Paragraph title = new Paragraph("☀️ SOLAR SYSTEM ANALYSIS REPORT\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Add metadata
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
        String dateTime = LocalDateTime.now().format(formatter);

        CountryConfig country = appliance.getCountry();
        String currencySymbol = country.getCurrencySymbol();

        document.add(new Paragraph("Generated on: " + dateTime, normalFont));
        document.add(new Paragraph("Appliance: " + appliance.getName(), normalFont));
        document.add(new Paragraph("Country: " + country.getDisplayName(), normalFont));
        document.add(new Paragraph("Currency: " + currencySymbol, normalFont));
        document.add(new Paragraph("\n"));

        // Perform calculations for the report
        double totalWh = appliance.energyPerDayWh();
        SolarCalculator calc = mainPage.getCalculator();
        double pvWatts = calc.requiredPvWatts(totalWh, appliance.getPeakSunHours());
        double batteryAh = calc.requiredBatteryAh(totalWh, appliance.getDaysOfAutonomy(),
                appliance.getDepthOfDischarge(), appliance.getSystemVoltage());
        double inverterW = calc.recommendedInverterW(appliance.getWatts() * appliance.getQuantity());
        double controllerA = calc.recommendedControllerA(pvWatts, appliance.getSystemVoltage());

        // EXECUTIVE SUMMARY
        Paragraph execHeader = new Paragraph("🎯 EXECUTIVE SUMMARY", headerFont);
        execHeader.setSpacingBefore(20f);
        execHeader.setSpacingAfter(10f);
        document.add(execHeader);
        document.add(new Paragraph("────────────────────────────────────────────────────────", normalFont));

        // System Classification
        String systemClass = getSystemClassification(totalWh, appliance.getSystemVoltage());
        document.add(new Paragraph("System Classification: " + systemClass, boldFont));
        document.add(new Paragraph("Overall Score: " + calculateSystemScore(totalWh, pvWatts,
                appliance.getPeakSunHours(), appliance.getDaysOfAutonomy(),
                appliance.getSystemVoltage(), inverterW) + "/100", boldFont));
        document.add(new Paragraph("\n"));

        // KEY COMPONENTS SUMMARY
        Paragraph componentsHeader = new Paragraph("⚙️ REQUIRED COMPONENTS", headerFont);
        componentsHeader.setSpacingBefore(15f);
        componentsHeader.setSpacingAfter(10f);
        document.add(componentsHeader);
        document.add(new Paragraph("────────────────────────────────────────────────────────", normalFont));

        document.add(createComponentLine("Total Daily Energy", String.format("%.0f Wh/day", totalWh)));
        document.add(createComponentLine("Solar PV Array", String.format("%.0f W", pvWatts)));
        document.add(createComponentLine("Battery Bank", String.format("%.0f Ah @ %dV", batteryAh, appliance.getSystemVoltage())));
        document.add(createComponentLine("Inverter", String.format("%.0f W Pure Sine Wave", inverterW)));
        document.add(createComponentLine("Charge Controller", String.format("%.0f A MPPT", controllerA)));
        document.add(new Paragraph("\n"));

        // SOLAR PARAMETERS
        Paragraph paramsHeader = new Paragraph("📊 SYSTEM PARAMETERS", headerFont);
        paramsHeader.setSpacingBefore(15f);
        paramsHeader.setSpacingAfter(10f);
        document.add(paramsHeader);
        document.add(new Paragraph("────────────────────────────────────────────────────────", normalFont));

        document.add(createParameterLine("Peak Sun Hours", String.format("%.1f hours", appliance.getPeakSunHours())));
        document.add(createParameterLine("Depth of Discharge", String.format("%.0f%%", appliance.getDepthOfDischarge())));
        document.add(createParameterLine("Days of Autonomy", appliance.getDaysOfAutonomy() + " days"));
        document.add(createParameterLine("System Voltage", appliance.getSystemVoltage() + "V DC"));
        document.add(new Paragraph("\n"));

        // COST ANALYSIS
        Paragraph costHeader = new Paragraph("💰 INVESTMENT ANALYSIS", headerFont);
        costHeader.setSpacingBefore(15f);
        costHeader.setSpacingAfter(10f);
        document.add(costHeader);
        document.add(new Paragraph("────────────────────────────────────────────────────────", normalFont));

        double estimatedCost = estimateSystemCost(pvWatts, batteryAh, inverterW, appliance.getSystemVoltage());
        double monthlyKwh = (totalWh * 30) / 1000;
        double monthlySavings = monthlyKwh * country.getElectricityRatePerKwh();
        double annualSavings = monthlySavings * 12;
        double paybackYears = estimatedCost / annualSavings;

        document.add(createCostLine("Estimated System Cost", String.format("%s%,.0f", currencySymbol, estimatedCost)));
        document.add(createCostLine("Monthly Energy Savings", String.format("%s%,.0f", currencySymbol, monthlySavings)));
        document.add(createCostLine("Annual Savings", String.format("%s%,.0f", currencySymbol, annualSavings)));
        document.add(createCostLine("Payback Period", String.format("%.1f years", paybackYears)));
        document.add(new Paragraph("\n"));

        // DETAILED BREAKDOWN
        Paragraph breakdownHeader = new Paragraph("🔧 COMPONENT BREAKDOWN", headerFont);
        breakdownHeader.setSpacingBefore(15f);
        breakdownHeader.setSpacingAfter(10f);
        document.add(breakdownHeader);
        document.add(new Paragraph("────────────────────────────────────────────────────────", normalFont));

        document.add(new Paragraph("Solar Panels:", boldFont));
        document.add(new Paragraph(String.format("  • %.0fW total capacity", pvWatts), normalFont));
        document.add(new Paragraph(String.format("  • %d x 300W panels OR %d x 400W panels",
                (int) Math.ceil(pvWatts / 300.0), (int) Math.ceil(pvWatts / 400.0)), normalFont));
        document.add(new Paragraph(String.format("  • Estimated cost: %s%,.0f",
                currencySymbol, pvWatts * country.getSolarPanelPricePerWatt()), normalFont));
        document.add(new Paragraph(""));

        document.add(new Paragraph("Battery Bank:", boldFont));
        document.add(new Paragraph(String.format("  • %.0fAh @ %dV (%.1f kWh usable)",
                batteryAh, appliance.getSystemVoltage(), (batteryAh * appliance.getSystemVoltage()) / 1000.0), normalFont));
        document.add(new Paragraph(String.format("  • %d days autonomy @ %.0f%% DoD",
                appliance.getDaysOfAutonomy(), appliance.getDepthOfDischarge()), normalFont));
        document.add(new Paragraph(String.format("  • Recommended: LiFePO4 chemistry", currencySymbol, batteryAh * country.getBatteryPricePerAh()), normalFont));
        document.add(new Paragraph(""));

        document.add(new Paragraph("Power Conversion:", boldFont));
        document.add(new Paragraph(String.format("  • Inverter: %.0fW pure sine wave", inverterW), normalFont));
        document.add(new Paragraph(String.format("  • Charge Controller: %.0fA MPPT", controllerA), normalFont));
        document.add(new Paragraph(""));

        // PERFORMANCE ANALYSIS
        Paragraph performanceHeader = new Paragraph("📈 PERFORMANCE ANALYSIS", headerFont);
        performanceHeader.setSpacingBefore(15f);
        performanceHeader.setSpacingAfter(10f);
        document.add(performanceHeader);
        document.add(new Paragraph("────────────────────────────────────────────────────────", normalFont));

        double dailyProduction = pvWatts * appliance.getPeakSunHours() * 0.85;
        double productionRatio = dailyProduction / totalWh;

        document.add(new Paragraph("Energy Production vs Consumption:", boldFont));
        document.add(new Paragraph(String.format("  • Daily Production: %.0f Wh", dailyProduction), normalFont));
        document.add(new Paragraph(String.format("  • Daily Consumption: %.0f Wh", totalWh), normalFont));
        document.add(new Paragraph(String.format("  • Production Ratio: %.2f:1", productionRatio),
                productionRatio >= 1.2 ? boldFont : normalFont));
        document.add(new Paragraph(""));

        String efficiencyNote = productionRatio >= 1.5 ? "EXCELLENT - Significant surplus for cloudy days" :
                productionRatio >= 1.2 ? "GOOD - Adequate margin for seasonal variation" :
                        productionRatio >= 1.0 ? "ADEQUATE - Meets basic needs" :
                                "INSUFFICIENT - Will not meet daily demand";
        document.add(new Paragraph("Efficiency Rating: " + efficiencyNote, boldFont));
        document.add(new Paragraph(""));

        // RECOMMENDATIONS
        Paragraph recommendationsHeader = new Paragraph("💡 RECOMMENDATIONS & NEXT STEPS", headerFont);
        recommendationsHeader.setSpacingBefore(15f);
        recommendationsHeader.setSpacingAfter(10f);
        document.add(recommendationsHeader);
        document.add(new Paragraph("────────────────────────────────────────────────────────", normalFont));

        document.add(new Paragraph("Immediate Actions:", boldFont));
        document.add(new Paragraph("  1. Verify local permitting requirements", normalFont));
        document.add(new Paragraph("  2. Obtain competitive quotes for components", normalFont));
        document.add(new Paragraph("  3. Plan installation location and mounting", normalFont));
        document.add(new Paragraph(""));

        document.add(new Paragraph("Component Sourcing:", boldFont));
        document.add(new Paragraph("  • Purchase from reputable solar equipment suppliers", normalFont));
        document.add(new Paragraph("  • Ensure proper warranties (25+ years for panels)", normalFont));
        document.add(new Paragraph("  • Consider professional installation for larger systems", normalFont));
        document.add(new Paragraph(""));

        document.add(new Paragraph("Maintenance Schedule:", boldFont));
        document.add(new Paragraph("  • Monthly: Visual inspection and connection checks", normalFont));
        document.add(new Paragraph("  • Quarterly: Panel cleaning and performance verification", normalFont));
        document.add(new Paragraph("  • Annually: Professional system inspection", normalFont));
        document.add(new Paragraph(""));

        // FOOTER
        Paragraph footer = new Paragraph("\n\n--- END OF REPORT ---\n", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        Paragraph disclaimer = new Paragraph(
                "This report provides estimated calculations for planning purposes.\n" +
                        "Actual performance may vary based on environmental conditions, installation quality,\n" +
                        "and component specifications. Consult with qualified professionals before proceeding\n" +
                        "with installation. Generated by Solar Calculator v1.0.",
                smallFont);
        disclaimer.setAlignment(Element.ALIGN_CENTER);
        document.add(disclaimer);

        document.close();
    }

    // Helper methods for PDF creation
    private Paragraph createComponentLine(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
        p.add(new Chunk(value, new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL)));
        return p;
    }

    private Paragraph createParameterLine(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk("• " + label + ": ", new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
        p.add(new Chunk(value, new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL)));
        return p;
    }

    private Paragraph createCostLine(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk("• " + label + ": ", new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
        p.add(new Chunk(value, new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD,
                new com.itextpdf.text.BaseColor(34, 197, 94))));
        return p;
    }

    private String getSystemClassification(double totalWh, int voltage) {
        if (totalWh < 500 && voltage == 12) return "Ultra-Portable / Emergency Backup";
        if (totalWh < 1500 && voltage <= 24) return "Small Off-Grid / Remote Power";
        if (totalWh < 3000 && voltage <= 24) return "Medium Residential / Home Office";
        if (totalWh < 5000 && voltage >= 24) return "Large Residential / Small Farm";
        return "Heavy-Duty Residential / Light Commercial";
    }

    // ============================================================================
    // SECTION 7: HELPER & UTILITY METHODS
    // ============================================================================

    // ---------- Results Display ----------

    private void displayWelcomeMessage() {
        resultsCard.removeAll();
        resultsCard.add(Box.createVerticalStrut(50));

        JLabel welcomeLabel = new JLabel("Ready for Individual Calculation");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(TEXT_SECONDARY);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructionLabel = new JLabel("Set solar parameters and click 'Calculate This Appliance'");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructionLabel.setForeground(TEXT_SECONDARY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultsCard.add(welcomeLabel);
        resultsCard.add(Box.createVerticalStrut(10));
        resultsCard.add(instructionLabel);

        resultsCard.revalidate();
        resultsCard.repaint();
    }

    private void updateResultsDisplay(String rawText) {
        resultsCard.removeAll();

        // Add results header
        JPanel resultsHeader = new JPanel(new BorderLayout());
        resultsHeader.setBackground(CARD_HEADER_BG);
        resultsHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel summaryLabel = new JLabel("Individual Appliance Analysis - " + appliance.getName());
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        summaryLabel.setForeground(PRIMARY_COLOR);

        resultsHeader.add(summaryLabel, BorderLayout.WEST);
        resultsCard.add(resultsHeader);

        // Parse and display results
        String[] lines = rawText.split("\n");
        resultsCard.add(Box.createVerticalStrut(10));

        boolean mainMetricsDisplayed = false;
        boolean analysisSection = false;

        for (String line : lines) {
            if (line.contains("=== SYSTEM ANALYSIS ===")) {
                analysisSection = true;
                addSectionHeader("System Analysis & Recommendations");
                continue;
            }

            if (analysisSection) {
                if (line.startsWith("---")) {
                    addSectionHeader(line.replace("---", "").trim());
                } else if (!line.trim().isEmpty()) {
                    JPanel analysisPanel = createStyledAnalysisPanel(line);
                    if (analysisPanel != null) {
                        analysisPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        resultsCard.add(analysisPanel);
                    }
                } else {
                    resultsCard.add(Box.createVerticalStrut(5));
                }
            } else if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String label = parts[0].trim();
                    String value = parts[1].trim();

                    if (isMainMetric(label)) {
                        if (!mainMetricsDisplayed) {
                            addSectionHeader("Required Components");
                            mainMetricsDisplayed = true;
                        }
                        resultsCard.add(createMetricPanel(label, value));
                        resultsCard.add(Box.createVerticalStrut(8));
                    }
                }
            }
        }

        resultsCard.revalidate();
        resultsCard.repaint();
    }

    private JPanel createStyledAnalysisPanel(String rawLine) {
        JPanel linePanel = new JPanel(new BorderLayout());

        Color bgColor = resultsCard.getBackground();
        Color fgColor = TEXT_PRIMARY;
        int fontStyle = Font.PLAIN;

        String content = rawLine;

        // Check for color tags
        if (rawLine.contains("[RED]")) {
            bgColor = ANALYSIS_RED_BG;
            fgColor = TEXT_WHITE;
            content = rawLine.replace("[RED]", "").replace("[/RED]", "");
        } else if (rawLine.contains("[YELLOW]")) {
            bgColor = ANALYSIS_YELLOW_BG;
            fgColor = TEXT_PRIMARY;
            content = rawLine.replace("[YELLOW]", "").replace("[/YELLOW]", "");
        } else if (rawLine.contains("[GREEN]")) {
            bgColor = ANALYSIS_GREEN_BG;
            fgColor = TEXT_WHITE;
            content = rawLine.replace("[GREEN]", "").replace("[/GREEN]", "");
        }

        // Check for bold tags
        if (content.contains("**")) {
            fontStyle = Font.BOLD;
            content = content.replace("**", "");
        }

        linePanel.setBackground(bgColor);

        JLabel analysisLine = new JLabel(content);
        analysisLine.setFont(new Font("Segoe UI", fontStyle, 13));
        analysisLine.setForeground(fgColor);

        // Adjust indentation
        if (content.trim().startsWith("•")) {
            linePanel.setBorder(BorderFactory.createEmptyBorder(3, 15, 3, 15));
        } else if (content.trim().startsWith("-")) {
            linePanel.setBorder(BorderFactory.createEmptyBorder(2, 30, 2, 15));
        } else {
            linePanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        }

        linePanel.add(analysisLine, BorderLayout.WEST);

        return linePanel;
    }

    private void addSectionHeader(String title) {
        resultsCard.add(Box.createVerticalStrut(20));

        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        header.setForeground(TEXT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        resultsCard.add(header);

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(229, 231, 235));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultsCard.add(separator);
        resultsCard.add(Box.createVerticalStrut(10));
    }

    private JPanel createMetricPanel(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel nameLabel = new JLabel(getIconForMetric(label) + " " + label);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(formatValue(value, label));
        valueLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        valueLabel.setForeground(getColorForMetric(label));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        String unit = getUnitForMetric(label);
        if (!unit.isEmpty()) {
            JLabel unitLabel = new JLabel(unit);
            unitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            unitLabel.setForeground(TEXT_SECONDARY);
            unitLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel valuePanel = new JPanel(new BorderLayout());
            valuePanel.setBackground(CARD_BACKGROUND);
            valuePanel.add(valueLabel, BorderLayout.CENTER);
            valuePanel.add(unitLabel, BorderLayout.SOUTH);

            panel.add(nameLabel, BorderLayout.WEST);
            panel.add(valuePanel, BorderLayout.EAST);
        } else {
            panel.add(nameLabel, BorderLayout.WEST);
            panel.add(valueLabel, BorderLayout.EAST);
        }

        return panel;
    }

    // ---------- Metric Helpers ----------

    private boolean isMainMetric(String label) {
        return label.equals("Total Daily Energy") || label.equals("Required PV Array") ||
                label.equals("Battery Capacity") || label.equals("Inverter Size") ||
                label.equals("Charge Controller");
    }

    private String getIconForMetric(String metric) {
        switch (metric) {
            case "Total Daily Energy": return "⚡";
            case "Required PV Array": return "☀️";
            case "Battery Capacity": return "🔋";
            case "Inverter Size": return "🔄";
            case "Charge Controller": return "🎛️";
            default: return "📊";
        }
    }

    private Color getColorForMetric(String metric) {
        switch (metric) {
            case "Total Daily Energy": return SUCCESS_COLOR;
            case "Required PV Array": return WARNING_COLOR;
            case "Battery Capacity": return PRIMARY_COLOR;
            case "Inverter Size": return new Color(99, 102, 241);
            case "Charge Controller": return new Color(239, 68, 68);
            default: return TEXT_PRIMARY;
        }
    }

    private String getUnitForMetric(String metric) {
        if (metric.contains("Energy")) return "Watt-hours per day";
        if (metric.contains("PV Array") || metric.contains("Inverter")) return "Watts";
        if (metric.contains("Battery")) return "Amp-hours";
        if (metric.contains("Controller")) return "Amps";
        return "";
    }

    private String formatValue(String value, String metric) {
        try {
            String numericValue = value.replaceAll("[^0-9.,]", "").replace(",", "").trim();
            double num = Double.parseDouble(numericValue);
            return num >= 1000 ? df.format(num) : String.format("%.2f", num);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    // ---------- Calculation Helpers ----------

    private double estimateSystemCost(double pvWatts, double batteryAh, double inverterW, int voltage) {
        CountryConfig country = appliance.getCountry();

        double pvCost = pvWatts * country.getSolarPanelPricePerWatt();
        double batteryCost = batteryAh * country.getBatteryPricePerAh();
        double inverterCost = inverterW * country.getInverterPricePerWatt();
        double controllerCost = (pvWatts / voltage) * country.getControllerPricePerAmp();

        double wiringCostMultiplier = voltage == 12 ? 1.3 : voltage == 24 ? 1.0 : 0.8;
        double wiringCost = country.getWiringCostBase() * wiringCostMultiplier;

        double mountingMultiplier = pvWatts < 1000 ? 1.0 : pvWatts < 3000 ? 2.0 : 3.0;
        double mountingCost = country.getMountingCost() * mountingMultiplier;

        double miscCost = country.getWiringCostBase() * 0.67;

        return pvCost + batteryCost + inverterCost + controllerCost + wiringCost + mountingCost + miscCost;
    }

    private boolean isVoltageOptimal(double totalWh, int voltage) {
        if (totalWh < 1000) return voltage == 12;
        if (totalWh < 3000) return voltage == 24;
        return voltage == 48;
    }

    // ---------- Field Value Helpers ----------

    private String getFieldValue(JTextField field) {
        String text = field.getText().trim();
        return text.startsWith("e.g.,") ? "" : text;
    }

    private double getSafeDoubleValue(JTextField field, double defaultValue) {
        try {
            String text = getFieldValue(field);
            return text.isEmpty() ? defaultValue : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int getSafeIntValue(JTextField field, int defaultValue) {
        try {
            String text = getFieldValue(field);
            return text.isEmpty() ? defaultValue : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ---------- Dialog Helpers ----------

    private void showCountrySelectionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Select Country/Region", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CARD_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel("Country/Region:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(label, gbc);

        JComboBox<CountryConfig> countrySelector = new JComboBox<>(CountryConfig.values());
        countrySelector.setSelectedItem(appliance.getCountry());
        countrySelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countrySelector.setBackground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 0;
        contentPanel.add(countrySelector, gbc);

        dialog.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BACKGROUND);

        JButton applyBtn = createStyledButton("Apply", SUCCESS_COLOR, new Color(22, 163, 74));
        applyBtn.setPreferredSize(new Dimension(100, 35));
        applyBtn.addActionListener(e -> {
            CountryConfig selected = (CountryConfig) countrySelector.getSelectedItem();

            // Apply selection to all appliances in the app so currency and cost outputs update globally
            if (mainPage != null && mainPage.getAppliances() != null) {
                for (Model.Appliance a : mainPage.getAppliances()) {
                    a.setCountry(selected);
                }
            }

            // Also ensure the current appliance is updated
            appliance.setCountry(selected);

            showSuccess("Country changed to: " + selected.getDisplayName());

            // Recalculate results for the current appliance so displayed currency/code updates.
            // Preserve the user's current tab to avoid unexpected navigation.
            int prevTab = tabbedPane.getSelectedIndex();
            try {
                calculateThisAppliance();
            } catch (Exception ex) {
                // calculateThisAppliance already handles exceptions; ignore here
            }
            // restore previous tab if it wasn't the Results tab
            if (prevTab != 2) {
                tabbedPane.setSelectedIndex(prevTab);
            }

            dialog.dispose();
        });

        JButton cancelBtn = createStyledButton("Cancel", new Color(100, 116, 139), new Color(71, 85, 105));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(applyBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleBackNavigation() {
        if (hasUnsavedChanges) {
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Do you want to save before leaving?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                if (saveChanges()) {
                    mainPage.showApplianceListPanel();
                }
            } else if (result == JOptionPane.NO_OPTION) {
                mainPage.showApplianceListPanel();
            }
        } else {
            mainPage.showApplianceListPanel();
        }
    }

    // ---------- Message Helpers ----------

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ============================================================================
    // INNER CLASSES
    // ============================================================================

    /**
     * Helper class for formatting colored text tags in analysis
     */
    private class ColorFormatter {
        String tag(String text, String color) {
            return String.format("[%s]%s[/%s]", color, text, color);
        }
    }
}