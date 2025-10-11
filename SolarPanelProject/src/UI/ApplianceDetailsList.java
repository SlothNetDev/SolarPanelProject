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
}
