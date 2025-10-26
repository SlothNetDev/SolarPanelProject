package UI;

import Model.Appliance;
import Model.CountryConfig;
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

/**
 * The ApplianceDetailsPanel class is a graphical user interface component that extends JPanel
 * and is used to display and manage the details of an appliance. It integrates various UI
 * features and functionalities for displaying, editing, and analyzing appliance data,
 * including both basic and solar-specific parameters, results visualization, and user interaction.
 *
 * This panel provides multiple tabs for organizing the data, such as basic details, solar
 * parameters, and calculated results. It also includes mechanisms for validating input, detecting
 * unsaved changes, and exporting results in PDF or CSV formats.
 *
 * Key Functions:
 * - Display and edit appliance details and parameters.
 * - Perform calculations and analytics for energy consumption and system designs.
 * - Export results to files and handle user interactions for data management.
 *
 * Constants:
 * - BACKGROUND_COLOR: Default background color for the panel.
 * - CARD_BACKGROUND: Background color for individual cards in the UI.
 * - PRIMARY_COLOR: Primary UI color used for highlights.
 * - SUCCESS_COLOR: Color indicating success states in the UI.
 * - WARNING_COLOR: Color indicating warning states in the UI.
 * - TEXT_PRIMARY: Primary text color used for standard text.
 * - TEXT_SECONDARY: Secondary text color used for supporting text.
 * - BORDER_COLOR: Default border color for card elements.
 *
 * Dependencies:
 * - Manipulates data from the Appliance object.
 * - Interacts with the MainPage for application-wide context.
 * - Utilizes various JPanel components for layout and display.
 */
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
            String analysiss = generateSystemSummaryAnalysis(totalWh, pvWatts, batteryAh, inverterW, controllerA);

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

    private String generateSystemAnalysis(double totalWh, double pvWatts, double batteryAh,
                                          double inverterW, double controllerA) {
        StringBuilder analysis = new StringBuilder();
        int voltage = appliance.getSystemVoltage();
        double psh = appliance.getPeakSunHours();
        double dod = appliance.getDepthOfDischarge();
        int days = appliance.getDaysOfAutonomy();

        //country pricing

        CountryConfig country = appliance.getCountry();

        // Helper for color formatting
        var format = new Object() {
            String tag(String text, String color) {
                return String.format("[%s]%s[/%s]", color, text, color);
            }
        };

        // ========================================================================
        // 1. ENERGY DEMAND ANALYSIS
        // ========================================================================
        analysis.append("--- 🔋 ENERGY DEMAND PROFILE ---\n");
        analysis.append(String.format("• **Daily Consumption:** %.0f Wh/day for %s\n",
                totalWh, appliance.getName()));

        double monthlyKwh = (totalWh * 30) / 1000;
        double monthlyBill = monthlyKwh * country.getElectricityRatePerKwh();

        analysis.append(String.format("• **Monthly Equivalent:** ~%.1f kWh (comparable to %s%.0f bill at %s%.2f/kWh)\n",
                monthlyKwh,
                country.getCurrencySymbol(), monthlyBill,
                country.getCurrencySymbol(), country.getElectricityRatePerKwh()));

        if (totalWh < 300) {
            analysis.append(format.tag("• **Load Type:** Minimal - Perfect for LED lighting, phone charging, small electronics.", "GREEN") + "\n");
            analysis.append(format.tag("• **Use Case:** Camping, emergency kits, van life essentials, or remote sensors.", "GREEN") + "\n");
        } else if (totalWh < 800) {
            analysis.append(format.tag("• **Load Type:** Light - Can power laptop, lights, fans, and small appliances.", "GREEN") + "\n");
            analysis.append(format.tag("• **Use Case:** Remote work setup, weekend cabin, or RV daily needs.", "GREEN") + "\n");
        } else if (totalWh < 2000) {
            analysis.append(format.tag("• **Load Type:** Moderate - Handles refrigerator, TV, microwave (not simultaneously).", "YELLOW") + "\n");
            analysis.append(format.tag("• **Use Case:** Off-grid home office, tiny house, or backup for essential circuits.", "YELLOW") + "\n");
        } else if (totalWh < 5000) {
            analysis.append(format.tag("• **Load Type:** High - Multiple appliances, washer, power tools can run with planning.", "YELLOW") + "\n");
            analysis.append(format.tag("• **Use Case:** Full-time off-grid living (small household) or comprehensive backup system.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Load Type:** Very High - Equivalent to typical household with AC, electric heating, or workshop.", "RED") + "\n");
            analysis.append(format.tag("• **Use Case:** Large residential system, farm operations, or small commercial applications.", "RED") + "\n");
        }

        // ========================================================================
        // 2. SOLAR PANEL ARRAY ANALYSIS
        // ========================================================================
        analysis.append("\n--- ☀️ PHOTOVOLTAIC ARRAY DESIGN ---\n");
        analysis.append(String.format("• **Required Capacity:** %.0f W peak power\n", pvWatts));

        // Panel configuration examples
        int panels300w = (int) Math.ceil(pvWatts / 300.0);
        int panels400w = (int) Math.ceil(pvWatts / 400.0);
        double roofArea = pvWatts / 150; // ~150W per sq meter typical

        analysis.append(String.format("• **Configuration Options:**\n"));
        analysis.append(String.format("  - %d× 300W panels (~%.1f m²) OR\n", panels300w, roofArea));
        analysis.append(String.format("  - %d× 400W panels (~%.1f m²)\n", panels400w, roofArea * 0.75));

        // Daily production estimate
        double dailyProduction = pvWatts * psh * 0.85; // 85% system efficiency
        double productionRatio = dailyProduction / totalWh;

        analysis.append(String.format("• **Daily Production:** ~%.0f Wh with %.1f peak sun hours\n",
                dailyProduction, psh));

        if (productionRatio >= 1.5) {
            analysis.append(format.tag("• **Production Status:** EXCELLENT - 50%+ surplus! Great for winter/cloudy days.", "GREEN") + "\n");
            analysis.append(format.tag("• **Recommendation:** Consider reducing array size to save costs, or add more loads.", "GREEN") + "\n");
        } else if (productionRatio >= 1.2) {
            analysis.append(format.tag("• **Production Status:** OPTIMAL - 20% safety margin for seasonal variation.", "GREEN") + "\n");
            analysis.append(format.tag("• **Recommendation:** Well-balanced system. No changes needed.", "GREEN") + "\n");
        } else if (productionRatio >= 1.0) {
            analysis.append(format.tag("• **Production Status:** ADEQUATE - Meets needs but minimal margin for cloudy days.", "YELLOW") + "\n");
            analysis.append(format.tag("• **Recommendation:** Consider 15-20% larger array for reliability in winter.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Production Status:** ⚠️ INSUFFICIENT - Will not meet daily demand!", "RED") + "\n");
            analysis.append(format.tag("• **Recommendation:** CRITICAL - Increase array by " +
                    String.format("%.0f%% minimum", (1/productionRatio - 1) * 100) +
                    " or reduce loads.", "RED") + "\n");
        }

        // Installation considerations
        if (pvWatts < 600) {
            analysis.append(format.tag("• **Installation:** Simple DIY - Can mount on RV roof, portable frames, or small ground mount.", "GREEN") + "\n");
        } else if (pvWatts < 2000) {
            analysis.append(format.tag("• **Installation:** Moderate complexity - Rooftop or ground mount. DIY-friendly with proper planning.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Installation:** Professional recommended - Large array requires structural assessment and code compliance.", "RED") + "\n");
        }

        // ========================================================================
        // 3. BATTERY STORAGE SYSTEM
        // ========================================================================
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
            analysis.append("  ✓ LiFePO4 - Premium option, longer lifespan (10+ years)\n");
        } else if (batteryAh < 300) {
            analysis.append(format.tag("  ✓ LiFePO4 (Recommended) - Better value long-term despite higher upfront cost", "GREEN") + "\n");
            analysis.append("  ○ AGM - Acceptable but requires 2× capacity vs lithium\n");
        } else {
            analysis.append(format.tag("  ✓ LiFePO4 ONLY - Large lead-acid banks are impractical (weight, space, maintenance)", "YELLOW") + "\n");
            analysis.append(format.tag("  ✓ Must include Battery Management System (BMS) for safety", "YELLOW") + "\n");
        }

        // Real-world context
        double runtimeHours = (batteryAh * voltage * (dod/100) * 0.85) / (totalWh / 24);
        analysis.append(String.format("\n• **Real-World Runtime:** ~%.1f hours of continuous operation at full load\n",
                runtimeHours));

        if (days >= 3) {
            analysis.append(format.tag("• **Resilience:** Excellent backup duration. Can handle extended storms or system maintenance.", "GREEN") + "\n");
        } else if (days >= 2) {
            analysis.append(format.tag("• **Resilience:** Good backup. Covers typical weather events (2-3 cloudy days).", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Resilience:** Minimal backup. System depends on daily solar charging.", "YELLOW") + "\n");
            analysis.append(format.tag("  Consider increasing autonomy to 2-3 days for critical applications.", "YELLOW") + "\n");
        }

        // ========================================================================
        // 4. INVERTER REQUIREMENTS
        // ========================================================================
        analysis.append("\n--- 🔌 POWER INVERTER SELECTION ---\n");
        double actualLoad = appliance.getWatts() * appliance.getQuantity();
        analysis.append(String.format("• **Continuous Rating:** %.0f W (with 25%% safety margin)\n", inverterW));
        analysis.append(String.format("• **Your Peak Load:** %.0f W actual\n", actualLoad));

        // Surge capacity consideration
        double surgeCap = inverterW * 2; // Most inverters: 2x continuous for 5-10 seconds
        analysis.append(String.format("• **Surge Capacity:** ~%.0f W (for motor/compressor startup)\n", surgeCap));

        if (inverterW < 1000) {
            analysis.append(format.tag("• **Type:** Modified sine wave acceptable, but pure sine recommended for electronics.", "GREEN") + "\n");
            analysis.append(format.tag("• **Form Factor:** Compact portable unit. Can mount near battery or integrate into panel.", "GREEN") + "\n");
        } else if (inverterW < 3000) {
            analysis.append(format.tag("• **Type:** Pure Sine Wave REQUIRED for sensitive electronics, appliances with motors.", "YELLOW") + "\n");
            analysis.append(format.tag("• **Form Factor:** Wall-mounted residential inverter. Requires proper ventilation.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Type:** High-quality Pure Sine Wave with low THD (<3%). Grid-tie capable recommended.", "RED") + "\n");
            analysis.append(format.tag("• **Form Factor:** Large format inverter. May require split-phase (120/240V) capability.", "RED") + "\n");
        }

        // Efficiency notes
        analysis.append("\n• **Efficiency Considerations:**\n");
        if (inverterW > actualLoad * 2) {
            analysis.append(format.tag("  ⚠️ Inverter is oversized - will have poor efficiency at low loads (idle draw).", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("  ✓ Good sizing - inverter will operate in efficient range (50-80% load).", "GREEN") + "\n");
        }

        // ========================================================================
        // 5. CHARGE CONTROLLER SPECIFICATIONS
        // ========================================================================
        analysis.append("\n--- 🎛️ SOLAR CHARGE CONTROLLER ---\n");
        analysis.append(String.format("• **Required Rating:** %.0f A MPPT controller\n", controllerA));
        analysis.append(String.format("• **System Voltage:** %dV (must match battery bank)\n", voltage));

        // MPPT vs PWM guidance
        analysis.append("\n• **Controller Technology:**\n");
        if (pvWatts < 400) {
            analysis.append("  ✓ MPPT Recommended - 20-30% more efficient, especially in cold weather\n");
            analysis.append("  ○ PWM Acceptable - Lower cost but requires panel voltage = battery voltage\n");
        } else {
            analysis.append(format.tag("  ✓ MPPT REQUIRED - System too large for PWM, would waste significant power", "YELLOW") + "\n");
        }

        // Voltage configuration
        double panelVoltage = voltage == 12 ? 18 : voltage == 24 ? 36 : 72; // Typical Vmp
        analysis.append(String.format("\n• **Panel String Configuration:**\n"));
        analysis.append(String.format("  - Panel Vmp should be %.0f-%.0fV for optimal MPPT tracking\n",
                panelVoltage * 0.9, panelVoltage * 1.3));
        analysis.append(String.format("  - Example: %s configuration for %dV system\n",
                voltage == 12 ? "1 panel in series" : voltage == 24 ? "2 panels in series" : "4 panels in series",
                voltage));

        if (controllerA < 30) {
            analysis.append(format.tag("• **Size Category:** Standard controller - Widely available, affordable, reliable.", "GREEN") + "\n");
        } else if (controllerA < 60) {
            analysis.append(format.tag("• **Size Category:** Medium-duty controller - Ensure proper heat dissipation and ventilation.", "YELLOW") + "\n");
        } else {
            analysis.append(format.tag("• **Size Category:** Heavy-duty controller - May need multiple units or commercial-grade equipment.", "RED") + "\n");
        }

        // ========================================================================
        // 6. SYSTEM VOLTAGE OPTIMIZATION
        // ========================================================================
        analysis.append("\n--- 🎯 VOLTAGE SELECTION ANALYSIS ---\n");
        analysis.append(String.format("• **Selected Voltage:** %dV DC System\n", voltage));

        analysis.append("\n• **Characteristics of " + voltage + "V systems:**\n");
        if (voltage == 12) {
            analysis.append("  ✓ Most common - Easy to find components and accessories\n");
            analysis.append("  ✓ Direct compatibility with automotive/marine equipment\n");
            analysis.append("  ⚠️ Higher current = thicker wires required (expensive, heavy)\n");
            analysis.append("  ⚠️ Voltage drop is critical - keep wiring under 10 feet total\n");

            if (totalWh > 1000) {
                analysis.append(format.tag("  ⚠️ WARNING: 12V is inefficient for this load size. Consider 24V or 48V.", "YELLOW") + "\n");
            }
        } else if (voltage == 24) {
            analysis.append("  ✓ Sweet spot for most residential systems (1-3 kWh/day)\n");
            analysis.append("  ✓ 50% less current than 12V = thinner, cheaper wiring\n");
            analysis.append("  ✓ Good component availability and pricing\n");
            analysis.append("  ○ Some 12V devices need DC-DC converters\n");

            if (totalWh < 500) {
                analysis.append(format.tag("  ℹ️ NOTE: 12V might be more practical for very small systems.", "YELLOW") + "\n");
            } else if (totalWh > 4000) {
                analysis.append(format.tag("  ℹ️ NOTE: 48V would be more efficient for this load size.", "YELLOW") + "\n");
            }
        } else { // 48V
            analysis.append("  ✓ Most efficient - Minimal losses, maximum range\n");
            analysis.append("  ✓ 75% less current than 12V = smallest wire gauge possible\n");
            analysis.append("  ✓ Standard for professional/commercial installations\n");
            analysis.append("  ⚠️ Requires step-down converters for 12V/24V devices\n");
            analysis.append("  ⚠️ Component costs slightly higher (but offset by savings)\n");

            if (totalWh < 2000) {
                analysis.append(format.tag("  ℹ️ NOTE: 48V may be overkill for smaller systems. 24V is adequate.", "YELLOW") + "\n");
            }
        }

        // Wire gauge guidance
        analysis.append("\n• **Wire Sizing Impact:**\n");
        double current = totalWh / (voltage * 24); // Rough average current
        if (voltage == 12) {
            analysis.append(String.format("  - Average current: ~%.1f A requires 6-10 AWG wire (thick!)\n", current));
        } else if (voltage == 24) {
            analysis.append(String.format("  - Average current: ~%.1f A requires 10-14 AWG wire (moderate)\n", current));
        } else {
            analysis.append(String.format("  - Average current: ~%.1f A requires 12-16 AWG wire (thin!)\n", current));
        }

        return analysis.toString();
    }

    private String generateSystemSummaryAnalysis(double totalWh, double pvWatts, double batteryAh,
                                                 double inverterW, double controllerA) {
        StringBuilder summary = new StringBuilder();
        int voltage = appliance.getSystemVoltage();
        double psh = appliance.getPeakSunHours();
        int days = appliance.getDaysOfAutonomy();
        double dod = appliance.getDepthOfDischarge();

        var format = new Object() {
            String tag(String text, String color) {
                return String.format("[%s]%s[/%s]", color, text, color);
            }
        };

        summary.append("\n═══════════════════════════════════════════════════════════════\n");
        summary.append("                    🎯 EXECUTIVE SUMMARY\n");
        summary.append("═══════════════════════════════════════════════════════════════\n\n");

        // ========================================================================
        // 1. SYSTEM CLASSIFICATION & SUITABILITY
        // ========================================================================
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

        // ========================================================================
        // 2. INVESTMENT & COMPLEXITY OVERVIEW
        // ========================================================================
        summary.append("--- 💰 INVESTMENT OVERVIEW ---\n");

        double estimatedCost = estimateSystemCost(pvWatts, batteryAh, inverterW, voltage);
        String costRange;
        String installTime;
        String skillLevel;

        if (estimatedCost < 50000) {
            costRange = "₱25,000 - ₱50,000";
            installTime = "4-8 hours (DIY weekend project)";
            skillLevel = "Beginner-friendly with basic electrical knowledge";
            colorTag = "GREEN";
        } else if (estimatedCost < 150000) {
            costRange = "₱50,000 - ₱150,000";
            installTime = "1-3 days (DIY) or 4-8 hours (professional)";
            skillLevel = "Intermediate DIY or hire licensed electrician";
            colorTag = "YELLOW";
        } else if (estimatedCost < 350000) {
            costRange = "₱150,000 - ₱350,000";
            installTime = "3-5 days (advanced DIY) or 1-2 days (professional)";
            skillLevel = "Advanced DIY with electrical experience, or professional strongly recommended";
            colorTag = "YELLOW";
        } else {
            costRange = "₱350,000 - ₱750,000+";
            installTime = "5-10 days including permitting and inspection";
            skillLevel = "Professional installation REQUIRED. Licensed electrician and permits needed.";
            colorTag = "RED";
        }

        summary.append(format.tag("• **Estimated Budget:** " + costRange + " (equipment only, no labor)", colorTag) + "\n");
        summary.append(format.tag("• **Installation Time:** " + installTime, colorTag) + "\n");
        summary.append(format.tag("• **Skill Level:** " + skillLevel, colorTag) + "\n\n");

        // Cost breakdown
        summary.append("• **Budget Breakdown:**\n");
        summary.append(String.format("  - Solar Panels: ~₱%.0f (%.0fW @ ₱40/W)\n", pvWatts * 40, pvWatts));
        summary.append(String.format("  - Battery Bank: ~₱%.0f (%.0fAh LiFePO4 @ ₱130/Ah)\n", batteryAh * 130, batteryAh));
        summary.append(String.format("  - Inverter: ~₱%.0f (%.0fW @ ₱20/W)\n", inverterW * 20, inverterW));
        summary.append(String.format("  - Charge Controller: ~₱%.0f (%.0fA MPPT)\n", controllerA * 600, controllerA));
        summary.append("  - Wiring/Hardware: ~₱15,000-25,000\n\n");

        // ========================================================================
        // 3. PERFORMANCE SCORECARD
        // ========================================================================
        summary.append("--- 📈 PERFORMANCE SCORECARD ---\n");

        // Energy Balance Score
        double dailyProduction = pvWatts * psh * 0.85;
        double productionRatio = dailyProduction / totalWh;
        String energyGrade;
        String energyFeedback;

        if (productionRatio >= 1.5) {
            energyGrade = "A+ (Excellent Surplus)";
            energyFeedback = "System produces 50%+ more than needed. Great for winter!";
            colorTag = "GREEN";
        } else if (productionRatio >= 1.2) {
            energyGrade = "A (Optimal Balance)";
            energyFeedback = "Perfect sizing with 20% safety margin for cloudy days.";
            colorTag = "GREEN";
        } else if (productionRatio >= 1.0) {
            energyGrade = "B (Adequate)";
            energyFeedback = "Meets needs but tight margin. Consider 15-20% larger array.";
            colorTag = "YELLOW";
        } else if (productionRatio >= 0.85) {
            energyGrade = "C (Marginal)";
            energyFeedback = "Will slowly drain batteries. Increase array by 15-20%.";
            colorTag = "YELLOW";
        } else {
            energyGrade = "F (Insufficient)";
            energyFeedback = "CRITICAL: Cannot meet daily demand! Increase by " +
                    String.format("%.0f%%", (1/productionRatio - 1) * 100) + " minimum.";
            colorTag = "RED";
        }

        summary.append(format.tag(String.format("1. **Energy Balance:** %s - %s", energyGrade, energyFeedback), colorTag) + "\n");
        summary.append(String.format("   (Produces %.0f Wh/day vs %.0f Wh/day needed)\n\n", dailyProduction, totalWh));

        // Battery Resilience Score
        String resilienceGrade;
        String resilienceFeedback;

        if (days >= 4) {
            resilienceGrade = "A+ (Excellent Backup)";
            resilienceFeedback = days + " days autonomy handles extended storms perfectly.";
            colorTag = "GREEN";
        } else if (days >= 3) {
            resilienceGrade = "A (Strong Backup)";
            resilienceFeedback = days + " days autonomy covers typical weather events.";
            colorTag = "GREEN";
        } else if (days >= 2) {
            resilienceGrade = "B (Good Backup)";
            resilienceFeedback = days + " days is adequate. Consider 3+ for critical loads.";
            colorTag = "YELLOW";
        } else {
            resilienceGrade = "C (Minimal Backup)";
            resilienceFeedback = "Only " + days + " day autonomy. Depends on daily solar charging.";
            colorTag = "YELLOW";
        }

        summary.append(format.tag(String.format("2. **Battery Resilience:** %s - %s", resilienceGrade, resilienceFeedback), colorTag) + "\n");
        summary.append(String.format("   (%.0f Ah @ %dV with %.0f%% DoD = %.1f kWh usable)\n\n",
                batteryAh, voltage, dod, (batteryAh * voltage * (dod/100)) / 1000));

        // Voltage Optimization Score
        boolean voltageOptimal = isVoltageOptimal(totalWh, voltage);
        String voltageGrade;
        String voltageFeedback;

        if (voltageOptimal) {
            voltageGrade = "A (Optimal Choice)";
            voltageFeedback = voltage + "V is perfect for this load size. Minimizes losses.";
            colorTag = "GREEN";
        } else {
            String recommended = totalWh > 4000 ? "48V" : totalWh > 1500 ? "24V" : "12V";
            voltageGrade = "B (Acceptable)";
            voltageFeedback = voltage + "V works, but " + recommended + " would be more efficient.";
            colorTag = "YELLOW";
        }

        summary.append(format.tag(String.format("3. **Voltage Selection:** %s - %s", voltageGrade, voltageFeedback), colorTag) + "\n");
        double avgCurrent = totalWh / (voltage * 24);
        summary.append(String.format("   (Average current: ~%.1f A requires %s wire)\n\n",
                avgCurrent, voltage >= 48 ? "thin 12-14 AWG" : voltage >= 24 ? "moderate 10-12 AWG" : "thick 6-10 AWG"));

        // Component Sizing Score
        double inverterUtilization = (appliance.getWatts() * appliance.getQuantity()) / inverterW;
        String sizingGrade;
        String sizingFeedback;

        if (inverterUtilization >= 0.5 && inverterUtilization <= 0.8 && productionRatio >= 1.1) {
            sizingGrade = "A (Well Balanced)";
            sizingFeedback = "All components properly sized with appropriate safety margins.";
            colorTag = "GREEN";
        } else if (inverterUtilization >= 0.4 && productionRatio >= 0.95) {
            sizingGrade = "B (Good Sizing)";
            sizingFeedback = "Components sized appropriately with minor room for improvement.";
            colorTag = "YELLOW";
        } else {
            sizingGrade = "C (Needs Adjustment)";
            sizingFeedback = "Some components oversized or undersized. Review recommendations.";
            colorTag = "YELLOW";
        }

        summary.append(format.tag(String.format("4. **Component Sizing:** %s - %s", sizingGrade, sizingFeedback), colorTag) + "\n");
        summary.append(String.format("   (Inverter efficiency: %.0f%% utilization)\n\n", inverterUtilization * 100));

        // ========================================================================
        // 4. CRITICAL CONSIDERATIONS & ACTION ITEMS
        // ========================================================================
        summary.append("--- ⚠️ CRITICAL CONSIDERATIONS ---\n");

        boolean hasCriticalIssues = false;

        if (productionRatio < 1.0) {
            summary.append(format.tag("❌ ENERGY DEFICIT: Solar array undersized. System will drain batteries daily!", "RED") + "\n");
            summary.append(format.tag("   → ACTION: Increase PV array by " +
                    String.format("%.0f%%", (1/productionRatio - 1) * 100) +
                    " OR reduce load consumption.", "RED") + "\n");
            hasCriticalIssues = true;
        }

        if (batteryAh > 400 && !summary.toString().contains("BMS")) {
            summary.append(format.tag("❌ SAFETY: Large battery bank REQUIRES Battery Management System (BMS)!", "RED") + "\n");
            summary.append(format.tag("   → ACTION: Ensure lithium batteries include built-in BMS or add external BMS.", "RED") + "\n");
            hasCriticalIssues = true;
        }

        if (!voltageOptimal && totalWh > 2000) {
            String recommended = totalWh > 4000 ? "48V" : "24V";
            summary.append(format.tag("⚠️ EFFICIENCY: Voltage suboptimal for this load size.", "YELLOW") + "\n");
            summary.append(format.tag("   → RECOMMENDATION: Consider " + recommended +
                    " system for better efficiency and lower wire costs.", "YELLOW") + "\n");
        }

        if (inverterW > 3000 && voltage < 48) {
            summary.append(format.tag("⚠️ POWER QUALITY: Large inverter may require split-phase (120V/240V) capability.", "YELLOW") + "\n");
            summary.append(format.tag("   → ACTION: Verify inverter supports 240V if needed for large appliances.", "YELLOW") + "\n");
        }

        if (controllerA > 60) {
            summary.append(format.tag("⚠️ CONTROLLER: Very high current rating may require multiple controllers.", "YELLOW") + "\n");
            summary.append(format.tag("   → ACTION: Consider 2× smaller controllers in parallel or commercial-grade unit.", "YELLOW") + "\n");
        }

        if (!hasCriticalIssues) {
            summary.append(format.tag("✅ No critical issues found. System design is sound.", "GREEN") + "\n");
        }

        summary.append("\n");

        // ========================================================================
        // 5. PRE-PURCHASE CHECKLIST
        // ========================================================================
        summary.append("--- ✅ PRE-PURCHASE CHECKLIST ---\n");
        summary.append("Before buying components, verify:\n\n");

        summary.append("**Solar Panels:**\n");
        summary.append(String.format("  ☐ Total wattage: %.0fW minimum (%.0fW+ recommended)\n", pvWatts * 0.9, pvWatts));
        summary.append(String.format("  ☐ Panel voltage (Vmp): %.0f-%.0fV for %dV system\n",
                voltage == 12 ? 17.0 : voltage == 24 ? 34.0 : 68.0,
                voltage == 12 ? 22.0 : voltage == 24 ? 44.0 : 88.0,
                voltage));
        summary.append("  ☐ Warranty: 25-year power output guarantee (standard)\n");
        summary.append("  ☐ Mounting hardware included or purchased separately\n\n");

        summary.append("**Battery Bank:**\n");
        summary.append(String.format("  ☐ Capacity: %.0f Ah minimum @ %dV\n", batteryAh, voltage));
        summary.append("  ☐ Chemistry: LiFePO4 recommended (10+ year lifespan)\n");
        summary.append("  ☐ BMS included (critical for lithium batteries)\n");
        summary.append("  ☐ Temperature rating suitable for installation location\n");
        if (batteryAh > 200) {
            summary.append("  ☐ Consider modular batteries for easier replacement\n");
        }
        summary.append("\n");

        summary.append("**Inverter:**\n");
        summary.append(String.format("  ☐ Continuous rating: %.0fW minimum\n", inverterW));
        summary.append(String.format("  ☐ Surge rating: %.0fW minimum (2× continuous)\n", inverterW * 2));
        summary.append("  ☐ Pure sine wave (required for sensitive electronics)\n");
        summary.append(String.format("  ☐ Input voltage: %dV DC\n", voltage));
        summary.append("  ☐ Output: 120V AC " + (inverterW > 3000 ? "(or 120/240V split-phase)" : "") + "\n");
        summary.append("  ☐ Efficiency: >90% at 50-80% load\n\n");

        summary.append("**Charge Controller:**\n");
        summary.append(String.format("  ☐ Current rating: %.0fA minimum (MPPT type)\n", controllerA));
        summary.append(String.format("  ☐ System voltage: %dV compatible\n", voltage));
        summary.append(String.format("  ☐ Max PV input: %.0fV minimum\n",
                voltage == 12 ? 50.0 : voltage == 24 ? 100.0 : 150.0));
        summary.append("  ☐ Temperature compensation feature included\n");
        summary.append("  ☐ Display/monitoring capabilities (recommended)\n\n");

        summary.append("**Wiring & Safety:**\n");
        double wireGauge = voltage == 12 ? 6 : voltage == 24 ? 10 : 12;
        summary.append(String.format("  ☐ Wire gauge: %.0f AWG minimum for main runs\n", wireGauge));
        summary.append("  ☐ DC-rated circuit breakers for all components\n");
        summary.append("  ☐ Fuses: PV input, battery, inverter connections\n");
        summary.append("  ☐ Properly rated MC4 connectors for solar panels\n");
        if (totalWh > 2000) {
            summary.append("  ☐ Battery disconnect switch (required for safety)\n");
            summary.append("  ☐ Ground fault protection (GFPD) for solar array\n");
        }
        summary.append("\n");

        // ========================================================================
        // 6. INSTALLATION PRIORITIES
        // ========================================================================
        summary.append("--- 🔧 INSTALLATION PRIORITIES ---\n");
        summary.append("Complete these steps in order:\n\n");

        summary.append("**Phase 1: Planning (1-2 weeks before)**\n");
        if (totalWh > 3000 || inverterW > 3000) {
            summary.append("  1. Check local codes and permit requirements (REQUIRED for large systems)\n");
            summary.append("  2. Schedule electrical inspection if needed\n");
        } else {
            summary.append("  1. Review local codes (permits may not be required for small systems)\n");
        }
        summary.append("  2. Measure and plan mounting locations (roof/ground/wall)\n");
        summary.append("  3. Calculate exact wire runs and sizes\n");
        summary.append("  4. Order all components with 2-week buffer for delivery\n\n");

        summary.append("**Phase 2: Installation**\n");
        summary.append("  1. Mount solar panels with proper orientation (south-facing, optimal tilt)\n");
        summary.append("  2. Install battery bank in temperature-controlled, ventilated area\n");
        summary.append("  3. Mount charge controller near batteries (short wire runs)\n");
        summary.append("  4. Install inverter close to main loads\n");
        summary.append("  5. Wire DC side FIRST (PV → Controller → Battery)\n");
        summary.append("  6. Add all fuses and breakers BEFORE connecting battery\n");
        summary.append("  7. Wire AC side LAST (Inverter → Load panel)\n\n");

        summary.append("**Phase 3: Testing & Commissioning**\n");
        summary.append("  1. Verify all voltages with multimeter before powering on\n");
        summary.append("  2. Check polarity (critical - reverse polarity destroys components!)\n");
        summary.append("  3. Power on charge controller, verify battery charging\n");
        summary.append("  4. Test inverter with small load first, then gradually increase\n");
        summary.append("  5. Monitor system for 3-5 days to verify performance\n");
        summary.append("  6. Document all settings and take photos for reference\n\n");

        // ========================================================================
        // 7. ONGOING MAINTENANCE SCHEDULE
        // ========================================================================
        summary.append("--- 🛠️ MAINTENANCE SCHEDULE ---\n");

        summary.append("**Monthly:**\n");
        summary.append("  • Check battery voltage and state of charge\n");
        summary.append("  • Inspect all connections for corrosion or looseness\n");
        summary.append("  • Clean dust from inverter and controller vents\n\n");

        summary.append("**Quarterly:**\n");
        summary.append("  • Clean solar panels (bird droppings, dust, pollen)\n");
        summary.append("  • Verify charge controller settings and performance\n");
        summary.append("  • Check for any unusual noises or heat from components\n\n");

        summary.append("**Annually:**\n");
        summary.append("  • Professional inspection (recommended for systems >3kW)\n");
        summary.append("  • Battery capacity test and equalization (if lead-acid)\n");
        summary.append("  • Torque-check all electrical connections\n");
        summary.append("  • Update firmware on smart controllers/inverters\n\n");

        // ========================================================================
        // 8. EXPANSION PLANNING
        // ========================================================================
        if (totalWh < 5000) {
            summary.append("--- 📈 FUTURE EXPANSION OPTIONS ---\n");
            summary.append("Plan for growth by:\n\n");

            summary.append("  • **Add More Panels:** System can easily handle 20-30% more PV capacity\n");
            summary.append(String.format("    → Up to %.0fW without controller upgrade\n", pvWatts * 1.3));

            summary.append("  • **Add Battery Capacity:** Increase autonomy or support more loads\n");
            summary.append(String.format("    → Can parallel additional %.0fAh @ %dV banks\n", batteryAh, voltage));

            if (inverterW < 3000) {
                summary.append("  • **Upgrade Inverter:** If loads increase significantly\n");
                summary.append(String.format("    → Next size up: %.0fW (plan wiring accordingly)\n",
                        Math.ceil(inverterW * 1.5 / 500) * 500));
            }

            summary.append("\n  💡 TIP: Size conduit and wiring 25-30% larger than current needs!\n\n");
        }

        // ========================================================================
        // 9. COST-BENEFIT ANALYSIS
        // ========================================================================
        summary.append("--- 💵 RETURN ON INVESTMENT ---\n");

        double monthlyKwh = (totalWh * 30) / 1000;
        double monthlySavings = monthlyKwh * 11; // Average ₱11/kWh (Meralco rates)
        double annualSavings = monthlySavings * 12;
        double paybackYears = estimatedCost / annualSavings;

        summary.append(String.format("• **Grid Equivalent:** %.1f kWh/month\n", monthlyKwh));
        summary.append(String.format("• **Monthly Savings:** ₱%.0f (at ₱11/kWh Meralco rate)\n", monthlySavings));
        summary.append(String.format("• **Annual Savings:** ₱%.0f\n", annualSavings));

        if (paybackYears < 6) {
            summary.append(format.tag(String.format("• **Payback Period:** ~%.1f years (Excellent ROI for PH)", paybackYears), "GREEN") + "\n");
        } else if (paybackYears < 12) {
            summary.append(format.tag(String.format("• **Payback Period:** ~%.1f years (Good ROI)", paybackYears), "YELLOW") + "\n");
        } else {
            summary.append(format.tag(String.format("• **Payback Period:** ~%.1f years (Long-term investment)", paybackYears), "YELLOW") + "\n");
        }

        summary.append("\n**Additional Benefits (Not Quantified):**\n");
        summary.append("  • Energy independence during brownouts/blackouts (common in PH)\n");
        summary.append("  • Protection from Meralco rate increases\n");
        summary.append("  • Reduced carbon footprint\n");
        summary.append("  • Increased property value\n");
        if (totalWh < 2000) {
            summary.append("  • Portable power for emergencies or recreation\n");
        }
        summary.append("\n");

        // ========================================================================
        // 10. FINAL VERDICT & RECOMMENDATION
        // ========================================================================
        summary.append("═══════════════════════════════════════════════════════════════\n");
        summary.append("                  🌟 FINAL RECOMMENDATION\n");
        summary.append("═══════════════════════════════════════════════════════════════\n\n");

        // Calculate overall system score
        int score = 0;
        if (productionRatio >= 1.2) score += 25;
        else if (productionRatio >= 1.0) score += 15;
        else if (productionRatio >= 0.85) score += 5;

        if (days >= 3) score += 25;
        else if (days >= 2) score += 20;
        else if (days >= 1) score += 10;

        if (voltageOptimal) score += 25;
        else score += 15;

        if (inverterUtilization >= 0.5 && inverterUtilization <= 0.8) score += 25;
        else if (inverterUtilization >= 0.4) score += 15;
        else score += 10;

        String overallGrade;
        String verdict;
        String action;

        if (score >= 90) {
            overallGrade = "A+ (Excellent System)";
            verdict = "This is an **EXCEPTIONALLY WELL-DESIGNED SYSTEM** with optimal component sizing, "
                    + "strong energy margins, and excellent resilience. All aspects of the design demonstrate "
                    + "careful planning and adherence to best practices.";
            action = "**PROCEED WITH CONFIDENCE.** This system will provide reliable, efficient power for years. "
                    + "Focus on quality components and proper installation.";
            colorTag = "GREEN";
        } else if (score >= 75) {
            overallGrade = "A (Very Good System)";
            verdict = "This is a **SOLID, WELL-BALANCED SYSTEM** that will meet your needs effectively. "
                    + "Component sizing is appropriate with good safety margins. Minor optimizations possible "
                    + "but not critical.";
            action = "**RECOMMENDED FOR IMPLEMENTATION.** Review the considerations above, but overall this "
                    + "design is sound. Expect reliable performance with proper maintenance.";
            colorTag = "GREEN";
        } else if (score >= 60) {
            overallGrade = "B (Good System with Caveats)";
            verdict = "This is a **FUNCTIONAL SYSTEM** that will work, but has areas for improvement. "
                    + "Some components may be slightly undersized or oversized. Review the yellow-flagged "
                    + "items carefully.";
            action = "**PROCEED WITH ADJUSTMENTS.** Address the recommendations in the 'Critical Considerations' "
                    + "section before purchasing. Small changes will significantly improve performance.";
            colorTag = "YELLOW";
        } else if (score >= 40) {
            overallGrade = "C (Needs Significant Improvement)";
            verdict = "This system has **SEVERAL DESIGN ISSUES** that should be addressed before implementation. "
                    + "While it may function, performance will be suboptimal and reliability may be compromised.";
            action = "**REVISE DESIGN BEFORE PROCEEDING.** Work through the critical issues identified above. "
                    + "Consider consulting with a solar professional for design review.";
            colorTag = "YELLOW";
        } else {
            overallGrade = "D (Major Concerns)";
            verdict = "This system has **CRITICAL DESIGN FLAWS** that will prevent proper operation. "
                    + "Components are significantly mismatched or undersized. System will not meet expectations.";
            action = "**DO NOT PROCEED WITHOUT MAJOR REVISIONS.** Strongly recommend professional consultation. "
                    + "Current design will lead to poor performance, shortened component life, or safety issues.";
            colorTag = "RED";
        }

        summary.append(format.tag("**Overall Grade:** " + overallGrade + " (Score: " + score + "/100)", colorTag) + "\n\n");
        summary.append(format.tag(verdict, colorTag) + "\n\n");
        summary.append(format.tag("**Next Steps:** " + action, colorTag) + "\n\n");

        // Personalized closing based on system size
        if (totalWh < 1000) {
            summary.append("This is a great entry-level system! Perfect for learning solar basics and gaining "
                    + "hands-on experience. Start small, learn the principles, then expand as needed.\n");
        } else if (totalWh < 3000) {
            summary.append("This system represents the sweet spot for residential off-grid or backup power. "
                    + "It's large enough to be useful but manageable enough for careful DIY installation. "
                    + "Take your time, follow safety protocols, and enjoy energy independence!\n");
        } else {
            summary.append("This is a substantial investment in energy independence. Given the system size and "
                    + "complexity, professional installation is strongly recommended unless you have significant "
                    + "electrical experience. The payoff will be comprehensive, reliable power for your home.\n");
        }

        summary.append("\n");
        summary.append("═══════════════════════════════════════════════════════════════\n");
        summary.append("        📋 Save this analysis for reference during installation!\n");
        summary.append("═══════════════════════════════════════════════════════════════\n");

        return summary.toString();
    }


// === Helper Methods ===

    private double estimateSystemCost(double pvWatts, double batteryAh, double inverterW, int voltage) {
        CountryConfig country = appliance.getCountry();

        double pvCost = pvWatts * country.getSolarPanelPricePerWatt();
        double batteryCost = batteryAh * country.getBatteryPricePerAh();
        double inverterCost = inverterW * country.getInverterPricePerWatt();
        double controllerCost = (pvWatts/voltage) * country.getControllerPricePerAmp();

        // Voltage-based wiring cost adjustment
        double wiringCostMultiplier = voltage == 12 ? 1.3 : voltage == 24 ? 1.0 : 0.8;
        double wiringCost = country.getWiringCostBase() * wiringCostMultiplier;

        // Size-based mounting cost
        double mountingMultiplier = pvWatts < 1000 ? 1.0 : pvWatts < 3000 ? 2.0 : 3.0;
        double mountingCost = country.getWiringCostBase() * mountingMultiplier;

        double miscCost = country.getWiringCostBase() * 0.67; // ~67% of wiring cost

        return pvCost + batteryCost + inverterCost + controllerCost + wiringCost + mountingCost + miscCost;
    }

    private boolean isVoltageOptimal(double totalWh, int voltage) {
        // Industry best practices for voltage selection
        if (totalWh < 1000) return voltage == 12;
        if (totalWh < 3000) return voltage == 24;
        return voltage == 48;
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
                            addSectionHeader("");
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
        return label.equals("") || label.equals("") ||
                label.equals("") || label.equals("");
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

        JButton countryBtn = new JButton("🌏 Change Country/Region");
        countryBtn.addActionListener((ActionEvent e) -> {
            showCountrySelectionDialog();
        });

        toolbar.add(saveBtn);
        toolbar.add(loadBtn);
        toolbar.add(countryBtn);
        return toolbar;
    }
    private void showCountrySelectionDialog() {
        // Create a dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Select Country/Region", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        // Create panel for country selection
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CARD_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label
        JLabel label = new JLabel("Country/Region:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(label, gbc);

        // Country selector
        JComboBox<CountryConfig> countrySelector = new JComboBox<>(CountryConfig.values());
        countrySelector.setSelectedItem(appliance.getCountry());
        countrySelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countrySelector.setBackground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 0;
        contentPanel.add(countrySelector, gbc);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(CARD_BACKGROUND);

        JButton applyBtn = createStyledButton("Apply", SUCCESS_COLOR, new Color(22, 163, 74));
        applyBtn.setPreferredSize(new Dimension(100, 35));
        applyBtn.addActionListener(e -> {
            appliance.setCountry((CountryConfig) countrySelector.getSelectedItem());
            JOptionPane.showMessageDialog(dialog,
                    "Country changed to: " + countrySelector.getSelectedItem(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Optionally recalculate if results are already shown
            if (tabbedPane.getSelectedIndex() == 2) {
                calculateThisAppliance();
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
}