package za.ac.wits.campusnavigator.navigationengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Pure Java A* over a walkway graph -- distance-weighted, and accessibility-aware since
 * Story 3.1 (AD-8): when {@code avoidStairs} is requested, stair-tagged edges are
 * excluded from the search space entirely before the search runs, so "no accessible
 * route" falls out as a natural no-path-exists outcome rather than a cost heuristic.
 */
public final class AStarRouter {

    /**
     * Snaps {@code startLat/startLon} and {@code destLat/destLon} to their nearest
     * {@link GraphNode} by straight-line distance, runs A* between those nodes, and
     * returns the full waypoint list (start point -> nearest start node -> ...path
     * nodes... -> nearest destination node -> destination point), or a not-found result
     * if the graph is empty or the two nodes aren't connected.
     *
     * <p>Equivalent to {@code findRoute(nodes, edges, startLat, startLon, destLat,
     * destLon, false)} -- preserved so Story 2.2's existing callers/tests are unaffected
     * by Story 3.1's accessibility parameter.</p>
     */
    public PathResult findRoute(List<GraphNode> nodes, List<GraphEdge> edges,
                                 double startLat, double startLon, double destLat, double destLon) {
        return findRoute(nodes, edges, startLat, startLon, destLat, destLon, false);
    }

    /**
     * Same as the 6-arg {@link #findRoute}, but when {@code avoidStairs} is {@code true},
     * any edge with {@code isStairs=true} is treated as impassable -- removed from the
     * search space before A* runs, not merely made more expensive (AD-8).
     */
    public PathResult findRoute(List<GraphNode> nodes, List<GraphEdge> edges,
                                 double startLat, double startLon, double destLat, double destLon,
                                 boolean avoidStairs) {
        if (nodes.isEmpty()) {
            return PathResult.notFound();
        }

        GraphNode startNode = nearestNode(nodes, startLat, startLon);
        GraphNode destNode = nearestNode(nodes, destLat, destLon);

        List<GraphNode> path = runAStar(nodes, edges, startNode, destNode, avoidStairs);
        if (path == null) {
            return PathResult.notFound();
        }

        List<RoutePoint> waypoints = new ArrayList<>();
        waypoints.add(new RoutePoint(startLat, startLon));
        for (GraphNode node : path) {
            waypoints.add(new RoutePoint(node.latitude, node.longitude));
        }
        waypoints.add(new RoutePoint(destLat, destLon));
        return PathResult.found(waypoints);
    }

    private GraphNode nearestNode(List<GraphNode> nodes, double lat, double lon) {
        GraphNode nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (GraphNode node : nodes) {
            double distance = Haversine.distanceMeters(lat, lon, node.latitude, node.longitude);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = node;
            }
        }
        return nearest;
    }

    /** A queued node with its priority frozen at insertion time -- see {@link #runAStar}. */
    private static final class QueueEntry {
        final long nodeId;
        final double priority;

        QueueEntry(long nodeId, double priority) {
            this.nodeId = nodeId;
            this.priority = priority;
        }
    }

    private List<GraphNode> runAStar(List<GraphNode> nodes, List<GraphEdge> edges, GraphNode start, GraphNode goal,
                                      boolean avoidStairs) {
        Map<Long, GraphNode> nodesById = new HashMap<>();
        for (GraphNode node : nodes) {
            nodesById.put(node.id, node);
        }

        Map<Long, List<GraphEdge>> adjacency = new HashMap<>();
        for (GraphEdge edge : edges) {
            // No Room-level referential-integrity enforcement on Edge.from_node_id/
            // to_node_id (EdgeEntity has no @ForeignKey) -- a dangling reference must
            // degrade to "this edge doesn't exist" here, not NPE later when the id is
            // looked up in nodesById during path reconstruction.
            if (!nodesById.containsKey(edge.fromNodeId) || !nodesById.containsKey(edge.toNodeId)) {
                continue;
            }
            // AD-8: a stair-tagged edge is impassable when accessible routing is
            // requested -- excluded from the search space entirely, before A* ever
            // runs, not down-weighted or skipped-only-if-a-cheaper-alternative-exists.
            // "No accessible route" must fall out as a natural no-path-exists outcome.
            if (avoidStairs && edge.isStairs) {
                continue;
            }
            adjacency.computeIfAbsent(edge.fromNodeId, k -> new ArrayList<>()).add(edge);
            // Undirected -- register the reverse direction too, since a single row
            // represents a walkable connection both ways.
            adjacency.computeIfAbsent(edge.toNodeId, k -> new ArrayList<>())
                    .add(new GraphEdge(edge.toNodeId, edge.fromNodeId, edge.distanceMeters, edge.isStairs));
        }

        if (start.id == goal.id) {
            return Collections.singletonList(start);
        }

        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, Long> cameFrom = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        gScore.put(start.id, 0.0);

        // Priority is computed once, at insertion, and frozen in the QueueEntry -- java.util
        // .PriorityQueue's contract requires a queued element's comparison result to stay
        // stable while it's enqueued, and gScore keeps mutating as relaxation proceeds. A
        // live lookup (recomputing priority from the current gScore on every comparison,
        // as before) can violate the heap invariant for entries already sitting in the
        // queue. Standard lazy-deletion fix: push a new frozen-priority entry on every
        // relaxation; the `visited` check below skips any stale duplicate once a node's
        // true shortest distance has been finalized.
        double startPriority = Haversine.distanceMeters(start.latitude, start.longitude, goal.latitude, goal.longitude);
        PriorityQueue<QueueEntry> openSet = new PriorityQueue<>(Comparator.comparingDouble(e -> e.priority));
        openSet.add(new QueueEntry(start.id, startPriority));

        while (!openSet.isEmpty()) {
            long currentId = openSet.poll().nodeId;
            if (currentId == goal.id) {
                return reconstructPath(cameFrom, nodesById, goal.id);
            }
            if (!visited.add(currentId)) {
                continue;
            }

            for (GraphEdge edge : adjacency.getOrDefault(currentId, Collections.emptyList())) {
                double tentativeG = gScore.get(currentId) + edge.distanceMeters;
                if (tentativeG < gScore.getOrDefault(edge.toNodeId, Double.MAX_VALUE)) {
                    gScore.put(edge.toNodeId, tentativeG);
                    cameFrom.put(edge.toNodeId, currentId);
                    GraphNode toNode = nodesById.get(edge.toNodeId);
                    double priority = tentativeG
                            + Haversine.distanceMeters(toNode.latitude, toNode.longitude, goal.latitude, goal.longitude);
                    openSet.add(new QueueEntry(edge.toNodeId, priority));
                }
            }
        }

        return null;
    }

    private List<GraphNode> reconstructPath(Map<Long, Long> cameFrom, Map<Long, GraphNode> nodesById, long goalId) {
        List<GraphNode> path = new ArrayList<>();
        Long currentId = goalId;
        while (currentId != null) {
            path.add(nodesById.get(currentId));
            currentId = cameFrom.get(currentId);
        }
        Collections.reverse(path);
        return path;
    }
}
