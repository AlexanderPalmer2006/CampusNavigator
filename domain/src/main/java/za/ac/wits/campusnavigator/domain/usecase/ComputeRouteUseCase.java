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
import za.ac.wits.campusnavigator.navigationengine.PathResult;
import za.ac.wits.campusnavigator.navigationengine.RoutePoint;

/**
 * Computes a walking route from the current position to a Building (FR-6), offline, via
 * :navigation-engine's pure A* router. Translates :navigation-engine's local "not found"
 * wrapper into :domain's shared {@link Result} type (AD-9) -- this is the seam where that
 * translation happens, :navigation-engine itself has no dependency on Result (AD-5).
 */
public final class ComputeRouteUseCase {

    private final RoutingRepository routingRepository;
    private final AStarRouter router;

    public ComputeRouteUseCase(RoutingRepository routingRepository) {
        this.routingRepository = routingRepository;
        this.router = new AStarRouter();
    }

    public Result<Route> execute(Position currentPosition, Building destination) {
        List<Node> nodes = routingRepository.getAllNodes();
        List<Edge> edges = routingRepository.getAllEdges();

        List<GraphNode> graphNodes = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            graphNodes.add(new GraphNode(node.getId(), node.getLatitude(), node.getLongitude()));
        }
        List<GraphEdge> graphEdges = new ArrayList<>(edges.size());
        for (Edge edge : edges) {
            graphEdges.add(new GraphEdge(edge.getFromNodeId(), edge.getToNodeId(), edge.getDistanceMeters()));
        }

        PathResult pathResult = router.findRoute(graphNodes, graphEdges,
                currentPosition.getLatitude(), currentPosition.getLongitude(),
                destination.getLatitude(), destination.getLongitude());

        if (!pathResult.isFound()) {
            return Result.error(Result.ErrorType.NO_ROUTE_AVAILABLE);
        }

        List<Position> waypoints = new ArrayList<>(pathResult.getWaypoints().size());
        for (RoutePoint point : pathResult.getWaypoints()) {
            waypoints.add(new Position(point.latitude, point.longitude, 0f));
        }
        return Result.success(new Route(waypoints));
    }
}
