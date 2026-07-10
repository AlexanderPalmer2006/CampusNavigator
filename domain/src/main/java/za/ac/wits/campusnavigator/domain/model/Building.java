package za.ac.wits.campusnavigator.domain.model;

/**
 * A single navigable structure on campus. Pure domain model — no Android or Room
 * dependency, per ARCHITECTURE-SPINE.md AD-5 (:domain carries zero Android deps).
 */
public final class Building {

    private final long id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final String campusId;

    public Building(long id, String name, double latitude, double longitude, String campusId) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.campusId = campusId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Defaults to a single V1 value covering the whole West+East main campus.
     * Absorb-growth seed for future multi-campus support (ARCHITECTURE-SPINE.md,
     * Structural Seed) — not a per-row choice made now.
     */
    public String getCampusId() {
        return campusId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Building)) return false;
        Building other = (Building) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
