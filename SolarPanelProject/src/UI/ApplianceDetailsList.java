package UI;

import Model.Appliance;
import Model.SolarCalculator;
/*import Utils.ProjectManager;*/
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
public class ApplianceDetailsList {
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
}
