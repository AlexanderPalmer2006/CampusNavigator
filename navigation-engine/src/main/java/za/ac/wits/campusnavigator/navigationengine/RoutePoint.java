package za.ac.wits.campusnavigator.navigationengine;

/** One waypoint in a computed route -- a plain lat/lng, no Android type. */
public final class RoutePoint {

    public final double latitude;
    public final double longitude;

    public RoutePoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
