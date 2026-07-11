package za.ac.wits.campusnavigator.domain.model;

/**
 * A routing-graph vertex. Pure domain model -- no Android or Room dependency, per
 * ARCHITECTURE-SPINE.md AD-5. Story 2.2.
 */
public final class Node {

    private final long id;
    private final double latitude;
    private final double longitude;
    private final String campusId;
    private final String levelId;

    public Node(long id, double latitude, double longitude, String campusId, String levelId) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.campusId = campusId;
        this.levelId = levelId;
    }

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCampusId() {
        return campusId;
    }

    /** {@code "OUTDOOR"} sentinel for every V1 row -- never a raw null. */
    public String getLevelId() {
        return levelId;
    }
}
