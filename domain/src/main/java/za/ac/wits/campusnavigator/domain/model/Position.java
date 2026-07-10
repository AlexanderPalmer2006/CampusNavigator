package za.ac.wits.campusnavigator.domain.model;

/**
 * A single GPS fix. Pure domain model -- no Android dependency, per
 * ARCHITECTURE-SPINE.md AD-5 (:domain carries zero Android deps).
 */
public final class Position {

    private final double latitude;
    private final double longitude;
    private final float accuracyMeters;

    public Position(double latitude, double longitude, float accuracyMeters) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyMeters = accuracyMeters;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Estimated horizontal error radius in meters (68% confidence), matching
     * Android's own {@code Location.getAccuracy()} contract.
     */
    public float getAccuracyMeters() {
        return accuracyMeters;
    }
}
