package Main;

import javax.swing.*;
import java.awt.*;
import UI.*;

/**
 * The SolarApp class serves as the main entry point for the Solar Power System Calculator application.
 * It extends the JFrame class to provide a graphical user interface window and integrates the MainPage
 * as the primary application content. The class is responsible for initializing the application
 * window, setting its properties, and managing the overall layout.
 *
 * Key Responsibilities:
 * - Setting up the main application window with default configurations such as title, size, and layout.
 * - Integrating the MainPage as the central interface for navigating between application panels.
 * - Launching the application through the main method and ensuring it runs on the Event Dispatch Thread (EDT).
 *
 * Features:
 * - Resizable window with minimum size constraints for usability.
 * - Automatic positioning of the application window at the center of the screen.
 * - Dynamic layout adjustments using BorderLayout to allow flexible resizing of components.
 *
 * Usage:
 * This class is designed to be started via its main method, which instantiates the SolarApp,
 * configures the necessary properties, and sets it to be visible.
 */
public class SolarApp extends JFrame {

    public SolarApp() {
        setTitle("🔆 Solar Power System Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(new Dimension(1200, 800));
        setMinimumSize(new Dimension(900, 600));
        setResizable(true); // Allow manual resizing

        setLocationRelativeTo(null);

        // Let layout handle the sizing
        setLayout(new BorderLayout());

        // Add MainPage
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