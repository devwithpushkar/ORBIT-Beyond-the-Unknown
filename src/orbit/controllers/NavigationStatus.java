package orbit.controllers;

/**
 * NavigationStatus defines the outcome categories for interplanetary travel operations.
 */
public enum NavigationStatus {
    SUCCESS,
    INVALID_DESTINATION,
    DESTINATION_LOCKED,
    INSUFFICIENT_FUEL,
    ALREADY_AT_DESTINATION
}

