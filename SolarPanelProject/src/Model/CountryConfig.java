package Model;

/**
 * Represents configuration details related to a specific country for solar energy systems.
 * Each country configuration includes the country's display name, currency symbol, and various cost factors
 * associated with solar energy components and installation.
 */
public enum CountryConfig {
    PHILIPPINES("Philippines", "₱", 10.0, 35.0, 120.0, 15.0, 500.0, 18000.0, 12000.0),
    USA("United States", "$", 0.18, 0.50, 2.0, 0.30, 10.0, 500.0, 300.0),
    AUSTRALIA("Australia", "A$", 0.35, 0.80, 2.8, 0.45, 15.0, 700.0, 450.0),
    INDIA("India", "₹", 6.5, 25.0, 150.0, 12.0, 600.0, 6000.0, 4000.0),
    THAILAND("Thailand", "฿", 4.2, 45.0, 150.0, 20.0, 600.0, 10000.0, 7000.0),
    SINGAPORE("Singapore", "S$", 0.35, 0.90, 3.0, 0.50, 14.0, 500.0, 300.0),
    MALAYSIA("Malaysia", "RM", 0.50, 1.50, 5.0, 0.75, 20.0, 500.0, 350.0),
    JAPAN("Japan", "¥", 31.0, 80.0, 300.0, 40.0, 1200.0, 20000.0, 12000.0),
    SOUTH_KOREA("South Korea", "₩", 130.0, 750.0, 2800.0, 380.0, 11000.0, 30000.0, 18000.0),
    CHINA("China", "¥", 0.60, 2.5, 8.0, 1.2, 40.0, 800.0, 600.0),
    INDONESIA("Indonesia", "Rp", 1450.0, 13000.0, 40000.0, 6000.0, 180000.0, 450000.0, 280000.0),
    VIETNAM("Vietnam", "₫", 1800.0, 18000.0, 55000.0, 8000.0, 200000.0, 500000.0, 350000.0),
    UK("United Kingdom", "£", 0.35, 0.50, 1.8, 0.30, 9.0, 200.0, 150.0),
    GERMANY("Germany", "€", 0.40, 0.60, 2.0, 0.35, 10.0, 220.0, 170.0),
    CANADA("Canada", "C$", 0.15, 0.80, 2.8, 0.45, 14.0, 350.0, 220.0),
    MEXICO("Mexico", "MX$", 1.0, 15.0, 50.0, 6.0, 200.0, 5000.0, 3500.0),
    BRAZIL("Brazil", "R$", 0.80, 3.5, 12.0, 1.6, 55.0, 1200.0, 800.0);
    private final String displayName;
    private final String currencySymbol;
    private final double electricityRatePerKwh;
    private final double solarPanelPricePerWatt;
    private final double batteryPricePerAh;
    private final double inverterPricePerWatt;
    private final double controllerPricePerAmp;
    private final double wiringCostBase;
    private final double mountingCostBase;

    CountryConfig(String displayName, String currencySymbol, double electricityRate,
                  double solarPrice, double batteryPrice, double inverterPrice,
                  double controllerPrice, double wiringCost, double mountingCost) {
        this.displayName = displayName;
        this.currencySymbol = currencySymbol;
        this.electricityRatePerKwh = electricityRate;
        this.solarPanelPricePerWatt = solarPrice;
        this.batteryPricePerAh = batteryPrice;
        this.inverterPricePerWatt = inverterPrice;
        this.controllerPricePerAmp = controllerPrice;
        this.wiringCostBase = wiringCost;
        this.mountingCostBase = mountingCost;
    }

    // Getters
    public String getDisplayName() { return displayName; }
    public String getCurrencySymbol() { return currencySymbol; }
    public double getElectricityRatePerKwh() { return electricityRatePerKwh; }
    public double getSolarPanelPricePerWatt() { return solarPanelPricePerWatt; }
    public double getBatteryPricePerAh() { return batteryPricePerAh; }
    public double getInverterPricePerWatt() { return inverterPricePerWatt; }
    public double getControllerPricePerAmp() { return controllerPricePerAmp; }
    public double getWiringCostBase() { return wiringCostBase; }
    public double getMountingCost() { return mountingCostBase; }
}
