package Model;
import java.util.List;
import javax.swing.JOptionPane;
// SolarCalculator - core calculation engine with corrected formulas.
// Based on industry-standard solar system design principles.
public class SolarCalculator {
    // Default configuration values (industry standards)
    private double systemEfficiency = 0.85; // 85% (battery + wiring + inverter losses)
    private double inverterSafetyFactor = 1.25; // 25% safety margin
    private double controllerSafetyFactor = 1.25; // 25% safety margin
    private double batteryEfficiency = 0.85; // 85% battery round-trip efficiency

    // ---- Configuration setters ----
    public void setSystemEfficiency(double systemEfficiency) {
        if (systemEfficiency <= 0 || systemEfficiency > 1) {
            JOptionPane.showMessageDialog(null, "System efficiency must be between 0 and 1 (e.g., 0.85).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.systemEfficiency = systemEfficiency;
    }

    public void setInverterSafetyFactor(double inverterSafetyFactor) {
        if (inverterSafetyFactor <= 0) {
            JOptionPane.showMessageDialog(null, "Inverter safety factor must be > 0.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.inverterSafetyFactor = inverterSafetyFactor;
    }

    public void setControllerSafetyFactor(double controllerSafetyFactor) {
        if (controllerSafetyFactor <= 0) {
            JOptionPane.showMessageDialog(null, "Controller safety factor must be > 0.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.controllerSafetyFactor = controllerSafetyFactor;
    }

    public void setBatteryEfficiency(double batteryEfficiency) {
        if (batteryEfficiency <= 0 || batteryEfficiency > 1) {
            JOptionPane.showMessageDialog(null, "Battery efficiency must be between 0 and 1 (e.g., 0.85).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.batteryEfficiency = batteryEfficiency;
    }
}
