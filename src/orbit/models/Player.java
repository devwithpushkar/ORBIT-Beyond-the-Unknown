package orbit.models;

/**
 * Player represents the spacecraft commander in ORBIT.
 * Encapsulates the commander's identity and assigned vessel name.
 */
public class Player {

    public static final String DEFAULT_SPACESHIP_NAME = "ODYSSEY-07";

    private String commanderName;
    private String spaceshipName;

    /**
     * Creates a new Player with the specified commander name and default spaceship name.
     *
     * @param commanderName The name of the commander
     * @throws IllegalArgumentException if commanderName is null or blank
     */
    public Player(String commanderName) {
        this(commanderName, DEFAULT_SPACESHIP_NAME);
    }

    /**
     * Creates a new Player with the specified commander name and spaceship name.
     *
     * @param commanderName The name of the commander
     * @param spaceshipName The name of the spacecraft
     * @throws IllegalArgumentException if commanderName is null or blank
     */
    public Player(String commanderName, String spaceshipName) {
        setCommanderName(commanderName);
        setSpaceshipName(spaceshipName);
    }

    public String getCommanderName() {
        return commanderName;
    }

    public void setCommanderName(String commanderName) {
        if (commanderName == null || commanderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Commander name cannot be null or empty.");
        }
        this.commanderName = commanderName.trim();
    }

    public String getSpaceshipName() {
        return spaceshipName;
    }

    public void setSpaceshipName(String spaceshipName) {
        if (spaceshipName == null || spaceshipName.trim().isEmpty()) {
            this.spaceshipName = DEFAULT_SPACESHIP_NAME;
        } else {
            this.spaceshipName = spaceshipName.trim();
        }
    }

    @Override
    public String toString() {
        return "Player{commander='" + commanderName + "', ship='" + spaceshipName + "'}";
    }
}
