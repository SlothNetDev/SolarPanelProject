package UI;

import Main.SolarApp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

//WelcomePanel — Entry screen with "Start" and "Quit" options, with enhanced UI aesthetics.
public class WelcomePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private MainPage mainPage;

    // Define a consistent modern color palette
    private static final Color BACKGROUND_COLOR = new Color(245, 248, 255); // Light Gray/Blue-White
    private static final Color PRIMARY_COLOR = new Color(46, 204, 113);    // Green (Start Button)
    private static final Color ACCENT_COLOR = new Color(52, 152, 219);     // Blue (Title Accent)
    private static final Color QUIT_COLOR = new Color(231, 76, 60);        // Red (Quit Button)
    private static final Color TEXT_COLOR = new Color(44, 62, 80);         // Dark Text

    public WelcomePanel(MainPage mainPage) {
        this.mainPage = mainPage;

        // Use a centralized panel with a CardLayout to achieve a clean, centered look
        setLayout(new GridBagLayout()); // Use GridBagLayout to center the main content panel
        setBackground(BACKGROUND_COLOR);

        //Main Content Container (to apply padding and a soft container look
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1), // Light border
                        BorderFactory.createEmptyBorder(60, 50, 60, 50) // Internal padding
                )
        );

        // Title
        JLabel title = new JLabel("🔆 Solar Load Calculator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(ACCENT_COLOR); // Use the blue accent color
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(title);

        // Add vertical space
        contentPanel.add(Box.createVerticalStrut(10));

        // --- Separator Line ---
        JPanel separator = new JPanel();
        separator.setMaximumSize(new Dimension(250, 2));
        separator.setBackground(PRIMARY_COLOR);
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(separator);

        // Add vertical space
        contentPanel.add(Box.createVerticalStrut(30));

        // Center Message
        JTextArea message = new JTextArea(
                "Welcome! This app helps estimate your solar energy needs.\n\n"
                        + "Click 'Start the App' to begin entering your appliances."
        );
        message.setEditable(false);
        message.setWrapStyleWord(true);
        message.setLineWrap(true);
        message.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        message.setForeground(TEXT_COLOR);
        message.setBackground(Color.WHITE);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);
        message.setMaximumSize(new Dimension(400, 100)); // Limit width for better readability
        // Center the text within the JTextArea
        message.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        contentPanel.add(message);

        // Add vertical space
        contentPanel.add(Box.createVerticalStrut(40));

        // --- Buttons ---
        JButton startBtn = createButton("▶ Start the App", PRIMARY_COLOR);
        JButton quitBtn = createButton("⏻ Quit", QUIT_COLOR);

        startBtn.addActionListener(e -> mainPage.showLoadInputPanel());
        quitBtn.addActionListener(e -> System.exit(0));

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 0)); // Center buttons with more space

        btnPanel.add(startBtn);
        btnPanel.add(quitBtn);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(btnPanel);

        // Add the main content panel to the WelcomePanel
        add(contentPanel);
    }

    // Custom button creator with a modern, flat style and hover effect
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);

        // Use a more substantial border with a slight roundness
        btn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));

        // Add a simple hover effect for better user feedback
        Color darkerColor = color.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(darkerColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });

        return btn;
    }
}