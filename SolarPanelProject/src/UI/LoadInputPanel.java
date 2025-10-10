package UI;
import Model.Appliance;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
public class LoadInputPanel {
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

}
