package orbit.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Inventory manages the spacecraft's cargo hold and supply stores.
 * Safely tracks quantities of five distinct item categories:
 * - Metal (Hull repairs, restores 20 Hull)
 * - Energy Cells (Reactor power, restores 25 Energy)
 * - Oxygen Canisters (Life support, restores 25 Oxygen)
 * - Fuel Cells (Impulse propellant, restores 25 Fuel)
 * - Alien Artifacts (Special discoveries, cannot be consumed)
 */
public class Inventory {

    public static final String METAL = "Metal";
    public static final String ENERGY_CELLS = "Energy Cells";
    public static final String OXYGEN_CANISTERS = "Oxygen Canisters";
    public static final String FUEL_CELLS = "Fuel Cells";
    public static final String ALIEN_ARTIFACTS = "Alien Artifacts";

    public static final Set<String> VALID_ITEMS = Set.of(
            METAL,
            ENERGY_CELLS,
            OXYGEN_CANISTERS,
            FUEL_CELLS,
            ALIEN_ARTIFACTS
    );

    private final Map<String, Integer> items;

    /**
     * Initializes an empty inventory with 0 quantity for each valid item.
     */
    public Inventory() {
        this.items = new LinkedHashMap<>();
        items.put(METAL, 0);
        items.put(ENERGY_CELLS, 0);
        items.put(OXYGEN_CANISTERS, 0);
        items.put(FUEL_CELLS, 0);
        items.put(ALIEN_ARTIFACTS, 0);
    }

    /**
     * Factory method creating a default starting inventory:
     * - Metal: 2
     * - Energy Cells: 2
     * - Oxygen Canisters: 2
     * - Fuel Cells: 2
     * - Alien Artifacts: 0
     */
    public static Inventory createStartingInventory() {
        Inventory inv = new Inventory();
        inv.setQuantity(METAL, 2);
        inv.setQuantity(ENERGY_CELLS, 2);
        inv.setQuantity(OXYGEN_CANISTERS, 2);
        inv.setQuantity(FUEL_CELLS, 2);
        inv.setQuantity(ALIEN_ARTIFACTS, 0);
        return inv;
    }

    /**
     * Resolves an item name case-insensitively to its canonical string representation.
     */
    public static String resolveCanonicalName(String itemName) {
        if (itemName == null) return null;
        for (String valid : VALID_ITEMS) {
            if (valid.equalsIgnoreCase(itemName.trim())) {
                return valid;
            }
        }
        return null;
    }

    /**
     * Checks if a given item name corresponds to a valid recognized item type.
     */
    public static boolean isValidItem(String itemName) {
        return resolveCanonicalName(itemName) != null;
    }

    /**
     * Checks whether an item type is consumable to restore ship resources.
     */
    public static boolean isConsumable(String itemName) {
        String canonical = resolveCanonicalName(itemName);
        if (canonical == null) return false;
        return !canonical.equals(ALIEN_ARTIFACTS);
    }

    /**
     * Returns the restoration amount provided by one unit of the specified item.
     */
    public static int getRestorationAmount(String itemName) {
        String canonical = resolveCanonicalName(itemName);
        if (canonical == null) return 0;
        return switch (canonical) {
            case METAL -> 20;
            case ENERGY_CELLS -> 25;
            case OXYGEN_CANISTERS -> 25;
            case FUEL_CELLS -> 25;
            default -> 0;
        };
    }

    /**
     * Returns a brief description of the item's purpose.
     */
    public static String getItemDescription(String itemName) {
        String canonical = resolveCanonicalName(itemName);
        if (canonical == null) return "";
        return switch (canonical) {
            case METAL -> "Repairs spacecraft hull (+20 Hull)";
            case ENERGY_CELLS -> "Restores spacecraft energy (+25 Energy)";
            case OXYGEN_CANISTERS -> "Restores spacecraft oxygen (+25 Oxygen)";
            case FUEL_CELLS -> "Restores spacecraft fuel (+25 Fuel)";
            case ALIEN_ARTIFACTS -> "Special discoveries (Cannot be consumed)";
            default -> "";
        };
    }

    /**
     * Retrieves the current stored quantity of a specified item.
     */
    public int getQuantity(String itemName) {
        String canonical = resolveCanonicalName(itemName);
        if (canonical == null) {
            throw new IllegalArgumentException("Invalid item name: " + itemName);
        }
        return items.getOrDefault(canonical, 0);
    }

    /**
     * Sets the quantity of an item directly. Rejects negative quantities.
     */
    public void setQuantity(String itemName, int quantity) {
        String canonical = resolveCanonicalName(itemName);
        if (canonical == null) {
            throw new IllegalArgumentException("Invalid item name: " + itemName);
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        items.put(canonical, quantity);
    }

    /**
     * Adds a positive quantity of an item to the inventory.
     */
    public void add(String itemName, int amount) {
        String canonical = resolveCanonicalName(itemName);
        if (canonical == null) {
            throw new IllegalArgumentException("Invalid item name: " + itemName);
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add a negative amount: " + amount);
        }
        if (amount == 0) return;

        int current = items.getOrDefault(canonical, 0);
        items.put(canonical, current + amount);
    }

    /**
     * Removes a specified quantity of an item from the inventory.
     * Prevents negative quantities and prevents removing more items than are available.
     *
     * @return true if items were successfully removed; false if insufficient quantity
     */
    public boolean remove(String itemName, int amount) {
        String canonical = resolveCanonicalName(itemName);
        if (canonical == null) {
            throw new IllegalArgumentException("Invalid item name: " + itemName);
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot remove a negative amount: " + amount);
        }
        if (amount == 0) return true;

        int current = items.getOrDefault(canonical, 0);
        if (current < amount) {
            return false;
        }

        items.put(canonical, current - amount);
        return true;
    }

    /**
     * Checks if the inventory holds at least the specified quantity of an item.
     */
    public boolean hasItem(String itemName, int amount) {
        String canonical = resolveCanonicalName(itemName);
        if (canonical == null) return false;
        return items.getOrDefault(canonical, 0) >= amount;
    }

    /**
     * Returns an unmodifiable map of all item quantities.
     */
    public Map<String, Integer> getAllItems() {
        return Collections.unmodifiableMap(items);
    }

    /**
     * Returns the total sum of all cargo units stored.
     */
    public int getTotalItemCount() {
        int total = 0;
        for (int qty : items.values()) {
            total += qty;
        }
        return total;
    }

    @Override
    public String toString() {
        return "Inventory" + items;
    }
}
