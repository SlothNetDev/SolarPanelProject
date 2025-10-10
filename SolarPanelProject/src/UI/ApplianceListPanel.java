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
    private JPanel createListCard() {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 12),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel infoBar = createInfoBar();
        card.add(infoBar, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        applianceList = new JList<>(listModel);
        applianceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // We'll manage checks manually
        applianceList.setFixedCellHeight(70);
        applianceList.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applianceList.setBackground(CARD_BG);
        applianceList.setCellRenderer(new ModernApplianceRenderer());

        JScrollPane scrollPane = new JScrollPane(applianceList);
        scrollPane.setBorder(new RoundedBorder(BORDER_COLOR, 8));
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setPreferredSize(new Dimension(0, 400));

        card.add(scrollPane, BorderLayout.CENTER);

        setupListInteractions();
        refreshList();

        return card;
    }

    private JPanel createInfoBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JLabel countLabel = new JLabel(appliances.size() + " appliance(s) added");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        countLabel.setForeground(TEXT_SECONDARY);

        JLabel hintLabel = new JLabel("Click checkboxes to select • Double-click for details");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hintLabel.setForeground(TEXT_SECONDARY);

        bar.add(countLabel, BorderLayout.WEST);
        bar.add(hintLabel, BorderLayout.EAST);

        return bar;
    }

    private void setupListInteractions() {
        applianceList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = applianceList.locationToIndex(e.getPoint());
                if (index < 0) return;

                Rectangle bounds = applianceList.getCellBounds(index, index);
                if (bounds != null && e.getX() < bounds.x + 30) { // Checkbox area
                    if (checkedIndices.contains(index)) {
                        checkedIndices.remove(index);
                    } else {
                        checkedIndices.add(index);
                    }
                    applianceList.repaint(bounds);
                    updateDeleteButton();
                } else if (e.getClickCount() == 2) {
                    Appliance selected = listModel.get(index);
                    mainPage.showApplianceDetailsPanel(selected);
                }
            }
        });
    }
    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        deleteBtn = createModernButton("🗑️ Delete Selected (0)", DELETE_COLOR, DELETE_HOVER, true);
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> deleteSelectedAppliances());

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        navPanel.setOpaque(false);
        navPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton backBtn = createModernButton("← Back to Add Appliances", BACK_COLOR, BACK_HOVER, false);
        backBtn.addActionListener(e -> mainPage.showLoadInputPanel());

        navPanel.add(backBtn);

        panel.add(deleteBtn);
        panel.add(Box.createVerticalStrut(15));
        panel.add(navPanel);

        return panel;
    }

    private void updateDeleteButton() {
        if (deleteBtn == null) return; // ✅ Prevent crash if not yet initialized

        int selectedCount = checkedIndices.size();
        if (selectedCount > 0) {
            deleteBtn.setEnabled(true);
            deleteBtn.setText("🗑️ Delete Selected (" + selectedCount + ")");
            deleteBtn.setForeground(DELETE_COLOR);
        } else {
            deleteBtn.setEnabled(false);
            deleteBtn.setText("🗑️ Delete Selected (0)");
            deleteBtn.setForeground(new Color(156, 163, 175));
        }
    }
    private JButton createModernButton(String text, Color bgColor, Color hoverColor, boolean outline) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(220, 44));

        if (outline) {
            button.setBackground(CARD_BG);
            button.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(bgColor, 8),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
            ));
            button.setForeground(bgColor);
        } else {
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setOpaque(true);
        }

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(outline ? new Color(254, 242, 242) : hoverColor);
                }
            }

            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(outline ? CARD_BG : bgColor);
                }
            }
        });

        return button;
    }

    public void refreshList() {
        if (listModel == null) return;
        listModel.clear();
        checkedIndices.clear();
        for (Appliance a : appliances) listModel.addElement(a);
        updateDeleteButton();
    }
}
