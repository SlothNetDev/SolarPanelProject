package UI;

import Main.SolarApp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * WelcomePanel is a graphical user interface component that serves as the entry screen
 * for the application. It displays options to either start the application or quit, along
 * with visually appealing elements and animations.
 *
 * The panel includes features such as:
 * - A premium dark-themed gradient background.
 * - Glassmorphism-based UI design with rounded corners and subtle glow effects.
 * - Animated glowing elements to enhance visual appeal.
 * - Centralized content arranged using a flexible layout.
 * - Buttons styled with hover effects and distinct colors for success and danger actions.
 * - A dynamic pulsating effect on selected UI elements.
 *
 * The panel ensures a visually engaging and user-friendly experience while providing
 * straightforward navigation to the primary application functions.
 *
 * It also integrates with a provided MainPage instance to facilitate navigation between
 * different sections of the application.
 */
//WelcomePanel — Entry screen with "Start" and "Quit" options, with enhanced UI aesthetics.
public class WelcomePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private MainPage mainPage;

    // Premium color palette
    private static final Color BG_START = new Color(15, 23, 42);           // Deep Dark Blue
    private static final Color BG_END = new Color(30, 41, 59);             // Slate
    private static final Color CARD_COLOR = new Color(30, 41, 59, 230);    // Semi-transparent slate
    private static final Color ACCENT_PRIMARY = new Color(56, 189, 248);   // Cyan
    private static final Color ACCENT_GLOW = new Color(14, 165, 233);      // Bright Cyan
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);     // Emerald
    private static final Color SUCCESS_HOVER = new Color(22, 163, 74);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);      // Red
    private static final Color DANGER_HOVER = new Color(220, 38, 38);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);    // Almost White
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);  // Slate Gray

    private Timer pulseTimer;
    private float pulseAlpha = 0.3f;
    private boolean pulseIncreasing = true;

    public WelcomePanel(MainPage mainPage) {
        this.mainPage = mainPage;

        setLayout(new GridBagLayout());

        // Animated pulse effect for the glow
        pulseTimer = new Timer(50, e -> {
            if (pulseIncreasing) {
                pulseAlpha += 0.02f;
                if (pulseAlpha >= 0.6f) pulseIncreasing = false;
            } else {
                pulseAlpha -= 0.02f;
                if (pulseAlpha <= 0.3f) pulseIncreasing = true;
            }
            repaint();
        });
        pulseTimer.start();

        // Main Content Container
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Glass morphism effect
                g2d.setColor(CARD_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Subtle border glow
                g2d.setColor(new Color(56, 189, 248, 40));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 30, 30);
            }
        };

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        // Animated solar icon with glow
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 110;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Outer glow (pulsing)
                int glowSize = size + 40;
                int glowX = (getWidth() - glowSize) / 2;
                int glowY = (getHeight() - glowSize) / 2;

                RadialGradientPaint outerGlow = new RadialGradientPaint(
                        getWidth() / 2f, getHeight() / 2f, glowSize / 2f,
                        new float[]{0f, 1f},
                        new Color[]{
                                new Color(251, 191, 36, (int)(pulseAlpha * 255)),
                                new Color(251, 191, 36, 0)
                        }
                );
                g2d.setPaint(outerGlow);
                g2d.fillOval(glowX, glowY, glowSize, glowSize);

                // Main gradient circle
                RadialGradientPaint gradient = new RadialGradientPaint(
                        getWidth() / 2f, getHeight() / 2f, size / 2f,
                        new float[]{0f, 0.7f, 1f},
                        new Color[]{
                                new Color(253, 224, 71),
                                new Color(251, 191, 36),
                                new Color(245, 158, 11)
                        }
                );
                g2d.setPaint(gradient);
                g2d.fillOval(x, y, size, size);

                // Inner highlight
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillOval(x + 15, y + 15, 40, 40);
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(150, 150));
        iconPanel.setMaximumSize(new Dimension(150, 150));
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconPanel.setLayout(new GridBagLayout());

        contentPanel.add(iconPanel);
        contentPanel.add(Box.createVerticalStrut(35));

        // Title with glow effect
        JLabel title = new JLabel("Solar Calculator") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Text shadow/glow
                g2d.setColor(new Color(56, 189, 248, 80));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = fm.getAscent();
                g2d.drawString(getText(), x+2, y+2);

                super.paintComponent(g);
            }
        };
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(title);

        contentPanel.add(Box.createVerticalStrut(15));

        // Minimal impactful subtitle
        JLabel subtitle = new JLabel("Power Your Future");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(ACCENT_PRIMARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subtitle);

        contentPanel.add(Box.createVerticalStrut(60));

        // Premium buttons
        JButton startBtn = createPremiumButton("Get Started", SUCCESS_COLOR, SUCCESS_HOVER, true);
        JButton quitBtn = createPremiumButton("Exit", DANGER_COLOR, DANGER_HOVER, false);

        startBtn.addActionListener(e -> mainPage.showLoadInputPanel());
        quitBtn.addActionListener(e -> System.exit(0));

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 0));

        btnPanel.add(startBtn);
        btnPanel.add(quitBtn);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(btnPanel);

        add(contentPanel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark gradient background
        GradientPaint gradient = new GradientPaint(
                0, 0, BG_START,
                getWidth(), getHeight(), BG_END
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Ambient light particles effect
        g2d.setColor(new Color(56, 189, 248, 30));
        for (int i = 0; i < 20; i++) {
            int x = (int)(Math.sin(i * 0.5) * getWidth() * 0.4 + getWidth() * 0.5);
            int y = (int)(Math.cos(i * 0.7) * getHeight() * 0.4 + getHeight() * 0.5);
            int size = (int)(Math.sin(i * 1.2) * 3 + 5);
            g2d.fillOval(x, y, size, size);
        }
    }

    private JButton createPremiumButton(String text, Color baseColor, Color hoverColor, boolean isPrimary) {
        JButton btn = new JButton(text) {
            private boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button background with gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, getBackground(),
                        0, getHeight(), getBackground().darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // Glow effect on hover
                if (isHovered && isPrimary) {
                    g2d.setColor(new Color(getBackground().getRed(),
                            getBackground().getGreen(),
                            getBackground().getBlue(), 60));
                    g2d.fillRoundRect(-4, -4, getWidth()+8, getHeight()+8, 20, 20);
                }

                // Inner highlight
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.fillRoundRect(2, 2, getWidth()-4, getHeight()/2-2, 14, 14);

                super.paintComponent(g);
            }

            @Override
            public void setBackground(Color bg) {
                super.setBackground(bg);
                repaint();
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, isPrimary ? 18 : 16));
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(isPrimary ? 200 : 140, isPrimary ? 56 : 50));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JButton)e.getSource()).putClientProperty("isHovered", true);
                btn.setBackground(hoverColor);
                btn.setFont(new Font("Segoe UI", Font.BOLD, isPrimary ? 19 : 17));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JButton)e.getSource()).putClientProperty("isHovered", false);
                btn.setBackground(baseColor);
                btn.setFont(new Font("Segoe UI", Font.BOLD, isPrimary ? 18 : 16));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(baseColor.darker().darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBackground(hoverColor);
            }
        });

        return btn;
    }
}