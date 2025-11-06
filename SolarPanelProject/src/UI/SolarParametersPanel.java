package UI;

import Utils.ProjectData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * SolarParametersPanel - Centralized panel for configuring solar system parameters
 * that apply to all appliances in the system.
 */
public class SolarParametersPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final MainPage mainPage;

    // Solar parameter fields
    private JTextField pshField, dodField, daysField;
    private JComboBox<String> voltageCombo;

    // Modern color palette
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color PRIMARY_HOVER = new Color(37, 99, 235);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color SUCCESS_HOVER = new Color(22, 163, 74);
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(71, 85, 105);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color INPUT_BORDER = new Color(203, 213, 225);

    public SolarParametersPanel(MainPage mainPage) {
        this.mainPage = mainPage;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(createMainContent());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createMainContent() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));

        JPanel header = createHeader();
        JPanel card = createFormCard();
        JPanel actionPanel = createActionPanel();

        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(header);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(card);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(actionPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(contentPanel, gbc);

        // Load current values after UI is created
        loadCurrentValues();

        return wrapper;
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel("☀️", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Solar System Parameters", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));

        JLabel subtitle = new JLabel("Configure system-wide solar specifications", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, subtitle.getPreferredSize().height));

        headerPanel.add(icon);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitle);

        return headerPanel;
    }

    private JPanel createFormCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 12),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)
        ));

        // Initialize fields with placeholders
        pshField = createInputField("e.g., 5.0");
        dodField = createInputField("e.g., 50");
        daysField = createInputField("e.g., 2");
        voltageCombo = new JComboBox<>(new String[]{"12", "24", "48"});
        voltageCombo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        voltageCombo.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        card.add(createFormRow("☀️ Peak Sun Hours", pshField, "Average daily peak sun hours in your location"), gbc);
        gbc.gridy++;
        card.add(createFormRow("🔋 Depth of Discharge (%)", dodField, "Recommended: 50% for lead-acid, 80% for LiFePO4"), gbc);
        gbc.gridy++;
        card.add(createFormRow("📅 Days of Autonomy", daysField, "Number of days system should run without sun"), gbc);
        gbc.gridy++;
        card.add(createFormRow("⚡ System Voltage", voltageCombo, "Higher voltage for larger systems"), gbc);

        return card;
    }

    private JPanel createFormRow(String labelText, JComponent inputComponent, String helpText) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (inputComponent instanceof JTextField) {
            inputComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
            inputComponent.setMaximumSize(new Dimension(250, 42));
            ((JTextField) inputComponent).setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(INPUT_BORDER, 8),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));
        } else if (inputComponent instanceof JComboBox) {
            inputComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
            inputComponent.setMaximumSize(new Dimension(250, 42));
        }

        JLabel help = new JLabel(helpText);
        help.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        help.setForeground(TEXT_SECONDARY);
        help.setAlignmentX(Component.CENTER_ALIGNMENT);

        row.add(label);
        row.add(Box.createVerticalStrut(6));
        row.add(inputComponent);
        row.add(Box.createVerticalStrut(4));
        row.add(help);

        return row;
    }

    private JTextField createInputField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(new Color(148, 163, 184));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(INPUT_BORDER, 8),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                String currentText = field.getText();
                // Only clear if it's actually the placeholder OR starts with "e.g.,"
                if (currentText.equals(placeholder) || currentText.startsWith("e.g.,")) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }

            public void focusLost(FocusEvent e) {
                String currentText = field.getText().trim();
                // Only restore placeholder if field is truly empty
                if (currentText.isEmpty()) {
                    field.setForeground(new Color(148, 163, 184));
                    field.setText(placeholder);
                } else {
                    // ✅ Ensure text color is correct for real values
                    field.setForeground(TEXT_PRIMARY);
                }
            }
        });

        return field;
    }
    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        actionPanel.setOpaque(false);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JButton saveBtn = createModernButton("💾 Save & Apply", SUCCESS_COLOR, SUCCESS_HOVER);
        JButton backBtn = createModernButton("← Back", PRIMARY_COLOR, PRIMARY_HOVER);

        saveBtn.addActionListener(e -> {
            if (saveSolarParameters()) {
                showSuccess("Solar parameters saved successfully!");
                // Navigate back to the last viewed appliance details (if any)
                navigateBack();
            }
        });

        backBtn.addActionListener(e -> navigateBack());

        actionPanel.add(saveBtn);
        actionPanel.add(backBtn);

        return actionPanel;
    }

    private void navigateBack() {
        // Check if we came from an appliance details panel
        if (mainPage.getLastViewedAppliance() != null) {
            // Return to the appliance details panel
            mainPage.showApplianceDetailsPanel(mainPage.getLastViewedAppliance());
        } else {
            // Default: go back to appliance list
            mainPage.showApplianceListPanel();
        }
    }

    private JButton createModernButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 44));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // ============================================================================
    // PUBLIC METHODS
    // ============================================================================


    /**
     * Load current values from MainPage storage
     */
    public void loadCurrentValues() {
        if (pshField != null && dodField != null && daysField != null && voltageCombo != null) {
            // Get current values from MainPage (our source of truth)
            double currentPsh = mainPage.getPeakSunHours();
            double currentDod = mainPage.getDepthOfDischarge();
            int currentDays = mainPage.getDaysOfAutonomy();
            int currentVoltage = mainPage.getSystemVoltage();

            // Debug logging
            System.out.println("=== SolarParametersPanel.loadCurrentValues() ===");
            System.out.println("From MainPage:");
            System.out.println("  Peak Sun Hours: " + currentPsh);
            System.out.println("  Depth of Discharge: " + currentDod);
            System.out.println("  Days of Autonomy: " + currentDays);
            System.out.println("  System Voltage: " + currentVoltage);

            // Update UI on EDT to ensure thread safety
            SwingUtilities.invokeLater(() -> {
                // Clear any placeholder styling
                pshField.putClientProperty("JTextField.placeholderText", "");
                dodField.putClientProperty("JTextField.placeholderText", "");
                daysField.putClientProperty("JTextField.placeholderText", "");

                // Use smart formatting: drop ".0" when value is whole
                pshField.setText(formatSmartDouble(currentPsh, 1));
                dodField.setText(formatSmartDouble(currentDod, 1));
                daysField.setText(String.valueOf(currentDays));
                voltageCombo.setSelectedItem(String.valueOf(currentVoltage));

                // Set proper styling
                pshField.setForeground(TEXT_PRIMARY);
                dodField.setForeground(TEXT_PRIMARY);
                daysField.setForeground(TEXT_PRIMARY);

                // Reset caret positions
                pshField.setCaretPosition(0);
                dodField.setCaretPosition(0);
                daysField.setCaretPosition(0);

                // Debug output
                System.out.println("  PSH field set to: '" + pshField.getText() + "'");
                System.out.println("  DoD field set to: '" + dodField.getText() + "'");
                System.out.println("  Days field set to: '" + daysField.getText() + "'");
                System.out.println("  Voltage combo set to: '" + voltageCombo.getSelectedItem() + "'");

                // Force immediate UI refresh
                pshField.revalidate();
                dodField.revalidate();
                daysField.revalidate();
                voltageCombo.revalidate();

                // Refresh the entire panel
                revalidate();
                repaint();

                // Verify the update
                System.out.println("After UI refresh:");
                System.out.println("  Days field now shows: '" + daysField.getText() + "'");
                System.out.println("  Days field foreground: " + daysField.getForeground());
            });

            System.out.println("===============================================");
        }
    }

    /**
     * Format a double but omit ".0" when it's a whole number.
     * @param value value to format
     * @param decimals number of decimals to show when not whole
     * @return formatted string
     */
    private static String formatSmartDouble(double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return "";
        long asLong = (long) value;
        if (Math.abs(value - asLong) < 1e-9) {
            return Long.toString(asLong);
        }
        String fmt = "%." + Math.max(0, decimals) + "f";
        return String.format(fmt, value);
    }
    /**
     * Save solar parameters to MainPage storage
     */
    public boolean saveSolarParameters() {
        try {
            double psh = Double.parseDouble(getFieldValue(pshField));
            double dod = Double.parseDouble(getFieldValue(dodField));
            int days = Integer.parseInt(getFieldValue(daysField));
            int voltage = Integer.parseInt((String) voltageCombo.getSelectedItem());

            // Validate ranges
            if (psh <= 0 || psh > 24) {
                showError("Peak Sun Hours must be between 0.1 and 24.");
                return false;
            }
            if (dod <= 0 || dod > 100) {
                showError("Depth of Discharge must be between 1 and 100%.");
                return false;
            }
            if (days <= 0 || days > 30) {
                showError("Days of Autonomy must be between 1 and 30.");
                return false;
            }

            // Save to MainPage (single source of truth)
            mainPage.setPeakSunHours(psh);
            mainPage.setDepthOfDischarge(dod);
            mainPage.setDaysOfAutonomy(days);
            mainPage.setSystemVoltage(voltage);

            // Also update all existing appliances with these values
            for (Model.Appliance appliance : mainPage.getAppliances()) {
                appliance.setPeakSunHours(psh);
                appliance.setDepthOfDischarge(dod);
                appliance.setDaysOfAutonomy(days);
                appliance.setSystemVoltage(voltage);
            }

            return true;

        } catch (NumberFormatException ex) {
            showError("Please enter valid numbers for all solar parameters.");
            return false;
        }
    }



    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private String getFieldValue(JTextField field) {
        String text = field.getText().trim();
        return text.startsWith("e.g.,") ? "" : text;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // ============================================================================
    // INNER CLASSES
    // ============================================================================

    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = 1;
            return insets;
        }
    }
}