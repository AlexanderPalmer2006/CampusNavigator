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
        List<GraphEdge> edges = Collections.singletonList(new GraphEdge(1L, 2L, 350.0));

        PathResult result = router.findRoute(nodes, edges, -26.1908, 28.0261, -26.1912, 28.0298);

        assertTrue(result.isFound());
        // start point + node1 + node2 + dest point
        assertEquals(4, result.getWaypoints().size());
    }

    @Test
    public void multiHopPath_routesThroughIntermediateNode() {
        // 1 -- 2 -- 3, no direct 1-3 edge -- forces the router to actually pathfind.
        List<GraphNode> nodes = Arrays.asList(
                new GraphNode(1L, 0.0, 0.0),
                new GraphNode(2L, 0.0, 0.001),
                new GraphNode(3L, 0.0, 0.002));
        List<GraphEdge> edges = Arrays.asList(
                new GraphEdge(1L, 2L, 100.0),
                new GraphEdge(2L, 3L, 100.0));

        PathResult result = router.findRoute(nodes, edges, 0.0, 0.0, 0.0, 0.002);

        assertTrue(result.isFound());
        assertEquals(5, result.getWaypoints().size()); // start + n1 + n2 + n3 + dest
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
                new GraphEdge(1L, 2L, 50.0),
                new GraphEdge(2L, 4L, 50.0),
                new GraphEdge(1L, 3L, 500.0),
                new GraphEdge(3L, 4L, 500.0));

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
                new GraphEdge(1L, 99L, 50.0),
                new GraphEdge(1L, 2L, 100.0));

        PathResult result = router.findRoute(nodes, edges, 0.0, 0.0, 0.0, 0.001);

        assertTrue(result.isFound());
        assertEquals(4, result.getWaypoints().size()); // start + n1 + n2 + dest, via the valid edge
    }
}
