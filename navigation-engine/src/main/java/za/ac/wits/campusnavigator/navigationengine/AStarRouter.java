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
 * Pure Java A* over a walkway graph -- distance-weighted, not yet accessibility-aware
 * (that's Epic 3's job, consuming {@code GraphEdge}'s reserved stair-tag data at the
 * :domain/:data layer, not here). Story 2.2 Task 1.
 */
public final class AStarRouter {

    /**
     * Snaps {@code startLat/startLon} and {@code destLat/destLon} to their nearest
     * {@link GraphNode} by straight-line distance, runs A* between those nodes, and
     * returns the full waypoint list (start point -> nearest start node -> ...path
     * nodes... -> nearest destination node -> destination point), or a not-found result
     * if the graph is empty or the two nodes aren't connected.
     */
    public PathResult findRoute(List<GraphNode> nodes, List<GraphEdge> edges,
                                 double startLat, double startLon, double destLat, double destLon) {
        if (nodes.isEmpty()) {
            return PathResult.notFound();
        }

        GraphNode startNode = nearestNode(nodes, startLat, startLon);
        GraphNode destNode = nearestNode(nodes, destLat, destLon);

        List<GraphNode> path = runAStar(nodes, edges, startNode, destNode);
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

    private List<GraphNode> runAStar(List<GraphNode> nodes, List<GraphEdge> edges, GraphNode start, GraphNode goal) {
        Map<Long, GraphNode> nodesById = new HashMap<>();
        for (GraphNode node : nodes) {
            nodesById.put(node.id, node);
        }

        Map<Long, List<GraphEdge>> adjacency = new HashMap<>();
        for (GraphEdge edge : edges) {
            adjacency.computeIfAbsent(edge.fromNodeId, k -> new ArrayList<>()).add(edge);
            // Undirected -- register the reverse direction too, since a single row
            // represents a walkable connection both ways.
            adjacency.computeIfAbsent(edge.toNodeId, k -> new ArrayList<>())
                    .add(new GraphEdge(edge.toNodeId, edge.fromNodeId, edge.distanceMeters));
        }

        if (start.id == goal.id) {
            return Collections.singletonList(start);
        }

        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, Long> cameFrom = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        gScore.put(start.id, 0.0);

        PriorityQueue<Long> openSet = new PriorityQueue<>(Comparator.comparingDouble(
                nodeId -> gScore.getOrDefault(nodeId, Double.MAX_VALUE)
                        + Haversine.distanceMeters(nodesById.get(nodeId).latitude, nodesById.get(nodeId).longitude,
                        goal.latitude, goal.longitude)));
        openSet.add(start.id);

        while (!openSet.isEmpty()) {
            long currentId = openSet.poll();
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
                    openSet.add(edge.toNodeId);
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
