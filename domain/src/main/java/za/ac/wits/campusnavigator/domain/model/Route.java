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
 *
 * <p>{@code distanceMeters} (Story 4.2, AD-7) is the real walked path length: the two
 * start/destination "snap" legs (great-circle, since they aren't graph edges) plus the sum
 * of the actual {@code Edge.distanceMeters} of every edge traversed between them --
 * computed in {@code :navigation-engine}'s {@code AStarRouter} as the same edge-weighted
 * cost A* itself minimized to find the path (its {@code gScore} at the destination node),
 * not a second, independently-recomputed approximation of it. This is deliberately not a
 * straight-line current-position-to-destination shortcut, and deliberately not a
 * Haversine-of-{@code waypoints} resummation either -- the latter would silently diverge
 * from what A* actually optimized on for any real-world edge that isn't a straight line
 * between its two node coordinates (e.g. a corridor that bends). It's what lets
 * {@code FindNearestCategoryPickUseCase} compare candidate Buildings by real walking
 * distance rather than as-the-crow-flies proximity.</p>
 */
public final class Route {

    private final List<Position> waypoints;
    private final boolean accessible;
    private final double distanceMeters;

    public Route(List<Position> waypoints, boolean accessible, double distanceMeters) {
        this.waypoints = waypoints;
        this.accessible = accessible;
        this.distanceMeters = distanceMeters;
    }

    public List<Position> getWaypoints() {
        return waypoints;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }
}
