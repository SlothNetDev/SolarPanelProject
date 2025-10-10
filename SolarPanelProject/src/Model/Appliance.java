package Model;
import javax.swing.JOptionPane;
import java.awt.Component;

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


}