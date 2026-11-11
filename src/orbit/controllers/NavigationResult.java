package orbit.controllers;

import orbit.models.Planet;

/**
 * NavigationResult represents the outcome of an interplanetary navigation attempt,
 * providing the status, user-facing narrative message, target destination, and fuel consumed.
 */
public class NavigationResult {

    private final NavigationStatus status;
    private final String message;
    private final Planet destination;
    private final int fuelConsumed;

    public NavigationResult(NavigationStatus status, String message, Planet destination, int fuelConsumed) {
        this.status = (status != null) ? status : NavigationStatus.INVALID_DESTINATION;
        this.message = (message != null) ? message : "";
        this.destination = destination;
        this.fuelConsumed = Math.max(0, fuelConsumed);
    }

    public NavigationStatus getStatus() {
        return status;
    }

    public NavigationStatus status() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String message() {
        return message;
    }

    public Planet getDestination() {
        return destination;
    }

    public Planet destination() {
        return destination;
    }

    public int getFuelConsumed() {
        return fuelConsumed;
    }

    public int fuelConsumed() {
        return fuelConsumed;
    }

    public boolean isSuccess() {
        return status == NavigationStatus.SUCCESS;
    }

    public static NavigationResult success(Planet destination, int fuelConsumed, String message) {
        return new NavigationResult(NavigationStatus.SUCCESS, message, destination, fuelConsumed);
    }

    public static NavigationResult invalidDestination(String message) {
        return new NavigationResult(NavigationStatus.INVALID_DESTINATION, message, null, 0);
    }

    public static NavigationResult destinationLocked(Planet destination, String message) {
        return new NavigationResult(NavigationStatus.DESTINATION_LOCKED, message, destination, 0);
    }

    public static NavigationResult insufficientFuel(Planet destination, int fuelRequired, int fuelAvailable, String message) {
        return new NavigationResult(NavigationStatus.INSUFFICIENT_FUEL, message, destination, 0);
    }

    public static NavigationResult alreadyAtDestination(Planet destination, String message) {
        return new NavigationResult(NavigationStatus.ALREADY_AT_DESTINATION, message, destination, 0);
    }

    @Override
    public String toString() {
        return "NavigationResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", destination=" + (destination != null ? destination.getName() : "null") +
                ", fuelConsumed=" + fuelConsumed +
                '}';
    }
}

