package orbit.controllers;

/**
 * Encapsulates the structured outcome of an inventory supply consumption attempt.
 * Conveys status, target spacecraft resource, restoration amounts, and user feedback.
 */
public class ItemUseResult {

    private final boolean success;
    private final String itemName;
    private final String targetResource;
    private final int restoredAmount;
    private final int previousValue;
    private final int newValue;
    private final int remainingQuantity;
    private final String message;

    private ItemUseResult(boolean success, String itemName, String targetResource,
                          int restoredAmount, int previousValue, int newValue,
                          int remainingQuantity, String message) {
        this.success = success;
        this.itemName = (itemName != null) ? itemName : "";
        this.targetResource = (targetResource != null) ? targetResource : "";
        this.restoredAmount = restoredAmount;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.remainingQuantity = remainingQuantity;
        this.message = (message != null) ? message : "";
    }

    public static ItemUseResult success(String itemName, String targetResource, int restoredAmount,
                                        int previousValue, int newValue, int remainingQuantity, String message) {
        return new ItemUseResult(true, itemName, targetResource, restoredAmount, previousValue, newValue, remainingQuantity, message);
    }

    public static ItemUseResult failure(String itemName, String message) {
        return new ItemUseResult(false, itemName, "", 0, 0, 0, 0, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getItemName() {
        return itemName;
    }

    public String getTargetResource() {
        return targetResource;
    }

    public int getRestoredAmount() {
        return restoredAmount;
    }

    public int getPreviousValue() {
        return previousValue;
    }

    public int getNewValue() {
        return newValue;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "[" + (success ? "SUCCESS" : "FAILURE") + "] " + itemName + ": " + message;
    }
}

