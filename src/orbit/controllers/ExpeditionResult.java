package orbit.controllers;

import orbit.models.ExplorationEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Structured outcome of an expedition initiation or step, containing event details,
 * narrative feedback, choice prompts, and applied effect logs.
 */
public class ExpeditionResult {

    public enum Type {
        LAUNCH_INHIBITED,   // Casualty or insufficient energy/oxygen
        UNSUPPORTED_PLANET, // Celestial body locked or landing prohibited
        AUTO_RESOLVED,      // Discovery, Resource, or generic event applied immediately
        HAZARD_CHOICE,      // Hazard event requiring choice (mitigate vs brace)
        BEACON_CHOICE,      // Beacon event requiring tactical triangulation choice
        MONOLITH_CHOICE,    // Xenological monolith requiring tactical protocol choice
        KEPLER_CLIMAX       // Special Kepler-186f climax event (artifact secured)
    }

    private final boolean success;
    private final Type type;
    private final String message;
    private final ExplorationEvent event;
    private final List<String> effectLog;
    private final List<String> availableChoices;

    private ExpeditionResult(boolean success, Type type, String message, ExplorationEvent event,
                             List<String> effectLog, List<String> availableChoices) {
        this.success = success;
        this.type = (type != null) ? type : Type.AUTO_RESOLVED;
        this.message = (message != null) ? message : "";
        this.event = event;
        this.effectLog = (effectLog != null) ? new ArrayList<>(effectLog) : Collections.emptyList();
        this.availableChoices = (availableChoices != null) ? new ArrayList<>(availableChoices) : Collections.emptyList();
    }

    public static ExpeditionResult launchInhibited(String message) {
        return new ExpeditionResult(false, Type.LAUNCH_INHIBITED, message, null, null, null);
    }

    public static ExpeditionResult unsupportedPlanet(String message) {
        return new ExpeditionResult(false, Type.UNSUPPORTED_PLANET, message, null, null, null);
    }

    public static ExpeditionResult autoResolved(ExplorationEvent event, String message, List<String> effectLog) {
        return new ExpeditionResult(true, Type.AUTO_RESOLVED, message, event, effectLog, null);
    }

    public static ExpeditionResult hazardChoice(ExplorationEvent event, String message, List<String> choices) {
        return new ExpeditionResult(true, Type.HAZARD_CHOICE, message, event, null, choices);
    }

    public static ExpeditionResult beaconChoice(ExplorationEvent event, String message, List<String> choices) {
        return new ExpeditionResult(true, Type.BEACON_CHOICE, message, event, null, choices);
    }

    public static ExpeditionResult monolithChoice(ExplorationEvent event, String message, List<String> choices) {
        return new ExpeditionResult(true, Type.MONOLITH_CHOICE, message, event, null, choices);
    }

    public static ExpeditionResult keplerClimax(String message, List<String> effectLog) {
        return new ExpeditionResult(true, Type.KEPLER_CLIMAX, message, null, effectLog, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public ExplorationEvent getEvent() {
        return event;
    }

    public List<String> getEffectLog() {
        return Collections.unmodifiableList(effectLog);
    }

    public List<String> getAvailableChoices() {
        return Collections.unmodifiableList(availableChoices);
    }
}

