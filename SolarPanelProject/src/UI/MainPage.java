package UI;

import Model.Appliance;
import Model.SolarCalculator;
import Utils.ProjectData;
import Utils.ProjectManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MainPage - Central hub for navigation and data management
 */
public class MainPage extends JPanel {
    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    // Shared data accessible to all panels
    private final List<Appliance> appliances = new ArrayList<>();
    private final SolarCalculator calculator = new SolarCalculator();

    // Panels
    private final WelcomePanel welcomePanel;
    private final LoadInputPanel loadInputPanel;
    private final ApplianceListPanel applianceListPanel;
    private final SolarParametersPanel solarParametersPanel;

    // Solar parameters storage (single source of truth)
    private double peakSunHours = 5.0;
    private double depthOfDischarge = 50.0;
    private int daysOfAutonomy = 2;
    private int systemVoltage = 12;

    // Track navigation history
    private Appliance lastViewedAppliance;

    public MainPage() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel, BorderLayout.CENTER);

        // Initialize all major panels
        welcomePanel = new WelcomePanel(this);
        loadInputPanel = new LoadInputPanel(this);
        applianceListPanel = new ApplianceListPanel(this, appliances);
        solarParametersPanel = new SolarParametersPanel(this);

        // Add them to card container
        cardPanel.add(welcomePanel, "WelcomePanel");
        cardPanel.add(loadInputPanel, "LoadInputPanel");
        cardPanel.add(applianceListPanel, "ApplianceListPanel");
        cardPanel.add(solarParametersPanel, "SolarParametersPanel");

        // Start with Welcome page
        showWelcomePanel();
    }

    // ============================================================================
    // NAVIGATION METHODS
    // ============================================================================

    public void showWelcomePanel() {
        cardLayout.show(cardPanel, "WelcomePanel");
    }

    public void showLoadInputPanel() {
        cardLayout.show(cardPanel, "LoadInputPanel");
    }

    public void showApplianceListPanel() {
        applianceListPanel.refreshList();
        cardLayout.show(cardPanel, "ApplianceListPanel");
    }

    public void showSolarParametersPanel() {
        // Ensure we refresh UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            solarParametersPanel.loadCurrentValues(); // Refresh with current values
            cardLayout.show(cardPanel, "SolarParametersPanel");
        });
    }

    public void showApplianceDetailsPanel(Appliance appliance) {
        setLastViewedAppliance(appliance); // Track which appliance we're viewing

        // Sync MainPage solar parameters to the appliance before showing details
        appliance.setPeakSunHours(peakSunHours);
        appliance.setDepthOfDischarge(depthOfDischarge);
        appliance.setDaysOfAutonomy(daysOfAutonomy);
        appliance.setSystemVoltage(systemVoltage);

        ApplianceDetailsPanel detailsPanel = new ApplianceDetailsPanel(appliance, this);
        cardPanel.add(detailsPanel, "ApplianceDetailsPanel");
        cardLayout.show(cardPanel, "ApplianceDetailsPanel");
    }

    // ============================================================================
    // DATA ACCESS - APPLIANCES
    // ============================================================================

    public List<Appliance> getAppliances() {
        return appliances;
    }

    public SolarCalculator getCalculator() {
        return calculator;
    }

    // ============================================================================
    // DATA ACCESS - SOLAR PARAMETERS
    // ============================================================================

    public double getPeakSunHours() {
        return peakSunHours;
    }

    public void setPeakSunHours(double peakSunHours) {
        this.peakSunHours = peakSunHours;
    }

    public double getDepthOfDischarge() {
        return depthOfDischarge;
    }

    public void setDepthOfDischarge(double depthOfDischarge) {
        this.depthOfDischarge = depthOfDischarge;
    }

    public int getDaysOfAutonomy() {
        return daysOfAutonomy;
    }

    public void setDaysOfAutonomy(int daysOfAutonomy) {
        this.daysOfAutonomy = daysOfAutonomy;
    }

    public int getSystemVoltage() {
        return systemVoltage;
    }

    public void setSystemVoltage(int systemVoltage) {
        this.systemVoltage = systemVoltage;
    }

    // ============================================================================
    // NAVIGATION HISTORY
    // ============================================================================

    public Appliance getLastViewedAppliance() {
        return lastViewedAppliance;
    }

    public void setLastViewedAppliance(Appliance appliance) {
        this.lastViewedAppliance = appliance;
    }

    // ============================================================================
    // NEW: Apply loaded ProjectData into MainPage state and UI
    // ============================================================================
    /**
     * Apply loaded project data into MainPage and refresh relevant panels.
     * Always runs UI updates on the Event Dispatch Thread.
     */
    public void applyProjectData(ProjectData projectData) {
        if (projectData == null) return;

        // Apply data on EDT
        SwingUtilities.invokeLater(() -> {
            // Replace appliances if present
            if (projectData.getAppliances() != null) {
                appliances.clear();
                appliances.addAll(projectData.getAppliances());
            }

            // Apply solar parameters
            setPeakSunHours(projectData.getPeakSunHours());
            setDepthOfDischarge(projectData.getDepthOfDischarge());
            setDaysOfAutonomy(projectData.getDaysOfAutonomy());
            setSystemVoltage(projectData.getSystemVoltage());

            // Ensure every appliance gets the new parameters
            for (Appliance a : appliances) {
                a.setPeakSunHours(getPeakSunHours());
                a.setDepthOfDischarge(getDepthOfDischarge());
                a.setDaysOfAutonomy(getDaysOfAutonomy());
                a.setSystemVoltage(getSystemVoltage());
            }

            // Refresh UI components that depend on these values
            applianceListPanel.refreshList();
            // If the solar panel is open, reload it
            solarParametersPanel.loadCurrentValues();
        });
    }

    /**
     * Convenience helper: call this to open a file and apply it directly to the app.
     * This ties ProjectManager.loadProject() → applyProjectData() in one place.
     */
    public void loadProjectFromFile(Component parent) {
        // ProjectManager will show file dialogs; call it from EDT or background as appropriate.
        ProjectData pd = ProjectManager.loadProject(parent);
        if (pd != null) {
            System.out.println("Loaded from JSON: " + pd);
            applyProjectData(pd);
            JOptionPane.showMessageDialog(this, "Project applied to application state.", "Load Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}