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

    //Do calculations
    /**
     * Total daily energy consumption (Wh/day) from a list of appliances.
     * CORRECT: This is correct - sums up all appliance energy consumption
     */
    public double totalDailyEnergyWh(List<Appliance> appliances) {
        double sum = 0.0;
        for (Appliance a : appliances) {
            sum += a.energyPerDayWh();
        }
        return Math.ceil(sum); // Round up to be conservative
    }

    /**
     * Required PV array power in Watts.
     * FIXED: Account for system losses and use proper solar insolation formula
     */
    public double requiredPvWatts(double totalWh, double peakSunHours) {
        if (peakSunHours <= 0) {
            JOptionPane.showMessageDialog(null, "Peak Sun Hours must be greater than 0.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        // CORRECTED FORMULA:
        // PV Watts = (Daily Energy / Peak Sun Hours) / System Efficiency
        // This accounts for all system losses
        double pvWatts = (totalWh / peakSunHours) / systemEfficiency;

        // Add 10% safety margin for PV degradation over time
        return Math.ceil(pvWatts * 1.1);
    }

    /**
     * Required battery capacity in Ah.
     * FIXED: Account for battery efficiency and depth of discharge properly
     */
    public double requiredBatteryAh(double totalWh, int daysOfAutonomy, double dodPercent, int systemVoltage) {
        if (dodPercent <= 0 || dodPercent > 100) {
            JOptionPane.showMessageDialog(null, "Depth of Discharge must be between 1 and 100%.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        if (systemVoltage <= 0) {
            JOptionPane.showMessageDialog(null, "System voltage must be greater than 0.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        if (daysOfAutonomy <= 0) {
            JOptionPane.showMessageDialog(null, "Days of Autonomy must be greater than 0.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        // CORRECTED FORMULA:
        // Battery Ah = (Daily Energy × Days of Autonomy) / (System Voltage × DoD × Battery Efficiency)
        double dodFraction = dodPercent / 100.0;
        double totalEnergyNeeded = totalWh * daysOfAutonomy;

        // Account for battery efficiency (energy lost during charge/discharge)
        double usableEnergy = totalEnergyNeeded / batteryEfficiency;

        double ah = usableEnergy / (systemVoltage * dodFraction);

        return Math.ceil(ah);
    }

    /**
     * Recommended inverter size in Watts.
     * IMPROVED: Ensure minimum inverter size for surge capacity
     */
    public double recommendedInverterW(double peakLoadWatts) {
        if (peakLoadWatts <= 0) {
            JOptionPane.showMessageDialog(null, "Peak load must be greater than 0.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        double baseInverterSize = peakLoadWatts * inverterSafetyFactor;

        // Ensure minimum inverter size for small systems (500W minimum)
        double minInverterSize = 500;

        return Math.ceil(Math.max(baseInverterSize, minInverterSize));
    }

    /**
     * Recommended charge controller in Amperes.
     * FIXED: Use proper PV current calculation with safety margins
     */
    public double recommendedControllerA(double pvWatts, int systemVoltage) {
        if (pvWatts <= 0) {
            JOptionPane.showMessageDialog(null, "Calculated PV watts must be greater than 0.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        if (systemVoltage <= 0) {
            JOptionPane.showMessageDialog(null, "System voltage must be greater than 0.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        // CORRECTED FORMULA:
        // Controller Current = (PV Watts / System Voltage) × Safety Factor
        // This accounts for peak power conditions and temperature derating
        double iscEstimate = pvWatts / systemVoltage;
        double controllerCurrent = iscEstimate * controllerSafetyFactor;

        // Round up to nearest standard controller size
        return roundToStandardControllerSize(Math.ceil(controllerCurrent));
    }

    /**
     * Calculate peak load watts (considering simultaneous operation).
     * IMPROVED: Consider if appliances might run simultaneously
     */
    public double peakLoadWatts(List<Appliance> appliances) {
        double sum = 0.0;
        for (Appliance a : appliances) {
            sum += a.getWatts() * a.getQuantity();
        }

        // For residential systems, apply diversity factor (not all appliances run at once)
        // Commercial/industrial might use 100%, residential typically 70-80%
        double diversityFactor = 0.8;

        return Math.ceil(sum * diversityFactor);
    }

    /**
     * Additional useful calculation: System autonomy in hours
     */
    public double systemAutonomyHours(double batteryAh, int systemVoltage, double dodPercent, double continuousLoadW) {
        if (continuousLoadW <= 0) return 0;

        double usableEnergyWh = batteryAh * systemVoltage * (dodPercent / 100.0) * batteryEfficiency;
        return usableEnergyWh / continuousLoadW;
    }

    /**
     * Additional: Daily solar energy production estimate
     */
    public double dailySolarProductionWh(double pvWatts, double peakSunHours) {
        return pvWatts * peakSunHours * systemEfficiency;
    }

    // ---- Helper methods ----

    /**
     * Round to standard charge controller sizes (10, 15, 20, 30, 40, 50, 60, 80, 100A)
     */
    private double roundToStandardControllerSize(double current) {
        int[] standardSizes = {10, 15, 20, 30, 40, 50, 60, 80, 100};
        for (int size : standardSizes) {
            if (current <= size) {
                return size;
            }
        }
        // If larger than 100A, round up to next multiple of 20
        return Math.ceil(current / 20) * 20;
    }

    // ---- Getters for configuration values ----
    public double getSystemEfficiency() { return systemEfficiency; }
    public double getInverterSafetyFactor() { return inverterSafetyFactor; }
    public double getControllerSafetyFactor() { return controllerSafetyFactor; }
    public double getBatteryEfficiency() { return batteryEfficiency; }

}
