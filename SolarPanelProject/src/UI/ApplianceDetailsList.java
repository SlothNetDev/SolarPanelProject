package UI;

import Model.Appliance;
import Model.SolarCalculator;
import Utils.ProjectManager;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*; // ADD THIS IMPORT for DocumentListener
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Component; // Still need this for Swing components
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;

public class ApplianceDetailsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Appliance appliance;
    private MainPage mainPage;

    // Form fields
    private JTextField nameField, wattsField, qtyField, hoursField;
    private JTextField pshField, dodField, daysField;
    private JComboBox<String> voltageCombo;
    private JTabbedPane tabbedPane;

    // Results display
    private JPanel resultsCard;
    private JPanel resultsContentPanel; // To store the actual results content

    // Track unsaved changes
    private boolean hasUnsavedChanges = false;
    private String originalBasicData = "";
    private String originalSolarData = "";

    // Color palette matching your preferred style
    private final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private final Color CARD_BACKGROUND = Color.WHITE;
    private final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    private DecimalFormat df = new DecimalFormat("#,##0.00");

    public ApplianceDetailsPanel(Appliance appliance, MainPage mainPage) {
        this.appliance = appliance;
        this.mainPage = mainPage;
        initializeUI();
        // Store original data after UI is initialized
        storeOriginalData();
        add(createToolbar(), BorderLayout.NORTH);add(createToolbar(), BorderLayout.NORTH);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Header
        JLabel header = new JLabel("⚙️ Appliance Details & Solar Parameters", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        add(header, BorderLayout.NORTH);

        // Create main content with tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("📝 Basic Details", createBasicDetailsPanel());
        tabbedPane.addTab("☀️ Solar Parameters", createSolarParametersPanel());
        tabbedPane.addTab("📊 Results", createResultsPanel());

        // Add tab change listener for auto-save
        tabbedPane.addChangeListener(e -> {
            if (hasUnsavedChanges) {
                promptSaveChanges();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton saveBtn = createStyledButton("💾 Save Changes", SUCCESS_COLOR, new Color(22, 163, 74));
        JButton calculateBtn = createStyledButton("🔢 Calculate This Appliance", PRIMARY_COLOR, new Color(37, 99, 235));
        JButton backBtn = createStyledButton("← Back to List", new Color(100, 116, 139), new Color(71, 85, 105));

        navPanel.add(saveBtn);
        navPanel.add(calculateBtn);
        navPanel.add(backBtn);
        add(navPanel, BorderLayout.SOUTH);

        // Load data and set up actions
        loadApplianceData();

        saveBtn.addActionListener(e -> saveChanges());
        calculateBtn.addActionListener(e -> calculateThisAppliance());
        backBtn.addActionListener(e -> {
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
                // Cancel - stay on current panel
            } else {
                mainPage.showApplianceListPanel();
            }
        });

        // Add change listeners to track unsaved changes
        addChangeListeners();
    }

    private void addChangeListeners() {
        // Listeners for basic details
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
        // Wait for fields to be initialized
        if (nameField != null && voltageCombo != null) {
            originalBasicData = nameField.getText() + wattsField.getText() + qtyField.getText() + hoursField.getText();
            originalSolarData = pshField.getText() + dodField.getText() + daysField.getText() + voltageCombo.getSelectedItem();
        }
    }

    private void checkForChanges() {
        if (nameField == null || voltageCombo == null) return;

        String currentBasicData = nameField.getText() + wattsField.getText() + qtyField.getText() + hoursField.getText();
        String currentSolarData = pshField.getText() + dodField.getText() + daysField.getText() + voltageCombo.getSelectedItem();

        hasUnsavedChanges = !currentBasicData.equals(originalBasicData) || !currentSolarData.equals(originalSolarData);

        // Update tab titles to show unsaved changes
        updateTabTitles();
    }

    private void updateTabTitles() {
        if (tabbedPane == null) return;

        String basicTitle = hasUnsavedChanges ? "📝 Basic Details ●" : "📝 Basic Details";
        String solarTitle = hasUnsavedChanges ? "☀️ Solar Parameters ●" : "☀️ Solar Parameters";

        tabbedPane.setTitleAt(0, basicTitle);
        tabbedPane.setTitleAt(1, solarTitle);
    }

    private void promptSaveChanges() {
        int result = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Would you like to save them now?",
                "Unsaved Changes Detected",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            if (saveChanges()) {
                // Changes saved, continue with tab switch
            } else {
                // Save failed, stay on current tab
                tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() == 0 ? 0 : 1);
            }
        } else {
            // User chose not to save, discard changes and continue
            hasUnsavedChanges = false;
            updateTabTitles();
        }
    }

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

        // Create export button panel
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportPanel.setBackground(BACKGROUND_COLOR);
        exportPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // 1. RED EXPORT TO PDF BUTTON (Using the new red colors)
        JButton exportBtn1 = createStyledButton("📄 Export to PDF", new Color(220, 53, 69), new Color(200, 35, 51));
        exportBtn1.setPreferredSize(new Dimension(150, 35));
        exportBtn1.addActionListener(e -> exportResultsToPDF());
        exportPanel.add(exportBtn1);

        // 2. EXPORT TO CSV BUTTON (Using the original blue color)
        JButton exportCvs = createStyledButton("📄 Export to CSV", new Color(0, 102, 204), new Color(0, 76, 153));
        exportCvs.setPreferredSize(new Dimension(150, 35));
        // Listener now opens the file dialog and calls the handler method
        exportCvs.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save CSV");
            fileChooser.setSelectedFile(new File("SolarCalculator_Results.csv"));

            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File csvFile = fileChooser.getSelectedFile();
                exportToCSVHandler(csvFile);
            }
        });
        exportPanel.add(exportCvs);


        // Main results content (No changes here)
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

        // Create a wrapper panel for the results content
        resultsContentPanel = new JPanel(new BorderLayout());
        resultsContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Add components to main panel
        panel.add(exportPanel, BorderLayout.NORTH);
        panel.add(resultsContentPanel, BorderLayout.CENTER);

        // Initial message
        displayWelcomeMessage();

        return panel;
    }

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

        if (inputComponent instanceof JTextField) {
            JTextField textField = (JTextField) inputComponent;
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textField.setHorizontalAlignment(JTextField.CENTER); // Center align text
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            textField.setPreferredSize(new Dimension(0, 38));

            if (placeholder != null) {
                textField.setForeground(Color.GRAY);
                textField.setText(placeholder);
                textField.addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        if (textField.getText().equals(placeholder)) {
                            textField.setText("");
                            textField.setForeground(Color.BLACK);
                        }
                    }
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        if (textField.getText().isEmpty()) {
                            textField.setForeground(Color.GRAY);
                            textField.setText(placeholder);
                        }
                    }
                });
            }
        } else if (inputComponent instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>) inputComponent;
            comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            comboBox.setPreferredSize(new Dimension(0, 38));
            comboBox.setBackground(Color.WHITE);
            // Center align combo box text too
            ((JLabel) comboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        }

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(inputComponent, BorderLayout.CENTER);

        return fieldPanel;
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

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
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

    private void loadApplianceData() {
        // Basic details
        nameField.setText(appliance.getName());
        wattsField.setText(String.valueOf(appliance.getWatts()));
        qtyField.setText(String.valueOf(appliance.getQuantity()));
        hoursField.setText(String.valueOf(appliance.getHoursPerDay()));

        // Solar parameters - only load if they have been set (not default values)
        if (appliance.getPeakSunHours() != 5.0) {
            pshField.setText(String.valueOf(appliance.getPeakSunHours()));
        } else {
            pshField.setText("e.g., 5.0");
            pshField.setForeground(Color.GRAY);
        }
        if (appliance.getDepthOfDischarge() != 50.0) {
            dodField.setText(String.valueOf(appliance.getDepthOfDischarge()));
        } else {
            dodField.setText("e.g., 50");
            dodField.setForeground(Color.GRAY);
        }
        if (appliance.getDaysOfAutonomy() != 2) {
            daysField.setText(String.valueOf(appliance.getDaysOfAutonomy()));
        } else {
            daysField.setText("e.g., 2");
            daysField.setForeground(Color.GRAY);
        }
        if (appliance.getSystemVoltage() != 12) {
            voltageCombo.setSelectedItem(String.valueOf(appliance.getSystemVoltage()));
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
                JOptionPane.showMessageDialog(this,
                        "Please fill in all basic appliance details.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Save basic details
            appliance.setName(name);
            appliance.setWatts(Double.parseDouble(wattsText));
            appliance.setQuantity(Integer.parseInt(qtyText));
            appliance.setHoursPerDay(Double.parseDouble(hoursText));

            // Save solar parameters only if they're filled (not placeholder)
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

            // Update original data and clear unsaved changes flag
            storeOriginalData();
            hasUnsavedChanges = false;
            updateTabTitles();

            JOptionPane.showMessageDialog(this,
                    "✓ Appliance details saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numeric values in all fields.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving changes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void calculateThisAppliance() {
        try {
            // Auto-save changes before calculation
            if (hasUnsavedChanges) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Save changes before calculating?",
                        "Unsaved Changes",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    if (!saveChanges()) {
                        return; // Save failed, don't proceed with calculation
                    }
                }
            }

            // Validate that solar parameters are set
            String pshText = getFieldValue(pshField);
            String dodText = getFieldValue(dodField);
            String daysText = getFieldValue(daysField);

            if (pshText.isEmpty() || dodText.isEmpty() || daysText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please set all solar parameters in the 'Solar Parameters' tab before calculating.",
                        "Missing Solar Parameters",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Perform calculation using the appliance's solar parameters
            SolarCalculator calc = mainPage.getCalculator();

            double totalWh = appliance.energyPerDayWh();
            double pvWatts = calc.requiredPvWatts(totalWh, appliance.getPeakSunHours());
            double batteryAh = calc.requiredBatteryAh(totalWh, appliance.getDaysOfAutonomy(),
                    appliance.getDepthOfDischarge(), appliance.getSystemVoltage());
            double inverterW = calc.recommendedInverterW(appliance.getWatts() * appliance.getQuantity());
            double controllerA = calc.recommendedControllerA(pvWatts, appliance.getSystemVoltage());

            // Generate intelligent analysis
            String analysis = generateSystemAnalysis(totalWh, pvWatts, batteryAh, inverterW, controllerA);

            // Format results
            String resultsText = String.format(
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

            // Update the results display and AUTO-REDIRECT to results tab
            updateResultsDisplay(resultsText);
            tabbedPane.setSelectedIndex(2); // Switch to Results tab

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error calculating: " + ex.getMessage(),
                    "Calculation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generateSystemAnalysis(double totalWh, double pvWatts, double batteryAh, double inverterW, double controllerA) {
        StringBuilder analysis = new StringBuilder();

        // --- Helper for formatting based on color ---
        // NOTE: Your UI must parse these tags: [RED], [YELLOW], [GREEN]
        var format = new Object() {
            String tag(String text, String color) {
                return String.format("[%s]%s[/%s]", color, text, color);
            }
        };

        // --- Energy Consumption Analysis ---
        analysis.append("--- 🔋 ENERGY DEMAND ---\n");
        if (totalWh < 500) {
            analysis.append(format.tag("• **Very Low Load:** Ideal for small devices. Perfect for portable or small off-grid kits.", "GREEN") + "\n");
        } else if (totalWh < 2000) {
            analysis.append(format.tag("• **Moderate Load:** Suitable for small appliances. Excellent for RV, camping, or small cabin systems.", "YELLOW") + "\n");
        } else if (totalWh < 5000) {
            analysis.append(format.tag("• **High Load:** Can power multiple appliances simultaneously. Suitable for home office or small household use.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Heavy Load:** Requires a robust, high-capacity system. Ideal for full residential or commercial applications.", "RED") + "\n");
        }

        // --- Solar Panel Analysis ---
        analysis.append("\n--- ☀️ PV ARRAY SIZING ---\n");
        if (pvWatts < 300) {
            analysis.append(format.tag("• **Small Array:** Equivalent to 1-2 standard 300W panels. Easy installation.", "GREEN") + "\n");
        } else if (pvWatts < 1000) {
            analysis.append(format.tag("• **Medium Array:** Requires 3-4 panels. Suitable for most standard residential rooftops.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Large Array:** Requires professional installation planning. Consider split arrays for optimized exposure.", "RED") + "\n");
        }

        // --- Battery System Analysis ---
        analysis.append("\n--- ⚡ BATTERY STORAGE ---\n");
        analysis.append(String.format("• **Capacity:** %.0f Ah required for %d days of autonomy.\n", batteryAh, appliance.getDaysOfAutonomy()));

        if (batteryAh < 150 && appliance.getDepthOfDischarge() <= 50) {
            analysis.append(format.tag("• **System Type:** Small, daily-cycling setup. AGM or basic Lithium recommended.", "GREEN") + "\n");
        } else if (batteryAh < 500) {
            analysis.append(format.tag("• **System Type:** Medium bank for extended use. Lithium (LiFePO4) is strongly recommended.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **System Type:** Large backup bank. Professional battery management system (BMS) is essential.", "RED") + "\n");
        }

        // --- Inverter Analysis ---
        analysis.append("\n--- 🔌 INVERTER SELECTION ---\n");
        analysis.append(String.format("• **Required Size:** %.0f W (Includes 25%% safety overhead).\n", inverterW));
        if (inverterW < 1000) {
            analysis.append(format.tag("• **Recommendation:** Use Pure Sine Wave for all sensitive electronics. Portable units are sufficient.", "GREEN") + "\n");
        } else if (inverterW < 3000) {
            analysis.append(format.tag("• **Recommendation:** Residential-grade central inverter. Can handle most major household appliances.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Recommendation:** Heavy-duty, possibly grid-tie hybrid inverter required. Consider split-phase setup.", "RED") + "\n");
        }

        // --- System Voltage Analysis ---
        analysis.append("\n--- 🎯 VOLTAGE OPTIMIZATION ---\n");
        int voltage = appliance.getSystemVoltage();
        analysis.append(String.format("• **Selected Voltage:** %dV System.\n", voltage));

        analysis.append("• **Best For:**\n");
        if (voltage == 12) {
            analysis.append("  - Small mobile applications (RV/Boats).\n");
            analysis.append("  - Very short cable runs (under 10ft).\n");
        } else if (voltage == 24) {
            analysis.append("  - Balanced power needs (Medium homes/cabins). [YELLOW]Better efficiency than 12V.[/YELLOW]\n");
        } else {
            analysis.append("  - High-power, full-home or commercial use. [GREEN]Maximum efficiency and minimal loss.[/GREEN]\n");
        }

        // --- Charge Controller Analysis ---
        analysis.append("\n--- 🎛️ CHARGE CONTROLLER ---\n");
        analysis.append(String.format("• **Required Rating:** %.0f A MPPT Controller.\n", controllerA));
        if (controllerA < 30) {
            analysis.append(format.tag("• **Type:** Standard, compact MPPT controller is sufficient and cost-effective.", "GREEN") + "\n");
        } else {
            analysis.append(format.tag("• **Type:** Heavy-duty MPPT controller required. Ensure it supports the panel voltage configuration.", "YELLOW") + "\n");
        }

        // --- Overall Recommendation ---
        analysis.append("\n--- 🌟 FINAL RECOMMENDATION ---\n");
        if (totalWh < 1000 && voltage <= 24) {
            analysis.append(format.tag("This is an **EXCELLENT, well-balanced setup** for portable or small off-grid applications. High cost-effectiveness.", "GREEN") + "\n");
        } else if (totalWh < 5000) {
            analysis.append(format.tag("This is a **SOLID residential system**. Plan for future expansion capacity now.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("This is a **POWERFUL system**. Professional design and installation are strongly recommended.", "RED") + "\n");
        }

        return analysis.toString();
    }

    private void exportResultsToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF");
        fileChooser.setSelectedFile(new File("SolarCalculator_Results.pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File pdfFile = fileChooser.getSelectedFile();

            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                document.open();

                // Modern font palette
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 20, com.itextpdf.text.Font.BOLD,
                        new com.itextpdf.text.BaseColor(30, 41, 59) // Deep modern gray
                );

                com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD,
                        new com.itextpdf.text.BaseColor(37, 99, 235) // Blue accent
                );

                com.itextpdf.text.Font labelFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD,
                        new com.itextpdf.text.BaseColor(55, 65, 81)
                );

                com.itextpdf.text.Font valueFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL,
                        new com.itextpdf.text.BaseColor(17, 24, 39)
                );

                com.itextpdf.text.Font analysisFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL,
                        new com.itextpdf.text.BaseColor(75, 85, 99)
                );

                // Header section
                Paragraph title = new Paragraph("☀️ Solar System Analysis Report\n\n", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                // Metadata (Date, Appliance Name)
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
                String dateTime = LocalDateTime.now().format(formatter);
                document.add(new Paragraph("Generated on: " + dateTime + "\n", valueFont));
                document.add(new Paragraph("Appliance: " + appliance.getName() + "\n\n", valueFont));

                // Divider line
                document.add(new Paragraph("──────────────────────────────────────────────\n", labelFont));

                // Extract and structure data
                boolean inAnalysis = false;
                boolean inMainMetrics = false;

                document.add(new Paragraph("⚙️ Required Components\n\n", sectionFont));
                for (Component comp : resultsCard.getComponents()) {
                    if (comp instanceof JLabel) {
                        JLabel label = (JLabel) comp;
                        String text = label.getText();

                        // Detect the "SYSTEM ANALYSIS" separator
                        if (text.contains("System Analysis")) {
                            document.add(new Paragraph("\n📊 System Analysis & Recommendations\n\n", sectionFont));
                            inAnalysis = true;
                            continue;
                        }

                        // Format structured lines
                        if (text.contains(":") && !inAnalysis) {
                            String[] parts = text.split(":");
                            if (parts.length == 2) {
                                document.add(new Paragraph(parts[0].trim() + ": ", labelFont));
                                document.add(new Paragraph(parts[1].trim() + "\n", valueFont));
                            }
                        } else if (inAnalysis) {
                            if (text.trim().isEmpty()) {
                                document.add(new Paragraph(" "));
                            } else {
                                document.add(new Paragraph("• " + text, analysisFont));
                            }
                        }
                    }
                }

                // Outro section
                document.add(new Paragraph("\n──────────────────────────────────────────────\n", labelFont));
                Paragraph footer = new Paragraph(
                        "💡 This report was generated by Solar Calculator.\n" +
                                "Designed to help you plan efficient and sustainable off-grid systems.",
                        new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.ITALIC,
                                new com.itextpdf.text.BaseColor(100, 116, 139))
                );
                footer.setAlignment(Element.ALIGN_CENTER);
                document.add(footer);

                document.close();
                JOptionPane.showMessageDialog(this,
                        "✅ Results exported successfully to PDF!\n" + pdfFile.getAbsolutePath(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "❌ Error exporting PDF: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // New wrapper method to handle the exception safely
    private void exportToCSVHandler(File file) {
        try {
            _exportToCSV(file);
            JOptionPane.showMessageDialog(null,
                    "✅ Results exported successfully to CSV!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "❌ Error exporting CSV: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void _exportToCSV(File file) throws IOException {

        // Ensure the file has the .csv extension if the user forgot it
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // --- PRE-CALCULATIONS ---
            double totalWh = appliance.energyPerDayWh();
            SolarCalculator calc = mainPage.getCalculator();
            double pvWatts = calc.requiredPvWatts(totalWh, appliance.getPeakSunHours());
            double batteryAh = calc.requiredBatteryAh(totalWh, appliance.getDaysOfAutonomy(), appliance.getDepthOfDischarge(), appliance.getSystemVoltage());
            double inverterW = calc.recommendedInverterW(appliance.getWatts() * appliance.getQuantity());
            double controllerA = calc.recommendedControllerA(pvWatts, appliance.getSystemVoltage());
            String analysis = generateSystemAnalysis(totalWh, pvWatts, batteryAh, inverterW, controllerA);


            // --- WRITE HEADER & BASIC DETAILS ---
            writer.println("Solar System Calculation Results");
            writer.println("Appliance:," + appliance.getName());
            writer.println("Export Date:," + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();

            writer.println("BASIC DETAILS");
            writer.println("Parameter,Value,Unit");
            writer.println("Appliance Name," + appliance.getName() + ",");
            writer.println("Power," + appliance.getWatts() + ",Watts");
            writer.println("Quantity," + appliance.getQuantity() + ",");
            writer.println("Hours per Day," + appliance.getHoursPerDay() + ",hours");
            writer.println();

            // --- WRITE SOLAR PARAMETERS ---
            writer.println("SOLAR PARAMETERS");
            writer.println("Parameter,Value,Unit");
            writer.println("Peak Sun Hours," + appliance.getPeakSunHours() + ",hours");
            writer.println("Depth of Discharge," + appliance.getDepthOfDischarge() + ",%");
            writer.println("Days of Autonomy," + appliance.getDaysOfAutonomy() + ",days");
            writer.println("System Voltage," + appliance.getSystemVoltage() + ",V");
            writer.println();

            // --- WRITE CALCULATION RESULTS ---
            writer.println("CALCULATION RESULTS");
            writer.println("Component,Value,Unit");

            writer.println("Total Daily Energy," + String.format("%.2f", totalWh) + ",Wh/day");
            writer.println("Required PV Array," + String.format("%.2f", pvWatts) + ",W");
            writer.println("Battery Capacity," + String.format("%.2f", batteryAh) + ",Ah");
            writer.println("Inverter Size," + String.format("%.2f", inverterW) + ",W");
            writer.println("Charge Controller," + String.format("%.2f", controllerA) + ",A");
            writer.println();

            // --- WRITE ANALYSIS SECTION ---
            writer.println("SYSTEM ANALYSIS");
            writer.println("Category,Recommendation");

            String[] analysisLines = analysis.split("\n");
            for (String line : analysisLines) {
                if (line.trim().isEmpty()) continue;

                // Handle section headers/metrics: e.g., "🔋 ENERGY CONSUMPTION:"
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        writer.println(parts[0].trim() + "," + parts[1].trim());
                    }
                } else {
                    // Handle sub-bullet points (e.g., "• Good for RV...")
                    // We use "General" as a fallback category if no specific header is present
                    writer.println("General," + line.trim());
                }
            }
        }
    }



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

        // --- Define Aesthetic Colors (Must be defined as constants in your class) ---
        // Example definitions (adjust as needed for aesthetics):
        final Color CARD_HEADER_BG = new Color(59, 130, 246, 15); // Light Blue
        final Color ANALYSIS_RED_BG = new Color(220, 53, 69);     // Vibrant Red
        final Color ANALYSIS_YELLOW_BG = new Color(255, 193, 7);   // Vibrant Yellow
        final Color ANALYSIS_GREEN_BG = new Color(40, 167, 69);   // Vibrant Green
        final Color TEXT_PRIMARY = new Color(33, 37, 41);         // Dark Gray Text
        final Color TEXT_WHITE = Color.WHITE;

        // Add results header
        JPanel resultsHeader = new JPanel(new BorderLayout());
        resultsHeader.setBackground(CARD_HEADER_BG);
        resultsHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel summaryLabel = new JLabel("Individual Appliance Analysis - " + appliance.getName());
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        summaryLabel.setForeground(PRIMARY_COLOR); // Assuming PRIMARY_COLOR is defined elsewhere

        resultsHeader.add(summaryLabel, BorderLayout.WEST);
        resultsCard.add(resultsHeader);

        // Parse and display formatted results
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
                // ⬇️ MODIFIED ANALYSIS SECTION ⬇️
                if (line.startsWith("---")) {
                    // New header style for the analysis sub-sections
                    addSectionHeader(line.replace("---", "").trim());
                } else if (!line.trim().isEmpty()) {
                    // Call the new styling helper method
                    JPanel analysisPanel = createStyledAnalysisPanel(
                            line,
                            ANALYSIS_RED_BG, ANALYSIS_YELLOW_BG, ANALYSIS_GREEN_BG,
                            TEXT_PRIMARY, TEXT_WHITE
                    );

                    if (analysisPanel != null) {
                        analysisPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        resultsCard.add(analysisPanel);
                    }
                } else {
                    resultsCard.add(Box.createVerticalStrut(5));
                }
                // ⬆️ END MODIFIED ANALYSIS SECTION ⬆️

            } else if (line.contains(":")) {
                // ... (Existing logic for metrics and parameters remains the same) ...
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String label = parts[0].trim();
                    String value = parts[1].trim();

                    if (isMainMetric(label)) {
                        if (!mainMetricsDisplayed) {
                            addSectionHeader("Required Components");
                            mainMetricsDisplayed = true;
                        }
                        JPanel metricPanel = createMetricPanel(label, value);
                        resultsCard.add(metricPanel);
                        resultsCard.add(Box.createVerticalStrut(8));
                    } else if (isInputParameter(label)) {
                        if (!analysisSection) {
                            addSectionHeader("Input Parameters");
                            analysisSection = true; // Use a different flag if necessary
                        }
                        JPanel paramPanel = createParameterPanel(label, value);
                        resultsCard.add(paramPanel);
                        resultsCard.add(Box.createVerticalStrut(5));
                    }
                }
            }
        }

        resultsCard.revalidate();
        resultsCard.repaint();
    }

    /**
     * Parses the analysis string for color tags and creates a styled JPanel for the line.
     */
    private JPanel createStyledAnalysisPanel(String rawLine, Color redBG, Color yellowBG, Color greenBG, Color primaryFG, Color whiteFG) {
        JPanel linePanel = new JPanel(new BorderLayout());

        Color bgColor = resultsCard.getBackground(); // Default background
        Color fgColor = primaryFG;                 // Default foreground
        int fontStyle = Font.PLAIN;

        String content = rawLine;

        // --- 1. Check for Color Tags ---
        if (rawLine.contains("[RED]")) {
            bgColor = redBG;
            fgColor = whiteFG;
            content = rawLine.replace("[RED]", "").replace("[/RED]", "");
        } else if (rawLine.contains("[YELLOW]")) {
            bgColor = yellowBG;
            fgColor = primaryFG; // Black text on yellow background
            content = rawLine.replace("[YELLOW]", "").replace("[/YELLOW]", "");
        } else if (rawLine.contains("[GREEN]")) {
            bgColor = greenBG;
            fgColor = whiteFG;
            content = rawLine.replace("[GREEN]", "").replace("[/GREEN]", "");
        }

        // --- 2. Check for Bold Tags (e.g., **Very Low Load**) ---
        if (content.contains("**")) {
            // Simple heuristic: If the whole line is meant to be bold (like the main recommendation)
            fontStyle = Font.BOLD;
            content = content.replace("**", ""); // Remove markdown stars
        }

        // --- 3. Set Panel and Label Style ---
        linePanel.setBackground(bgColor);

        JLabel analysisLine = new JLabel(content);
        analysisLine.setFont(new Font("Segoe UI", fontStyle, 13));
        analysisLine.setForeground(fgColor);

        // Add margin/padding to the panel, not the label
        linePanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // Adjust indentation for bullet points/lists
        if (content.trim().startsWith("•")) {
            // Use a slight right-alignment to give space for the bullet
            linePanel.setBorder(BorderFactory.createEmptyBorder(3, 15, 3, 15));
        } else if (content.trim().startsWith("-")) {
            // Deeper indentation for sub-points
            linePanel.setBorder(BorderFactory.createEmptyBorder(2, 30, 2, 15));
        }

        linePanel.add(analysisLine, BorderLayout.WEST);

        return linePanel;
    }

    // NOTE: You must ensure helper methods like addSectionHeader, isMainMetric,
    // createMetricPanel, isInputParameter, and createParameterPanel exist
    // and are correctly defined in your class.

    private boolean isMainMetric(String label) {
        return label.equals("Total Daily Energy") || label.equals("Required PV Array") ||
                label.equals("Battery Capacity") || label.equals("Inverter Size") ||
                label.equals("Charge Controller");
    }

    private boolean isInputParameter(String label) {
        return label.equals("System Voltage") || label.equals("Peak Sun Hours") ||
                label.equals("Depth of Discharge") || label.equals("Days of Autonomy");
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

    private JPanel createParameterPanel(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel nameLabel = new JLabel("• " + label);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);

        return panel;
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

    private String getFieldValue(JTextField field) {
        String text = field.getText().trim();
        return text.startsWith("e.g.,") ? "" : text;
    }

    //Save Json
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(new Color(245, 245, 245));

        JButton saveBtn = new JButton("💾 Save Project");
        saveBtn.addActionListener((ActionEvent e) -> {
            // ✅ Now it uses the mainPage's appliance list (which has real data)
            ProjectManager.saveProject(this, mainPage.getAppliances());
        });

        JButton loadBtn = new JButton("📂 Load Project");
        loadBtn.addActionListener((ActionEvent e) -> {
            List<Appliance> loaded = ProjectManager.loadProject(this);
            if (loaded != null) {
                mainPage.getAppliances().clear();
                mainPage.getAppliances().addAll(loaded);
                JOptionPane.showMessageDialog(this,
                        "Loaded " + loaded.size() + " appliances.",
                        "Load Successful", JOptionPane.INFORMATION_MESSAGE);
                mainPage.showApplianceListPanel(); // Refresh after load
            }
        });

        toolbar.add(saveBtn);
        toolbar.add(loadBtn);
        return toolbar;
    }

}