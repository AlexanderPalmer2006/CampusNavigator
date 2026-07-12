package za.ac.wits.campusnavigator.navigationengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class AStarRouterTest {

    private final AStarRouter router = new AStarRouter();

    @Test
    public void directlyConnectedNodes_findsRoute() {
        List<GraphNode> nodes = Arrays.asList(
                new GraphNode(1L, -26.1908, 28.0261),
                new GraphNode(2L, -26.1912, 28.0298));
        List<GraphEdge> edges = Collections.singletonList(new GraphEdge(1L, 2L, 350.0, false));

        PathResult result = router.findRoute(nodes, edges, -26.1908, 28.0261, -26.1912, 28.0298);

        assertTrue(result.isFound());
        // start point + node1 + node2 + dest point
        assertEquals(4, result.getWaypoints().size());
        // Start/dest coincide exactly with node1/node2 -- both snap legs are 0, so the
        // total is exactly the traversed edge's own distanceMeters (Story 4.2, AD-7):
        // proves getTotalDistanceMeters() reports the real edge-weighted cost A* itself
        // minimized, not a Haversine-of-waypoints recomputation that could silently
        // diverge from it for a real-world edge that isn't a straight line.
        assertEquals(350.0, result.getTotalDistanceMeters(), 0.01);
    }

    @Test
    public void multiHopPath_routesThroughIntermediateNode() {
        // 1 -- 2 -- 3, no direct 1-3 edge -- forces the router to actually pathfind.
        List<GraphNode> nodes = Arrays.asList(
                new GraphNode(1L, 0.0, 0.0),
                new GraphNode(2L, 0.0, 0.001),
                new GraphNode(3L, 0.0, 0.002));
        List<GraphEdge> edges = Arrays.asList(
                new GraphEdge(1L, 2L, 100.0, false),
                new GraphEdge(2L, 3L, 100.0, false));

        PathResult result = router.findRoute(nodes, edges, 0.0, 0.0, 0.0, 0.002);

        assertTrue(result.isFound());
        assertEquals(5, result.getWaypoints().size()); // start + n1 + n2 + n3 + dest
        // Sum of both traversed edges, not a recomputed Haversine of the (here,
        // deliberately collinear) waypoint coordinates -- see the test above.
        assertEquals(200.0, result.getTotalDistanceMeters(), 0.01);
    }

    @Test
    public void disconnectedGraph_returnsNotFound() {
        List<GraphNode> nodes = Arrays.asList(
                new GraphNode(1L, 0.0, 0.0),
                new GraphNode(2L, 1.0, 1.0)); // far away, no edge connecting them
        List<GraphEdge> edges = Collections.emptyList();

        PathResult result = router.findRoute(nodes, edges, 0.0, 0.0, 1.0, 1.0);

        assertFalse(result.isFound());
        assertTrue("Waypoints must be empty, never null, when not found", result.getWaypoints().isEmpty());
    }

    @Test
    public void emptyGraph_returnsNotFound() {
        PathResult result = router.findRoute(new ArrayList<>(), new ArrayList<>(), 0.0, 0.0, 1.0, 1.0);

        assertFalse(result.isFound());
    }

    @Test
    public void startAndDestSnapToSameNearestNode_stillReturnsARoute() {
        List<GraphNode> nodes = Collections.singletonList(new GraphNode(1L, 0.0, 0.0));
        List<GraphEdge> edges = Collections.emptyList();

        // Both start and destination snap to the single node -- trivial "already there" case.
        PathResult result = router.findRoute(nodes, edges, 0.0001, 0.0001, 0.0002, 0.0002);

        assertTrue(result.isFound());
        assertEquals(3, result.getWaypoints().size()); // start + node1 + dest
    }

    @Test
    public void edgeWeightsPreferShorterPath_overFewerHops() {
        // 1 -- 2 -- 4 (short, 2 hops) vs 1 -- 3 -- 4 (long, 2 hops) -- same hop count,
        // different weights, confirms weighted cost, not just hop count, drives the result.
        List<GraphNode> nodes = Arrays.asList(
                new GraphNode(1L, 0.0, 0.0),
                new GraphNode(2L, 0.0, 0.001),
                new GraphNode(3L, 0.0, -0.001),
                new GraphNode(4L, 0.0, 0.002));
        List<GraphEdge> edges = Arrays.asList(
                new GraphEdge(1L, 2L, 50.0, false),
                new GraphEdge(2L, 4L, 50.0, false),
                new GraphEdge(1L, 3L, 500.0, false),
                new GraphEdge(3L, 4L, 500.0, false));

        PathResult result = router.findRoute(nodes, edges, 0.0, 0.0, 0.0, 0.002);

        assertTrue(result.isFound());
        // start + n1 + n2 + n4 + dest -- the short path via node 2, not node 3.
        assertEquals(5, result.getWaypoints().size());
        assertEquals(0.001, result.getWaypoints().get(2).longitude, 0.0001);
    }

    @Test
    public void edgeReferencingUnknownNode_isIgnoredRatherThanCrashing() {
        // No Room @ForeignKey enforcement on Edge.from_node_id/to_node_id -- a dangling
        // reference (id 99 doesn't exist in `nodes`) must degrade to "that edge doesn't
        // exist" instead of NPEing during path reconstruction.
        List<GraphNode> nodes = Arrays.asList(
                new GraphNode(1L, 0.0, 0.0),
                new GraphNode(2L, 0.0, 0.001));
        List<GraphEdge> edges = Arrays.asList(
                new GraphEdge(1L, 99L, 50.0, false),
                new GraphEdge(1L, 2L, 100.0, false));

        PathResult result = router.findRoute(nodes, edges, 0.0, 0.0, 0.0, 0.001);

        assertTrue(result.isFound());
        assertEquals(4, result.getWaypoints().size()); // start + n1 + n2 + dest, via the valid edge
    }

    @Test
    public void avoidStairs_detoursAroundStairEdge_whenAlternatePathExists() {
        // 1 --(stairs, direct)-- 2, and 1 -- 3 -- 2 (longer, step-free) as the alternate.
        List<GraphNode> nodes = Arrays.asList(
                new GraphNode(1L, 0.0, 0.0),
                new GraphNode(2L, 0.0, 0.003),
                new GraphNode(3L, 0.0, 0.0015));
        List<GraphEdge> edges = Arrays.asList(
                new GraphEdge(1L, 2L, 50.0, true),
                new GraphEdge(1L, 3L, 100.0, false),
                new GraphEdge(3L, 2L, 100.0, false));

        // avoidStairs=false: takes the short direct (stair) edge.
        PathResult direct = router.findRoute(nodes, edges, 0.0, 0.0, 0.0, 0.003, false);
        assertTrue(direct.isFound());
        assertEquals(4, direct.getWaypoints().size()); // start + n1 + n2 + dest

        // avoidStairs=true: the direct edge is impassable, must detour via node 3.
        PathResult accessible = router.findRoute(nodes, edges, 0.0, 0.0, 0.0, 0.003, true);
        assertTrue(accessible.isFound());
        assertEquals(5, accessible.getWaypoints().size()); // start + n1 + n3 + n2 + dest
        // The detour's real distance (100+100=200) is longer than the direct edge's own
        // distanceMeters (50) -- confirms getTotalDistanceMeters() reflects the actual
        // detoured path taken, not the straight-line distance between start and dest
        // (which a naive Haversine(start, dest) shortcut would wrongly report as ~333m
        // for neither path -- this asserts the real per-edge-summed value instead).
        assertEquals(200.0, accessible.getTotalDistanceMeters(), 0.01);
    }

    @Test
    public void avoidStairs_returnsNotFound_whenOnlyPathRequiresStairs() {
        // 1 -- 2 is the only edge, and it's a stair edge -- no step-free alternative exists.
        List<GraphNode> nodes = Arrays.asList(
                new GraphNode(1L, 0.0, 0.0),
                new GraphNode(2L, 0.0, 0.001));
        List<GraphEdge> edges = Collections.singletonList(new GraphEdge(1L, 2L, 100.0, true));

        PathResult withStairsAllowed = router.findRoute(nodes, edges, 0.0, 0.0, 0.0, 0.001, false);
        assertTrue(withStairsAllowed.isFound());

        PathResult avoidingStairs = router.findRoute(nodes, edges, 0.0, 0.0, 0.0, 0.001, true);
        assertFalse(avoidingStairs.isFound());
    }
}
