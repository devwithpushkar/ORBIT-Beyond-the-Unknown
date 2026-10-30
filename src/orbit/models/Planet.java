package orbit.models;

/**
 * Planet represents an interplanetary destination within the ORBIT galaxy.
 * Stores geographical, environmental, and exploration telemetry.
 */
public class Planet {

    private String name;
    private String description;
    private String environmentDescription;
    private boolean discovered;
    private boolean explored;

    /**
     * Initializes a Planet with full discovery and exploration states.
     *
     * @param name                   The planetary designation
     * @param description            Overview of the destination
     * @param environmentDescription Details regarding climate, surface, and hazards
     * @param discovered             Whether the planet has been revealed on the galaxy map
     * @param explored               Whether the player has landed/scanned the surface
     */
    public Planet(String name, String description, String environmentDescription, boolean discovered, boolean explored) {
        setName(name);
        setDescription(description);
        setEnvironmentDescription(environmentDescription);
        this.discovered = discovered;
        this.explored = explored;
    }

    /**
     * Initializes an undiscovered, unexplored Planet.
     */
    public Planet(String name, String description, String environmentDescription) {
        this(name, description, environmentDescription, false, false);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Planet name cannot be null or empty.");
        }
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : "";
    }

    public String getEnvironmentDescription() {
        return environmentDescription;
    }

    public void setEnvironmentDescription(String environmentDescription) {
        this.environmentDescription = (environmentDescription != null) ? environmentDescription.trim() : "";
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public boolean isExplored() {
        return explored;
    }

    public void setExplored(boolean explored) {
        this.explored = explored;
    }

    @Override
    public String toString() {
        return "Planet{" +
                "name='" + name + '\'' +
                ", discovered=" + discovered +
                ", explored=" + explored +
                '}';
    }
}
