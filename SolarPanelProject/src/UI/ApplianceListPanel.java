package UI;
import Model.Appliance;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class ApplianceListPanel {
    private static final long serialVersionUID = 1L;
    private final MainPage mainPage;
    private final List<Appliance> appliances;

    private DefaultListModel<Appliance> listModel;
    private JList<Appliance> applianceList;
    private JButton deleteBtn;

    // Store manually checked indices
    private final Set<Integer> checkedIndices = new HashSet<>();

    // Modern color palette
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color ACCENT_COLOR = new Color(59, 130, 246);
    private static final Color DELETE_COLOR = new Color(239, 68, 68);
    private static final Color DELETE_HOVER = new Color(220, 38, 38);
    private static final Color BACK_COLOR = new Color(100, 116, 139);
    private static final Color BACK_HOVER = new Color(71, 85, 105);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color SELECTED_BG = new Color(219, 234, 254);

    public ApplianceListPanel(MainPage mainPage, List<Appliance> appliances) {
        this.appliances = appliances;
        this.mainPage = mainPage;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JScrollPane mainScroll = new JScrollPane(createMainContent());
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }
    private JPanel createMainContent() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));

        contentPanel.add(createHeader());
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(createListCard());
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(createActionPanel());

        wrapper.add(contentPanel, new GridBagConstraints());
        return wrapper;
    }
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel icon = new JLabel("📋", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Your Appliances", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Review and manage your appliance list", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(icon);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitle);

        return headerPanel;
    }

}
