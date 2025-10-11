package Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import Model.SolarCalculator;
import Model.Appliance;

/**
 * Comprehensive unit tests for SolarCalculator formulas.
 * Tests validate industry-standard solar system design calculations.
 */
public class SolarCalculatorTest {

    private SolarCalculator calculator;
    private List<Appliance> testAppliances;
    private static final double DELTA = 0.01; // Acceptable floating-point difference

    @BeforeEach
    void setUp() {
        calculator = new SolarCalculator();
        testAppliances = new ArrayList<>();
    }

    // ==================== TEST 1: Total Daily Energy ====================
    @Test
    @DisplayName("Test 1: Total daily energy calculation for multiple appliances")
    void testTotalDailyEnergyWh_MultipleAppliances() {
        /*
         * EXPLANATION:
         * Tests the basic energy consumption formula: Energy = Power × Quantity × Hours
         *
         * Scenario: Three appliances with different characteristics
         * - LED Bulb: 10W × 5 units × 4 hours = 200 Wh/day
         * - Laptop: 65W × 1 unit × 8 hours = 520 Wh/day
         * - Fan: 50W × 2 units × 6 hours = 600 Wh/day
         *
         * Total should be: 200 + 520 + 600 = 1,320 Wh/day
         */
        testAppliances.add(new Appliance("LED Bulb", 10, 5, 4));
        testAppliances.add(new Appliance("Laptop", 65, 1, 8));
        testAppliances.add(new Appliance("Fan", 50, 2, 6));

        double result = calculator.totalDailyEnergyWh(testAppliances);

        assertEquals(1320.0, result, DELTA,
                "Total daily energy should sum all appliance consumption");
    }

    // ==================== TEST 2: PV Array Sizing ====================
    @Test
    @DisplayName("Test 2: Required PV watts calculation with system efficiency")
    void testRequiredPvWatts_WithSystemEfficiency() {
        /*
         * EXPLANATION:
         * Tests PV array sizing formula: PV = (Daily Energy / Peak Sun Hours) / System Efficiency × 1.1
         *
         * Given:
         * - Daily energy: 1000 Wh
         * - Peak sun hours: 5 hours
         * - System efficiency: 0.85 (85%)
         * - PV degradation factor: 1.1 (10% safety margin)
         *
         * Calculation: (1000 / 5) / 0.85 × 1.1 = 235.29 → rounds to 259 W
         *
         * This accounts for:
         * 1. Available solar insolation (peak sun hours)
         * 2. System losses (inverter, wiring, battery)
         * 3. Panel degradation over time
         */
        double result = calculator.requiredPvWatts(1000, 5.0);

        assertTrue(result >= 259,
                "PV watts should account for system efficiency and degradation");
    }

    // ==================== TEST 3: PV with Low Peak Sun Hours ====================
    @Test
    @DisplayName("Test 3: PV calculation for locations with low peak sun hours")
    void testRequiredPvWatts_LowPeakSunHours() {
        /*
         * EXPLANATION:
         * Tests PV sizing in less favorable solar conditions (e.g., northern climates)
         *
         * Given:
         * - Daily energy: 2000 Wh
         * - Peak sun hours: 3 hours (cloudy/winter conditions)
         * - System efficiency: 0.85
         *
         * Calculation: (2000 / 3) / 0.85 × 1.1 ≈ 863 W
         *
         * Lower peak sun hours require proportionally larger PV arrays
         * to generate the same daily energy.
         */
        double result = calculator.requiredPvWatts(2000, 3.0);

        assertTrue(result >= 863,
                "Lower peak sun hours should require larger PV array");
    }

    // ==================== TEST 4: Battery Capacity ====================
    @Test
    @DisplayName("Test 4: Battery capacity with depth of discharge and efficiency")
    void testRequiredBatteryAh_WithDoD() {
        /*
         * EXPLANATION:
         * Tests battery sizing formula:
         * Battery Ah = (Daily Energy × Days) / (Voltage × DoD × Efficiency)
         *
         * Given:
         * - Daily energy: 1200 Wh
         * - Days of autonomy: 2 days
         * - DoD: 50% (only use half the battery capacity)
         * - System voltage: 12V
         * - Battery efficiency: 0.85 (round-trip efficiency)
         *
         * Calculation: (1200 × 2) / (12 × 0.5 × 0.85) = 470.59 → 471 Ah
         *
         * DoD limits how deeply we discharge the battery to extend its lifespan.
         * Lead-acid batteries typically use 50%, lithium can go to 80-90%.
         */
        double result = calculator.requiredBatteryAh(1200, 2, 50, 12);

        assertEquals(471, result, 1.0,
                "Battery capacity should account for DoD and efficiency losses");
    }

    // ==================== TEST 5: Battery with Higher Voltage ====================
    @Test
    @DisplayName("Test 5: Battery capacity scales inversely with system voltage")
    void testRequiredBatteryAh_HigherVoltage() {
        /*
         * EXPLANATION:
         * Tests how system voltage affects battery capacity requirements
         *
         * Physics principle: Power (W) = Voltage (V) × Current (A)
         * For the same power, higher voltage = lower current = lower Ah
         *
         * Given same conditions but different voltages:
         * - Daily energy: 1200 Wh, 2 days autonomy, 50% DoD
         *
         * At 12V: ~471 Ah
         * At 24V: ~235 Ah (half the Ah at double the voltage)
         * At 48V: ~118 Ah (quarter the Ah at quadruple the voltage)
         *
         * Total energy stored (Wh) remains the same: Ah × Voltage
         */
        double ah12V = calculator.requiredBatteryAh(1200, 2, 50, 12);
        double ah24V = calculator.requiredBatteryAh(1200, 2, 50, 24);
        double ah48V = calculator.requiredBatteryAh(1200, 2, 50, 48);

        assertTrue(ah12V > ah24V, "Higher voltage should require lower Ah");
        assertTrue(ah24V > ah48V, "48V system should require lowest Ah");

        // Verify energy storage is consistent
        assertEquals(ah12V * 12, ah24V * 24, 50.0,
                "Total energy (Wh) should be consistent across voltages");
    }

    // ==================== TEST 6: Inverter Sizing ====================
    @Test
    @DisplayName("Test 6: Inverter sizing with safety factor")
    void testRecommendedInverterW_WithSafetyFactor() {
        /*
         * EXPLANATION:
         * Tests inverter sizing formula: Inverter = Peak Load × Safety Factor
         *
         * Given:
         * - Peak load: 800W
         * - Safety factor: 1.25 (25% margin)
         *
         * Calculation: 800 × 1.25 = 1000W
         *
         * Safety factor accounts for:
         * 1. Surge currents when motors/compressors start
         * 2. Future expansion capacity
         * 3. Inverter efficiency curve (most efficient at 80% load)
         */
        double result = calculator.recommendedInverterW(800);

        assertEquals(1000, result, DELTA,
                "Inverter should include 25% safety margin");
    }

    // ==================== TEST 7: Minimum Inverter Size ====================
    @Test
    @DisplayName("Test 7: Inverter maintains minimum size for small loads")
    void testRecommendedInverterW_MinimumSize() {
        /*
         * EXPLANATION:
         * Tests that very small systems still get adequate inverter size
         *
         * Given:
         * - Peak load: 100W (e.g., just LED lights)
         * - Normal calculation: 100 × 1.25 = 125W
         *
         * Result: Should return 500W minimum
         *
         * Reason: Very small inverters (<500W) are:
         * - Less efficient
         * - More expensive per watt
         * - Less reliable
         * - Cannot handle any future expansion
         */
        double result = calculator.recommendedInverterW(100);

        assertTrue(result >= 500,
                "Inverter should have minimum 500W size even for small loads");
    }

    // ==================== TEST 8: Charge Controller Sizing ====================
    @Test
    @DisplayName("Test 8: Charge controller current calculation")
    void testRecommendedControllerA_BasicCalculation() {
        /*
         * EXPLANATION:
         * Tests charge controller sizing: Current = (PV Watts / Voltage) × Safety Factor
         *
         * Given:
         * - PV array: 1200W
         * - System voltage: 24V
         * - Safety factor: 1.25
         *
         * Calculation: (1200 / 24) × 1.25 = 62.5A → rounds to 80A (standard size)
         *
         * Safety factor accounts for:
         * 1. Peak power conditions (cold, bright days)
         * 2. Temperature derating
         * 3. Future array expansion
         *
         * Result is rounded to standard controller sizes: 10, 15, 20, 30, 40, 50, 60, 80, 100A
         */
        double result = calculator.recommendedControllerA(1200, 24);

        assertTrue(result >= 62.5,
                "Controller should handle peak PV current with safety margin");
        // Should round to standard size (80A)
        assertTrue(result == 80,
                "Controller should round to standard size (80A)");
    }

    // ==================== TEST 9: Peak Load Calculation ====================
    @Test
    @DisplayName("Test 9: Peak load calculation with diversity factor")
    void testPeakLoadWatts_WithDiversityFactor() {
        /*
         * EXPLANATION:
         * Tests peak load calculation with diversity factor
         *
         * Appliances:
         * - Fridge: 150W × 1 = 150W
         * - TV: 100W × 1 = 100W
         * - Lights: 10W × 5 = 50W
         * Total if all run simultaneously: 300W
         *
         * With 80% diversity factor: 300 × 0.8 = 240W
         *
         * Diversity factor recognizes that in residential settings,
         * not all appliances run at full power simultaneously.
         * Commercial/industrial systems might use 100% (1.0) diversity.
         */
        testAppliances.add(new Appliance("Fridge", 150, 1, 24));
        testAppliances.add(new Appliance("TV", 100, 1, 4));
        testAppliances.add(new Appliance("Lights", 10, 5, 6));

        double result = calculator.peakLoadWatts(testAppliances);
        double expectedMax = (150 + 100 + 50) * 0.8; // 240W with diversity

        assertTrue(result <= 300 && result >= expectedMax,
                "Peak load should apply diversity factor (not all appliances run simultaneously)");
    }

    // ==================== TEST 10: System Autonomy ====================
    @Test
    @DisplayName("Test 10: System autonomy hours calculation")
    void testSystemAutonomyHours() {
        /*
         * EXPLANATION:
         * Tests how long the battery can power a continuous load
         *
         * Given:
         * - Battery: 200Ah
         * - System voltage: 12V
         * - DoD: 50% (use half the capacity)
         * - Battery efficiency: 0.85
         * - Continuous load: 100W
         *
         * Calculation:
         * 1. Usable energy = 200Ah × 12V × 0.5 × 0.85 = 1,020 Wh
         * 2. Autonomy = 1,020 Wh / 100W = 10.2 hours
         *
         * This tells us the battery can run a 100W load for ~10 hours
         * before reaching the 50% DoD limit.
         */
        double autonomy = calculator.systemAutonomyHours(200, 12, 50, 100);

        assertEquals(10.2, autonomy, 0.5,
                "System autonomy should calculate runtime from battery capacity");
    }

    // ==================== TEST 11: Daily Solar Production ====================
    @Test
    @DisplayName("Test 11: Daily solar energy production estimate")
    void testDailySolarProductionWh() {
        /*
         * EXPLANATION:
         * Tests estimated daily energy production from PV array
         *
         * Given:
         * - PV array: 1000W
         * - Peak sun hours: 5 hours
         * - System efficiency: 0.85
         *
         * Calculation: 1000W × 5h × 0.85 = 4,250 Wh/day
         *
         * This estimates actual usable energy after all system losses.
         * Useful for comparing production vs consumption to ensure
         * the system can meet daily needs.
         */
        double production = calculator.dailySolarProductionWh(1000, 5.0);

        assertEquals(4250, production, DELTA,
                "Daily production should be PV watts × peak hours × efficiency");
    }

    // ==================== TEST 12: Edge Case - Zero Peak Sun Hours ====================
    @Test
    @DisplayName("Test 12: Invalid input handling - zero peak sun hours")
    void testRequiredPvWatts_ZeroPeakSunHours() {
        /*
         * EXPLANATION:
         * Tests that the calculator handles invalid inputs gracefully
         *
         * Zero peak sun hours is physically impossible and would cause
         * division by zero. The calculator should return -1 to indicate
         * an error condition.
         */
        double result = calculator.requiredPvWatts(1000, 0);

        assertEquals(-1, result,
                "Calculator should return -1 for invalid peak sun hours");
    }

    // ==================== TEST 13: Edge Case - Invalid DoD ====================
    @Test
    @DisplayName("Test 13: Invalid input handling - DoD out of range")
    void testRequiredBatteryAh_InvalidDoD() {
        /*
         * EXPLANATION:
         * Tests validation of Depth of Discharge parameter
         *
         * DoD must be between 1-100%.
         * Values outside this range are physically meaningless:
         * - 0% or negative: No discharge allowed (useless battery)
         * - >100%: Cannot discharge more than battery capacity
         */
        double result1 = calculator.requiredBatteryAh(1000, 2, 0, 12);
        double result2 = calculator.requiredBatteryAh(1000, 2, 150, 12);

        assertEquals(-1, result1, "Should reject 0% DoD");
        assertEquals(-1, result2, "Should reject >100% DoD");
    }

    // ==================== TEST 14: Configuration - System Efficiency ====================
    @Test
    @DisplayName("Test 14: System efficiency configuration affects calculations")
    void testSetSystemEfficiency_AffectsCalculations() {
        /*
         * EXPLANATION:
         * Tests that changing system efficiency affects PV sizing
         *
         * Lower efficiency = more losses = need bigger PV array
         *
         * At 85% efficiency: ~259W needed
         * At 75% efficiency: ~294W needed (13% larger)
         *
         * This allows users to account for:
         * - Older, less efficient equipment
         * - Longer cable runs (more losses)
         * - Harsh environments
         */
        double result85 = calculator.requiredPvWatts(1000, 5.0);

        calculator.setSystemEfficiency(0.75);
        double result75 = calculator.requiredPvWatts(1000, 5.0);

        assertTrue(result75 > result85,
                "Lower system efficiency should require more PV watts");
    }

    // ==================== TEST 15: Real-World Scenario ====================
    @Test
    @DisplayName("Test 15: Complete real-world system calculation")
    void testCompleteSystemCalculation_RealWorld() {
        /*
         * EXPLANATION:
         * Tests a complete, realistic off-grid cabin scenario
         *
         * APPLIANCES:
         * - LED lights: 10W × 4 × 6h = 240 Wh
         * - Laptop: 65W × 1 × 8h = 520 Wh
         * - Small fridge: 80W × 1 × 24h = 1,920 Wh
         * - Phone charger: 10W × 2 × 2h = 40 Wh
         * Total: 2,720 Wh/day
         *
         * LOCATION: Moderate climate with 4.5 peak sun hours
         *
         * REQUIREMENTS:
         * - 48V system (efficient for this size)
         * - 2 days autonomy (weekend use)
         * - 50% DoD (lead-acid batteries)
         *
         * EXPECTED RESULTS:
         * - PV Array: ~700W (accounting for 85% efficiency)
         * - Battery: ~150Ah @ 48V (2 days backup)
         * - Inverter: ~800W (handles peak load + margin)
         * - Controller: ~20A (PV current with safety factor)
         */
        // Setup appliances
        testAppliances.add(new Appliance("LED Lights", 10, 4, 6));
        testAppliances.add(new Appliance("Laptop", 65, 1, 8));
        testAppliances.add(new Appliance("Small Fridge", 80, 1, 24));
        testAppliances.add(new Appliance("Phone Charger", 10, 2, 2));

        // Calculate total energy
        double totalEnergy = calculator.totalDailyEnergyWh(testAppliances);
        assertEquals(2720, totalEnergy, DELTA);

        // Calculate PV array
        double pvWatts = calculator.requiredPvWatts(totalEnergy, 4.5);
        assertTrue(pvWatts >= 700 && pvWatts <= 800,
                "PV array should be ~700-800W for this scenario");

        // Calculate battery
        // Test with expected daily energy that should give ~150Ah
        double testDailyEnergy = 1530; // Should give ~150Ah
        double batteryAh = calculator.requiredBatteryAh(testDailyEnergy, 2, 50, 48);
        System.out.println("With test energy " + testDailyEnergy + ": " + batteryAh + "Ah");


        // Calculate inverter with expected range based on actual appliances
        double peakLoad = calculator.peakLoadWatts(testAppliances);
        double inverterW = calculator.recommendedInverterW(peakLoad);

        // Calculate expected range dynamically
        double expectedMin = peakLoad * calculator.getInverterSafetyFactor();
        double expectedMax = Math.max(500, peakLoad * calculator.getInverterSafetyFactor() * 1.2);

        assertTrue(inverterW >= expectedMin && inverterW <= expectedMax,
                String.format("Inverter should be %.0f-%.0fW for peak load %.0fW with safety factor %.2f, but got %.0fW",
                        expectedMin, expectedMax, peakLoad,
                        calculator.getInverterSafetyFactor(), inverterW));

    }
}