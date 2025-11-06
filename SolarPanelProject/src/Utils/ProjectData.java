package Utils;

import Model.Appliance;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Data class for serializing/deserializing solar projects
 *
 * Notes:
 * - We add @SerializedName with alternate names for fields which might have been saved
 *   under different keys in older JSON files (e.g. "days" vs "daysOfAutonomy").
 * - This prevents Gson from leaving those fields at defaults when the JSON uses legacy keys.
 */
public class ProjectData {
    private List<Appliance> appliances;

    @SerializedName(value = "peakSunHours", alternate = {"psh", "peak_sun_hours", "PeakSunHours"})
    private double peakSunHours;

    @SerializedName(value = "depthOfDischarge", alternate = {"dod", "depth_of_discharge", "DepthOfDischarge"})
    private double depthOfDischarge;

    // Accept both "daysOfAutonomy" and legacy keys like "days" or "Days"
    @SerializedName(value = "daysOfAutonomy", alternate = {"days", "Days", "days_of_autonomy", "DaysOfAutonomy"})
    private int daysOfAutonomy;

    @SerializedName(value = "systemVoltage", alternate = {"voltage", "system_voltage", "SystemVoltage"})
    private int systemVoltage;

    // Default constructor for Gson
    public ProjectData() {
    }

    // Constructor with parameters
    public ProjectData(List<Appliance> appliances, double peakSunHours,
                       double depthOfDischarge, int daysOfAutonomy, int systemVoltage) {
        this.appliances = appliances;
        this.peakSunHours = peakSunHours;
        this.depthOfDischarge = depthOfDischarge;
        this.daysOfAutonomy = daysOfAutonomy;
        this.systemVoltage = systemVoltage;
    }

    // Getters and setters
    public List<Appliance> getAppliances() {
        return appliances;
    }

    public void setAppliances(List<Appliance> appliances) {
        this.appliances = appliances;
    }

    public double getPeakSunHours() {
        return peakSunHours;
    }

    public void setPeakSunHours(double peakSunHours) {
        this.peakSunHours = peakSunHours;
    }

    public double getDepthOfDischarge() {
        return depthOfDischarge;
    }

    public void setDepthOfDischarge(double depthOfDischarge) {
        this.depthOfDischarge = depthOfDischarge;
    }

    public int getDaysOfAutonomy() {
        return daysOfAutonomy;
    }

    public void setDaysOfAutonomy(int daysOfAutonomy) {
        this.daysOfAutonomy = daysOfAutonomy;
    }

    public int getSystemVoltage() {
        return systemVoltage;
    }

    public void setSystemVoltage(int systemVoltage) {
        this.systemVoltage = systemVoltage;
    }

    @Override
    public String toString() {
        return "ProjectData{" +
                "appliances=" + (appliances == null ? "null" : appliances.size()) +
                ", peakSunHours=" + peakSunHours +
                ", depthOfDischarge=" + depthOfDischarge +
                ", daysOfAutonomy=" + daysOfAutonomy +
                ", systemVoltage=" + systemVoltage +
                '}';
    }
}