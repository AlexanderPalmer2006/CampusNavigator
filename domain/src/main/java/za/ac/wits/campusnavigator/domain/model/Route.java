package za.ac.wits.campusnavigator.domain.model;

import java.util.List;

/**
 * A computed walking route: an ordered list of waypoints from the current position to a
 * destination Building. Reuses {@link Position} as the waypoint carrier type (it already
 * represents "a lat/lng in the real world") -- {@code accuracyMeters} isn't meaningful
 * for a route waypoint and is always 0 here, not a GPS reading. Story 2.2.
 *
 * <p>{@code accessible} (Story 3.1) records whether this route was computed with the
 * Accessibility Preference honored -- intrinsic route metadata, not a separately-tracked
 * UI flag, so the "Accessible route" label (AC 2) can never drift out of sync with what
 * was actually computed. True whenever the preference was on at compute time, regardless
 * of whether the resulting path happened to need a detour around any stairs.</p>
 */
public final class Route {

    private final List<Position> waypoints;
    private final boolean accessible;

    public Route(List<Position> waypoints, boolean accessible) {
        this.waypoints = waypoints;
        this.accessible = accessible;
    }

    public List<Position> getWaypoints() {
        return waypoints;
    }

    public boolean isAccessible() {
        return accessible;
    }
}
