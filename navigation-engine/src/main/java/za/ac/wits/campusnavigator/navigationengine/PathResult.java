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

    private PathResult(List<RoutePoint> waypoints) {
        this.waypoints = waypoints;
    }

    public static PathResult found(List<RoutePoint> waypoints) {
        return new PathResult(waypoints);
    }

    public static PathResult notFound() {
        return new PathResult(null);
    }

    public boolean isFound() {
        return waypoints != null;
    }

    /** Empty (never null) when {@link #isFound()} is false. */
    public List<RoutePoint> getWaypoints() {
        return waypoints == null ? Collections.emptyList() : waypoints;
    }
}
