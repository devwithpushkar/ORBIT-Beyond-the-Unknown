package orbit.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MissionObjective represents a structured campaign milestone within ORBIT.
 * Tracks progression across the three major campaign stages:
 * - Stage 1 — Departure (Earth & Mars operations)
 * - Stage 2 — Deep Space Exploration (Europa & Titan expeditions and transmission evidence)
 * - Stage 3 — Beyond the Unknown (Kepler-186f transit and signal resolution)
 */
public class MissionObjective {

    public static final String STAGE_1 = "Stage 1 — Departure";
    public static final String STAGE_2 = "Stage 2 — Deep Space Exploration";
    public static final String STAGE_3 = "Stage 3 — Beyond the Unknown";

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }

    private final String id;
    private final String title;
    private final String description;
    private final String stage;
    private Status status;
    private String progressInfo;

    public MissionObjective(String id, String title, String description, String stage, Status status, String progressInfo) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Objective ID cannot be null or empty.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Objective title cannot be null or empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Objective description cannot be null or empty.");
        }
        if (stage == null || stage.trim().isEmpty()) {
            throw new IllegalArgumentException("Objective stage cannot be null or empty.");
        }

        this.id = id.trim();
        this.title = title.trim();
        this.description = description.trim();
        this.stage = stage.trim();
        this.status = (status != null) ? status : Status.PENDING;
        this.progressInfo = (progressInfo != null) ? progressInfo.trim() : "";
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStage() {
        return stage;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status != null) {
            this.status = status;
        }
    }

    public String getProgressInfo() {
        return progressInfo;
    }

    public void setProgressInfo(String progressInfo) {
        this.progressInfo = (progressInfo != null) ? progressInfo.trim() : "";
    }

    public boolean isCompleted() {
        return this.status == Status.COMPLETED;
    }

    public boolean isInProgress() {
        return this.status == Status.IN_PROGRESS;
    }

    public boolean isPending() {
        return this.status == Status.PENDING;
    }

    public void complete() {
        this.status = Status.COMPLETED;
        this.progressInfo = "Complete";
    }

    public void start() {
        if (this.status == Status.PENDING) {
            this.status = Status.IN_PROGRESS;
        }
    }

    /**
     * Initializes the standard 12 campaign objectives across all three campaign stages
     * according to the specifications in project.md.
     */
    public static List<MissionObjective> createCampaignObjectives() {
        List<MissionObjective> list = new ArrayList<>();

        // --- STAGE 1: DEPARTURE ---
        list.add(new MissionObjective(
                "obj_launch_earth",
                "Launch from Earth",
                "Disengage orbital docking clamps and begin deep-space operations away from Earth orbit.",
                STAGE_1,
                Status.IN_PROGRESS,
                "0/1 Departures"
        ));

        list.add(new MissionObjective(
                "obj_travel_mars",
                "Travel to Mars",
                "Plot an interplanetary burn vector and enter stable parking orbit around Mars.",
                STAGE_1,
                Status.IN_PROGRESS,
                "0/1 Arrivals"
        ));

        list.add(new MissionObjective(
                "obj_explore_surface",
                "Complete Initial Exploration",
                "Deploy a surface reconnaissance excursion on Earth or Mars.",
                STAGE_1,
                Status.IN_PROGRESS,
                "0/1 Surveys"
        ));

        list.add(new MissionObjective(
                "obj_collect_resources",
                "Collect Basic Resources",
                "Secure essential materials (Metal, Energy, Oxygen, or Fuel) from surface expeditions.",
                STAGE_1,
                Status.IN_PROGRESS,
                "0/1 Harvests"
        ));

        // --- STAGE 2: DEEP SPACE EXPLORATION ---
        list.add(new MissionObjective(
                "obj_explore_europa",
                "Explore Europa",
                "Navigate to the frozen moon Europa and survey its subterranean cryogenic ocean.",
                STAGE_2,
                Status.PENDING,
                "Locked"
        ));

        list.add(new MissionObjective(
                "obj_visit_titan",
                "Visit Titan",
                "Enter orbit around Titan and investigate its cryogenic methane atmosphere and seas.",
                STAGE_2,
                Status.PENDING,
                "Locked"
        ));

        list.add(new MissionObjective(
                "obj_collect_materials",
                "Collect Required Materials",
                "Gather rare outer-solar materials and telemetry to sustain deep-space operations.",
                STAGE_2,
                Status.PENDING,
                "Pending"
        ));

        list.add(new MissionObjective(
                "obj_discover_transmission",
                "Discover Unidentified Transmission",
                "Detect and record anomalous radio frequency evidence of the unknown signal.",
                STAGE_2,
                Status.PENDING,
                "Pending"
        ));

        // --- STAGE 3: BEYOND THE UNKNOWN ---
        list.add(new MissionObjective(
                "obj_travel_kepler",
                "Travel to Kepler-186f",
                "Execute high-impulse transit to the distant terrestrial exoplanet Kepler-186f.",
                STAGE_3,
                Status.PENDING,
                "Locked"
        ));

        list.add(new MissionObjective(
                "obj_investigate_signal",
                "Investigate the Signal",
                "Triangulate sensor telemetry to locate the origin of the unknown beacon.",
                STAGE_3,
                Status.PENDING,
                "Pending"
        ));

        list.add(new MissionObjective(
                "obj_discover_source",
                "Discover Signal Source",
                "Approach and scan the physical source of the anomalous deep-space transmission.",
                STAGE_3,
                Status.PENDING,
                "Pending"
        ));

        list.add(new MissionObjective(
                "obj_final_decision",
                "Make Final Mission Decision",
                "Determine the mission resolution: Return Home, Remain Behind, or Venture Beyond.",
                STAGE_3,
                Status.PENDING,
                "Pending"
        ));

        return list;
    }

    @Override
    public String toString() {
        return "MissionObjective{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", stage='" + stage + '\'' +
                ", status=" + status +
                '}';
    }
}
