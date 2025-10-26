package UI;

import Model.Appliance;
import Utils.ProjectManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * The LoadInputPanel class represents a user interface panel for adding details
 * about electrical appliances. It provides input fields for appliance name,
 * wattage, quantity, and usage hours, along with an option for enabling auto-redirection.
 * This class is intended to be used as part of the MainPage for interacting with
 * appliance-related data in a solar system calculator application.
 *
 * The panel includes various utility methods for creating UI components,
 * handling input field operations, and displaying feedback messages to the user.
 */
public class LoadInputPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final MainPage mainPage;

    private JTextField nameField, wattsField, qtyField, hoursField;

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

    // Auto-redirect option
    private JCheckBox autoRedirectCheckbox;

    public LoadInputPanel(MainPage mainPage) {
        this.mainPage = mainPage;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(createMainContent());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        add(loadingFile(), BorderLayout.NORTH);add(loadingFile(), BorderLayout.NORTH);
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
        JPanel nav = createNavigationPanel();

        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(header);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(card);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(nav);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(contentPanel, gbc);

        return wrapper;
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel("⚡", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Add Appliance Details", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));

        JLabel subtitle = new JLabel("Enter the specifications for your electrical appliances", SwingConstants.CENTER);
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
    private JPanel loadingFile(){
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(new Color(245, 245, 245));
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

        toolbar.add(loadBtn);
        return toolbar;
    }
    private JPanel createFormCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 12),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)
        ));

        nameField = createInputField("e.g., LED Bulb");
        wattsField = createInputField("e.g., 10");
        qtyField = createInputField("e.g., 5");
        hoursField = createInputField("e.g., 4.5");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        card.add(createFormRow("Appliance Name", nameField, "What is the appliance?"), gbc);
        gbc.gridy++;
        card.add(createFormRow("Power (Watts)", wattsField, "Power consumption per unit"), gbc);
        gbc.gridy++;
        card.add(createFormRow("Quantity", qtyField, "Number of units"), gbc);
        gbc.gridy++;
        card.add(createFormRow("Hours per Day", hoursField, "Daily usage duration"), gbc);
        gbc.gridy++;

        // Add auto-redirect checkbox
        gbc.gridy++;
        card.add(createAutoRedirectOption(), gbc);
        gbc.gridy++;

        card.add(Box.createVerticalStrut(20), gbc);
        gbc.gridy++;

        JButton addBtn = createModernButton("➕ Add Appliance", PRIMARY_COLOR, PRIMARY_HOVER);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(250, 45));
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(addBtn, gbc);

        // Hook up event to add appliance
        addBtn.addActionListener(e -> addAppliance());

        return card;
    }

    private JPanel createAutoRedirectOption() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(300, 40));

        autoRedirectCheckbox = new JCheckBox("Go to appliance list after adding");
        autoRedirectCheckbox.setSelected(true); // Default to enabled
        autoRedirectCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        autoRedirectCheckbox.setForeground(TEXT_SECONDARY);
        autoRedirectCheckbox.setOpaque(false);
        autoRedirectCheckbox.setFocusPainted(false);

        panel.add(autoRedirectCheckbox);
        return panel;
    }

    private JPanel createFormRow(String labelText, JTextField field, String helpText) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setMaximumSize(new Dimension(250, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(INPUT_BORDER, 8),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel help = new JLabel(helpText);
        help.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        help.setForeground(TEXT_SECONDARY);
        help.setAlignmentX(Component.CENTER_ALIGNMENT);

        row.add(label);
        row.add(Box.createVerticalStrut(6));
        row.add(field);
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
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }

            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(new Color(148, 163, 184));
                    field.setText(placeholder);
                }
            }
        });

        return field;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        navPanel.setOpaque(false);
        navPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton viewListBtn = createModernButton("📋 View Appliance List →", SUCCESS_COLOR, SUCCESS_HOVER);

        viewListBtn.addActionListener(e -> {
            if (mainPage.getAppliances().isEmpty()) {
                showWarning("Please add at least one appliance first.");
                return;
            }
            goToApplianceList();
        });
        navPanel.add(viewListBtn);

        return navPanel;
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
        button.setPreferredSize(new Dimension(220, 44));

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

    private void addAppliance() {
        String name = getFieldValue(nameField);
        if (name.isEmpty()) {
            showError("Please enter an appliance name.");
            return;
        }

        // Validate name length and format
        if (name.length() < 2 || name.length() > 30) {
            showError("Appliance name must be between 2 and 30 characters.");
            return;
        }
        if (!name.matches("[a-zA-Z0-9\\s]+")) {
            showError("Appliance name can only contain letters, numbers, and spaces.");
            return;
        }

        try {
            String wattsStr = getFieldValue(wattsField);
            String qtyStr = getFieldValue(qtyField);
            String hoursStr = getFieldValue(hoursField);

            double watts = Double.parseDouble(wattsStr);
            int qty = Integer.parseInt(qtyStr);
            double hours = Double.parseDouble(hoursStr);

            // ✅ Numeric value validation
            if (watts <= 0 || watts > 10000) {
                showError("Power (Watts) must be between 1 and 10,000.");
                return;
            }

            if (qty <= 0 || qty > 10000) {
                showError("Quantity must be between 1 and 10000.");
                return;
            }

            if (hours <= 0 || hours > 24) {
                showError("Hours per day must be between 0.1 and 24.");
                return;
            }

            // ✅ Passed all validations
            Appliance appliance = new Appliance(name, watts, qty, hours);
            mainPage.getAppliances().add(appliance);

            showSuccess(String.format("✓ Added: %s (%dW × %d)", name, (int) watts, qty));

            // Clear input fields for next entry
            clearField(nameField, "e.g., LED Bulb");
            clearField(wattsField, "e.g., 10");
            clearField(qtyField, "e.g., 5");
            clearField(hoursField, "e.g., 4.5");

            // Auto-redirect after success
            if (autoRedirectCheckbox.isSelected()) {
                Timer timer = new Timer(800, e -> goToApplianceList());
                timer.setRepeats(false);
                timer.start();
            } else {
                nameField.requestFocus();
            }

        } catch (NumberFormatException ex) {
            showError("Please enter valid numbers for Watts, Quantity, and Hours.");
        }
    }


    private void goToApplianceList() {
        // ✅ No need to transfer - just navigate
        mainPage.showApplianceListPanel();
    }

    private String getFieldValue(JTextField field) {
        String text = field.getText().trim();
        if (text.startsWith("e.g.,") || text.isEmpty()) return "";
        return text;
    }

    private void clearField(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(new Color(148, 163, 184));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

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