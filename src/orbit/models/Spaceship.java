package orbit.models;

/**
 * Spaceship manages the spacecraft's core telemetry attributes, vitals,
 * and structural integrity.
 *
 * All resource values (Fuel, Oxygen, Energy, Hull Integrity) are strictly
 * bounded between 0 and 100 percent.
 */
public class Spaceship {

    public static final int MAX_RESOURCE = 100;
    public static final int MIN_RESOURCE = 0;
    public static final int CRITICAL_THRESHOLD = 25;
    public static final int CAUTION_THRESHOLD = 50;

    private int fuel;
    private int oxygen;
    private int energy;
    private int hullIntegrity;

    /**
     * Initializes a new Spaceship with all resources at maximum capacity (100%).
     */
    public Spaceship() {
        this.fuel = MAX_RESOURCE;
        this.oxygen = MAX_RESOURCE;
        this.energy = MAX_RESOURCE;
        this.hullIntegrity = MAX_RESOURCE;
    }

    /**
     * Initializes a Spaceship with custom initial resource values, clamped between 0 and 100.
     */
    public Spaceship(int fuel, int oxygen, int energy, int hullIntegrity) {
        this.fuel = clamp(fuel);
        this.oxygen = clamp(oxygen);
        this.energy = clamp(energy);
        this.hullIntegrity = clamp(hullIntegrity);
    }

    // --- Resource Consumption ---

    public void consumeFuel(int amount) {
        if (amount > 0) {
            this.fuel = clamp(this.fuel - amount);
        }
    }

    public void consumeOxygen(int amount) {
        if (amount > 0) {
            this.oxygen = clamp(this.oxygen - amount);
        }
    }

    public void consumeEnergy(int amount) {
        if (amount > 0) {
            this.energy = clamp(this.energy - amount);
        }
    }

    // --- Resource Restoration ---

    public void restoreFuel(int amount) {
        if (amount > 0) {
            this.fuel = clamp(this.fuel + amount);
        }
    }

    public void restoreOxygen(int amount) {
        if (amount > 0) {
            this.oxygen = clamp(this.oxygen + amount);
        }
    }

    public void restoreEnergy(int amount) {
        if (amount > 0) {
            this.energy = clamp(this.energy + amount);
        }
    }

    public void repairHull(int amount) {
        if (amount > 0) {
            this.hullIntegrity = clamp(this.hullIntegrity + amount);
        }
    }

    // --- Damage Handling ---

    public void applyDamage(int amount) {
        if (amount > 0) {
            this.hullIntegrity = clamp(this.hullIntegrity - amount);
        }
    }

    // --- Status and Safeguards ---

    /**
     * A spacecraft is operational if its hull integrity and life support are intact.
     */
    public boolean isOperational() {
        return this.hullIntegrity > 0 && this.oxygen > 0;
    }

    // Fuel state checks
    public boolean isFuelDepleted() {
        return this.fuel == 0;
    }

    public boolean isFuelCritical() {
        return this.fuel > 0 && this.fuel <= CRITICAL_THRESHOLD;
    }

    public boolean isFuelCaution() {
        return this.fuel > CRITICAL_THRESHOLD && this.fuel <= CAUTION_THRESHOLD;
    }

    public boolean isFuelNormal() {
        return this.fuel > CAUTION_THRESHOLD;
    }

    // Oxygen state checks
    public boolean isOxygenDepleted() {
        return this.oxygen == 0;
    }

    public boolean isOxygenCritical() {
        return this.oxygen > 0 && this.oxygen <= CRITICAL_THRESHOLD;
    }

    public boolean isOxygenCaution() {
        return this.oxygen > CRITICAL_THRESHOLD && this.oxygen <= CAUTION_THRESHOLD;
    }

    public boolean isOxygenNormal() {
        return this.oxygen > CAUTION_THRESHOLD;
    }

    // Energy state checks
    public boolean isEnergyDepleted() {
        return this.energy == 0;
    }

    public boolean isEnergyCritical() {
        return this.energy > 0 && this.energy <= CRITICAL_THRESHOLD;
    }

    public boolean isEnergyCaution() {
        return this.energy > CRITICAL_THRESHOLD && this.energy <= CAUTION_THRESHOLD;
    }

    public boolean isEnergyNormal() {
        return this.energy > CAUTION_THRESHOLD;
    }

    // Hull state checks
    public boolean isHullDepleted() {
        return this.hullIntegrity == 0;
    }

    public boolean isHullCritical() {
        return this.hullIntegrity > 0 && this.hullIntegrity <= CRITICAL_THRESHOLD;
    }

    public boolean isHullCaution() {
        return this.hullIntegrity > CRITICAL_THRESHOLD && this.hullIntegrity <= CAUTION_THRESHOLD;
    }

    public boolean isHullNormal() {
        return this.hullIntegrity > CAUTION_THRESHOLD;
    }

    // Compound checks
    public boolean isAnyResourceDepleted() {
        return isFuelDepleted() || isOxygenDepleted() || isEnergyDepleted() || isHullDepleted();
    }

    public boolean isAnyResourceCritical() {
        return isFuelCritical() || isOxygenCritical() || isEnergyCritical() || isHullCritical();
    }

    public boolean isAnyResourceCaution() {
        return isFuelCaution() || isOxygenCaution() || isEnergyCaution() || isHullCaution();
    }

    public static boolean isCriticallyLow(int value) {
        return value > 0 && value <= CRITICAL_THRESHOLD;
    }

    public static boolean isDepleted(int value) {
        return value == 0;
    }

    public static boolean isCaution(int value) {
        return value > CRITICAL_THRESHOLD && value <= CAUTION_THRESHOLD;
    }

    public static boolean isNormal(int value) {
        return value > CAUTION_THRESHOLD;
    }

    public static String getResourceStatus(int value) {
        if (value == 0) return "DEPLETED";
        if (value <= CRITICAL_THRESHOLD) return "CRITICAL";
        if (value <= CAUTION_THRESHOLD) return "CAUTION";
        return "NORMAL";
    }

    // --- Clamping Helper ---

    private int clamp(int value) {
        return Math.max(MIN_RESOURCE, Math.min(MAX_RESOURCE, value));
    }

    // --- Getters ---

    public int getFuel() {
        return fuel;
    }

    public int getOxygen() {
        return oxygen;
    }

    public int getEnergy() {
        return energy;
    }

    public int getHullIntegrity() {
        return hullIntegrity;
    }

    @Override
    public String toString() {
        return "Spaceship{" +
                "fuel=" + fuel + "%" +
                ", oxygen=" + oxygen + "%" +
                ", energy=" + energy + "%" +
                ", hull=" + hullIntegrity + "%" +
                ", operational=" + isOperational() +
                '}';
    }
}
