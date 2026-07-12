package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Edge;
import za.ac.wits.campusnavigator.domain.model.Node;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.model.Route;
import za.ac.wits.campusnavigator.domain.repository.FakeRoutingRepository;
import za.ac.wits.campusnavigator.domain.result.Result;

public class ComputeRouteUseCaseTest {

    @Test
    public void routeExists_returnsSuccessWithWaypoints() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1L, -26.1908, 28.0261, "wits-main", "OUTDOOR"));
        nodes.add(new Node(2L, -26.1912, 28.0298, "wits-main", "OUTDOOR"));
        List<Edge> edges = Collections.singletonList(new Edge(1L, 1L, 2L, 371.8, false));
        ComputeRouteUseCase useCase = new ComputeRouteUseCase(new FakeRoutingRepository(nodes, edges));

        Position currentPosition = new Position(-26.1908, 28.0261, 8.0f);
        Building destination = new Building(2L, "Robert Sobukwe Block", -26.1912, 28.0298, "wits-main", "RSB", null, false);

        Result<Route> result = useCase.execute(currentPosition, destination, false);

        assertTrue(result instanceof Result.Success);
        Route route = ((Result.Success<Route>) result).getValue();
        assertEquals(4, route.getWaypoints().size());
        assertFalse("avoidStairs=false must not mark the route accessible", route.isAccessible());
        // currentPosition/destination coincide exactly with node1/node2 -- both snap legs
        // are 0, so getDistanceMeters() must equal the traversed edge's own distanceMeters
        // exactly (Story 4.2, AD-7), not a recomputed approximation of it.
        assertEquals(371.8, route.getDistanceMeters(), 0.01);
    }

    @Test
    public void noRouteExists_returnsErrorNoRouteAvailable() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1L, 0.0, 0.0, "wits-main", "OUTDOOR"));
        nodes.add(new Node(2L, 1.0, 1.0, "wits-main", "OUTDOOR")); // far away, unconnected
        List<Edge> edges = Collections.emptyList();
        ComputeRouteUseCase useCase = new ComputeRouteUseCase(new FakeRoutingRepository(nodes, edges));

        Position currentPosition = new Position(0.0, 0.0, 8.0f);
        Building destination = new Building(2L, "Far Building", 1.0, 1.0, "wits-main", "FAR", null, false);

        Result<Route> result = useCase.execute(currentPosition, destination, false);

        assertTrue(result instanceof Result.Error);
        assertEquals(Result.ErrorType.NO_ROUTE_AVAILABLE, ((Result.Error<Route>) result).getErrorType());
    }

    @Test
    public void emptyGraph_returnsErrorNoRouteAvailable() {
        ComputeRouteUseCase useCase =
                new ComputeRouteUseCase(new FakeRoutingRepository(new ArrayList<>(), new ArrayList<>()));

        Position currentPosition = new Position(0.0, 0.0, 8.0f);
        Building destination = new Building(1L, "Anywhere", 1.0, 1.0, "wits-main", "ANY", null, false);

        Result<Route> result = useCase.execute(currentPosition, destination, false);

        assertTrue(result instanceof Result.Error);
    }

    @Test
    public void avoidStairs_detoursAroundStairEdge_andMarksRouteAccessible() {
        // Direct edge is stairs (short); an alternate step-free path exists via node 3.
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1L, 0.0, 0.0, "wits-main", "OUTDOOR"));
        nodes.add(new Node(2L, 0.0, 0.003, "wits-main", "OUTDOOR"));
        nodes.add(new Node(3L, 0.0, 0.0015, "wits-main", "OUTDOOR"));
        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge(1L, 1L, 2L, 50.0, true));
        edges.add(new Edge(2L, 1L, 3L, 100.0, false));
        edges.add(new Edge(3L, 3L, 2L, 100.0, false));
        ComputeRouteUseCase useCase = new ComputeRouteUseCase(new FakeRoutingRepository(nodes, edges));

        Position currentPosition = new Position(0.0, 0.0, 8.0f);
        Building destination = new Building(2L, "Destination", 0.0, 0.003, "wits-main", "DEST", null, false);

        Result<Route> result = useCase.execute(currentPosition, destination, true);

        assertTrue(result instanceof Result.Success);
        Route route = ((Result.Success<Route>) result).getValue();
        assertEquals(5, route.getWaypoints().size()); // start + n1 + n3 + n2 + dest, detoured
        assertTrue("avoidStairs=true must mark the successful route accessible", route.isAccessible());
    }

    @Test
    public void avoidStairs_returnsErrorNoAccessibleRoute_whenOnlyPathRequiresStairs() {
        // The only edge connecting start and destination is a stair edge.
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1L, 0.0, 0.0, "wits-main", "OUTDOOR"));
        nodes.add(new Node(2L, 0.0, 0.001, "wits-main", "OUTDOOR"));
        List<Edge> edges = Collections.singletonList(new Edge(1L, 1L, 2L, 100.0, true));
        ComputeRouteUseCase useCase = new ComputeRouteUseCase(new FakeRoutingRepository(nodes, edges));

        Position currentPosition = new Position(0.0, 0.0, 8.0f);
        Building destination = new Building(2L, "Destination", 0.0, 0.001, "wits-main", "DEST", null, false);

        Result<Route> result = useCase.execute(currentPosition, destination, true);

        assertTrue(result instanceof Result.Error);
        assertEquals(Result.ErrorType.NO_ACCESSIBLE_ROUTE, ((Result.Error<Route>) result).getErrorType());
    }
}
