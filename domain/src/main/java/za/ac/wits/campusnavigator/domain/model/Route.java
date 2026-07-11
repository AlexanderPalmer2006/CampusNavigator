package za.ac.wits.campusnavigator.domain.model;

import java.util.List;

/**
 * A computed walking route: an ordered list of waypoints from the current position to a
 * destination Building. Reuses {@link Position} as the waypoint carrier type (it already
 * represents "a lat/lng in the real world") -- {@code accuracyMeters} isn't meaningful
 * for a route waypoint and is always 0 here, not a GPS reading. Story 2.2.
 */
public final class Route {

    private final List<Position> waypoints;

    public Route(List<Position> waypoints) {
        this.waypoints = waypoints;
    }

    public List<Position> getWaypoints() {
        return waypoints;
    }
}
