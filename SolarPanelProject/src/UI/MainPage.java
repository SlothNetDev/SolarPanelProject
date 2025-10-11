package UI;

import Model.Appliance;
import Model.SolarCalculator;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * MainPage acts as the navigation controller between all app panels.
 * It uses CardLayout to manage panel switching dynamically.
 */
public class MainPage extends JPanel {

    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    // Shared data accessible to all panels
    private final List<Appliance> appliances = new ArrayList<>();
    private final SolarCalculator calculator = new SolarCalculator(); // Add SolarCalculator instance

    // Panels
    private final WelcomePanel welcomePanel;
    private final LoadInputPanel loadInputPanel;
    private final ApplianceListPanel applianceListPanel;

    /*
     * private final ParametersPanel parametersPanel; private final ResultsPanel
     * resultsPanel;
     */
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
        /*
         * parametersPanel = new ParametersPanel(this); resultsPanel = new
         * ResultsPanel(this);
         */

        // Add them to card container
        cardPanel.add(welcomePanel, "WelcomePanel");
        cardPanel.add(loadInputPanel, "LoadInputPanel");
        cardPanel.add(applianceListPanel, "ApplianceListPanel");
        /*
         * cardPanel.add(parametersPanel, "ParametersPanel");
         * cardPanel.add(resultsPanel, "ResultsPanel");
         */

        // Start with Welcome page
        showWelcomePanel();
    }

    // -----------------------------
    // NAVIGATION METHODS
    // -----------------------------

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

    public void showParametersPanel() {
        cardLayout.show(cardPanel, "ParametersPanel");
    }

    /*
     * public void showResultsPanel(String resultsText) {
     * resultsPanel.updateResults(resultsText); cardLayout.show(cardPanel,
     * "ResultsPanel"); }
     */

/*
    public void showApplianceDetailsPanel(Appliance appliance) {
        ApplianceDetailsPanel detailsPanel = new ApplianceDetailsPanel(appliance, this);
        cardPanel.add(detailsPanel, "ApplianceDetailsPanel");
        cardLayout.show(cardPanel, "ApplianceDetailsPanel");
    }
*/

    // -----------------------------
    // DATA ACCESS
    // -----------------------------

    public List<Appliance> getAppliances() {
        return appliances;
    }

    // Add the missing getCalculator method
    public SolarCalculator getCalculator() {
        return calculator;
    }


}