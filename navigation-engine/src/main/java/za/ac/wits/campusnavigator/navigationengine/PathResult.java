package za.ac.wits.campusnavigator.navigationengine;

import java.util.Collections;
import java.util.List;

/**
 * :navigation-engine has no dependency on :domain's Result&lt;T&gt; (wrong dependency
 * direction per ARCHITECTURE-SPINE.md AD-5) -- this is a tiny local result wrapper for
 * "no path" instead of returning null. :domain's ComputeRouteUseCase translates this
 * into Result&lt;Route&gt; (Story 2.2 Task 1).
 */
public final class PathResult {

    private final List<RoutePoint> waypoints;
    private final double totalDistanceMeters;

    private PathResult(List<RoutePoint> waypoints, double totalDistanceMeters) {
        this.waypoints = waypoints;
        this.totalDistanceMeters = totalDistanceMeters;
    }

    /**
     * @param totalDistanceMeters the real path length: the two start/destination "snap"
     *                            legs (Haversine, since they aren't graph edges) plus the
     *                            actual {@code GraphEdge.distanceMeters} of every edge
     *                            traversed between them -- not a Haversine straight-line
     *                            shortcut between waypoint coordinates (Story 4.2, AD-7).
     *                            This is the same weighted cost A* itself minimized, not a
     *                            second, independently-computed approximation of it.
     */
    public static PathResult found(List<RoutePoint> waypoints, double totalDistanceMeters) {
        return new PathResult(waypoints, totalDistanceMeters);
    }

    public static PathResult notFound() {
        return new PathResult(null, 0.0);
    }

    public boolean isFound() {
        return waypoints != null;
    }

    /** Empty (never null) when {@link #isFound()} is false. */
    public List<RoutePoint> getWaypoints() {
        return waypoints == null ? Collections.emptyList() : waypoints;
    }

    /** Meaningless (0.0) when {@link #isFound()} is false. */
    public double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }
}
