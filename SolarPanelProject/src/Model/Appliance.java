package Model;
import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Represents an electronic or electrical appliance with defined power consumption,
 * quantity, usage hours, and solar power system parameters.
 */
public class Appliance {
    private String name;
    private double watts;
    private int quantity;
    private double hoursPerDay;

    // Individual solar parameters for each appliance
    private double peakSunHours;
    private double depthOfDischarge;
    private int daysOfAutonomy;
    private int systemVoltage;

    public Appliance(String name, double watts, int quantity, double hoursPerDay) {
        this.name = name;
        this.watts = watts;
        this.quantity = quantity;
        this.hoursPerDay = hoursPerDay;
    }
    // NEW: Enhanced validation to include solar parameters
    public static boolean validateInputs(Component parent, String name, String wattsText,
                                         String qtyText, String hoursText, String pshText,
                                         String dodText, String daysText, String voltageText) {
        // Existing validation for basic fields
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Appliance name cannot be empty.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate basic numeric fields (existing code)
        double watts, hours;
        int qty;
        try {
            watts = Double.parseDouble(wattsText.trim());
            qty = Integer.parseInt(qtyText.trim());
            hours = Double.parseDouble(hoursText.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "Please enter valid numbers for Watts, Quantity, and Hours.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate solar parameters
        double psh, dod;
        int days, voltage;
        try {
            psh = Double.parseDouble(pshText.trim());
            dod = Double.parseDouble(dodText.trim());
            days = Integer.parseInt(daysText.trim());
            voltage = Integer.parseInt(voltageText.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(parent, "Please enter valid numbers for solar parameters.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Range validation
        if (watts <= 0 || qty <= 0 || hours < 0 || hours > 24) {
            JOptionPane.showMessageDialog(parent, "Please check: Watts>0, Quantity>0, Hours 0-24", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (psh <= 0 || psh > 24 || dod <= 0 || dod > 100 || days <= 0 || voltage <= 0) {
            JOptionPane.showMessageDialog(parent, "Please check: PSH 0-24, DoD 1-100%, Days>0, Voltage>0", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    //Create Getter and Setter methods for each field
    // ===== Getters / Setters =====
    public String getName() { return name; }
    public double getWatts() { return watts; }
    public int getQuantity() { return quantity; }
    public double getHoursPerDay() { return hoursPerDay; }

    // NEW: Solar parameter getters/setters
    public double getPeakSunHours() { return peakSunHours; }
    public double getDepthOfDischarge() { return depthOfDischarge; }
    public int getDaysOfAutonomy() { return daysOfAutonomy; }
    public int getSystemVoltage() { return systemVoltage; }

    public void setName(String name) { this.name = name; }
    public void setWatts(double watts) { this.watts = watts; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setHoursPerDay(double hoursPerDay) { this.hoursPerDay = hoursPerDay; }

    // NEW: Solar parameter setters
    public void setPeakSunHours(double peakSunHours) { this.peakSunHours = peakSunHours; }
    public void setDepthOfDischarge(double depthOfDischarge) { this.depthOfDischarge = depthOfDischarge; }
    public void setDaysOfAutonomy(int daysOfAutonomy) { this.daysOfAutonomy = daysOfAutonomy; }
    public void setSystemVoltage(int systemVoltage) { this.systemVoltage = systemVoltage; }

    /**
     * Energy consumed by this appliance per day in Wh.
     */
    public double energyPerDayWh() {
        return watts * quantity * hoursPerDay;
    }

    /**
     * NEW: Calculate individual system requirements for this appliance
     */
    public double calculateRequiredPvWatts(SolarCalculator calc) {
        double totalWh = energyPerDayWh();
        return calc.requiredPvWatts(totalWh, peakSunHours);
    }

    public double calculateRequiredBatteryAh(SolarCalculator calc) {
        double totalWh = energyPerDayWh();
        return calc.requiredBatteryAh(totalWh, daysOfAutonomy, depthOfDischarge, systemVoltage);
    }

    public double calculateRequiredInverterW(SolarCalculator calc) {
        return calc.recommendedInverterW(watts * quantity);
    }

    public double calculateRequiredControllerA(SolarCalculator calc) {
        double pvWatts = calculateRequiredPvWatts(calc);
        return calc.recommendedControllerA(pvWatts, systemVoltage);
    }
}