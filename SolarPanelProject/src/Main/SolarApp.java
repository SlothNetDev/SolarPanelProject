package Main;

import javax.swing.*;
import java.awt.*;
import UI.*;

public class SolarApp extends JFrame {

    public SolarApp() {
        setTitle("🔆 Solar Power System Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Flexibility Changes ---
        // 1. Removed setExtendedState(JFrame.MAXIMIZED_BOTH)
        //    The app will start in a standard window, giving the user control.
        // 2. Set an initial size (e.g., 1200x800) for a good starting point.
        setSize(new Dimension(1200, 800));
        setMinimumSize(new Dimension(900, 600));
        setResizable(true); // Allow manual resizing

        // Center the frame on the screen when it starts
        setLocationRelativeTo(null);

        // Let layout handle the sizing
        setLayout(new BorderLayout());

        // Add MainPage (which manages the panels)
        MainPage mainPage = new MainPage();
        // The MainPage fills the entire frame (CENTER) and will resize flexibly.
        add(mainPage, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SolarApp().setVisible(true);
        });
    }
}