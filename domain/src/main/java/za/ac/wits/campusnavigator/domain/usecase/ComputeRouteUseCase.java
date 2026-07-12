package za.ac.wits.campusnavigator.domain.usecase;

import java.util.ArrayList;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Edge;
import za.ac.wits.campusnavigator.domain.model.Node;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.model.Route;
import za.ac.wits.campusnavigator.domain.repository.RoutingRepository;
import za.ac.wits.campusnavigator.domain.result.Result;
import za.ac.wits.campusnavigator.navigationengine.AStarRouter;
import za.ac.wits.campusnavigator.navigationengine.GraphEdge;
import za.ac.wits.campusnavigator.navigationengine.GraphNode;
import za.ac.wits.campusnavigator.navigationengine.Haversine;
import za.ac.wits.campusnavigator.navigationengine.PathResult;
import za.ac.wits.campusnavigator.navigationengine.RoutePoint;

/**
 * Computes a walking route from the current position to a Building (FR-6), offline, via
 * :navigation-engine's pure A* router. Translates :navigation-engine's local "not found"
 * wrapper into :domain's shared {@link Result} type (AD-9) -- this is the seam where that
 * translation happens, :navigation-engine itself has no dependency on Result (AD-5).
 *
 * <p>{@code avoidStairs} (Story 3.1, AD-8, FR-7/FR-11) is passed straight through to
 * {@link AStarRouter}, which excludes stair-tagged edges from the search space entirely
 * when set. The two failure modes are user-facing-distinct (AC 3): a not-found result
 * while {@code avoidStairs} is on means "no step-free path exists" ({@code
 * NO_ACCESSIBLE_ROUTE}), not the generic {@code NO_ROUTE_AVAILABLE}.</p>
 */
public final class ComputeRouteUseCase {

    private final RoutingRepository routingRepository;
    private final AStarRouter router;

    public ComputeRouteUseCase(RoutingRepository routingRepository) {
        this.routingRepository = routingRepository;
        this.router = new AStarRouter();
    }

    public Result<Route> execute(Position currentPosition, Building destination, boolean avoidStairs) {
        List<Node> nodes = routingRepository.getAllNodes();
        List<Edge> edges = routingRepository.getAllEdges();

        List<GraphNode> graphNodes = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            graphNodes.add(new GraphNode(node.getId(), node.getLatitude(), node.getLongitude()));
        }
        List<GraphEdge> graphEdges = new ArrayList<>(edges.size());
        for (Edge edge : edges) {
            graphEdges.add(new GraphEdge(edge.getFromNodeId(), edge.getToNodeId(), edge.getDistanceMeters(), edge.isStairs()));
        }

        PathResult pathResult = router.findRoute(graphNodes, graphEdges,
                currentPosition.getLatitude(), currentPosition.getLongitude(),
                destination.getLatitude(), destination.getLongitude(), avoidStairs);

        if (!pathResult.isFound()) {
            return Result.error(avoidStairs ? Result.ErrorType.NO_ACCESSIBLE_ROUTE : Result.ErrorType.NO_ROUTE_AVAILABLE);
        }

        List<Position> waypoints = new ArrayList<>(pathResult.getWaypoints().size());
        for (RoutePoint point : pathResult.getWaypoints()) {
            waypoints.add(new Position(point.latitude, point.longitude, 0f));
        }
        return Result.success(new Route(waypoints, avoidStairs, totalDistanceMeters(waypoints)));
    }

    /**
     * Story 4.2 (AD-7): the real walked path length -- the sum of the great-circle
     * distance between every consecutive waypoint pair, i.e. the length of the exact
     * polyline this route renders as. Not a straight-line current-position-to-destination
     * shortcut; see {@link Route}'s own Javadoc for why this generalizes correctly to
     * non-straight real-world edges too, not just this app's current hand-authored graph.
     */
    private static double totalDistanceMeters(List<Position> waypoints) {
        double total = 0.0;
        for (int i = 1; i < waypoints.size(); i++) {
            Position from = waypoints.get(i - 1);
            Position to = waypoints.get(i);
            total += Haversine.distanceMeters(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
        }
        return total;
    }
}
