package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertEquals;
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
        Building destination = new Building(2L, "Robert Sobukwe Block", -26.1912, 28.0298, "wits-main", "RSB", null);

        Result<Route> result = useCase.execute(currentPosition, destination);

        assertTrue(result instanceof Result.Success);
        Route route = ((Result.Success<Route>) result).getValue();
        assertEquals(4, route.getWaypoints().size());
    }

    @Test
    public void noRouteExists_returnsErrorNoRouteAvailable() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1L, 0.0, 0.0, "wits-main", "OUTDOOR"));
        nodes.add(new Node(2L, 1.0, 1.0, "wits-main", "OUTDOOR")); // far away, unconnected
        List<Edge> edges = Collections.emptyList();
        ComputeRouteUseCase useCase = new ComputeRouteUseCase(new FakeRoutingRepository(nodes, edges));

        Position currentPosition = new Position(0.0, 0.0, 8.0f);
        Building destination = new Building(2L, "Far Building", 1.0, 1.0, "wits-main", "FAR", null);

        Result<Route> result = useCase.execute(currentPosition, destination);

        assertTrue(result instanceof Result.Error);
        assertEquals(Result.ErrorType.NO_ROUTE_AVAILABLE, ((Result.Error<Route>) result).getErrorType());
    }

    @Test
    public void emptyGraph_returnsErrorNoRouteAvailable() {
        ComputeRouteUseCase useCase =
                new ComputeRouteUseCase(new FakeRoutingRepository(new ArrayList<>(), new ArrayList<>()));

        Position currentPosition = new Position(0.0, 0.0, 8.0f);
        Building destination = new Building(1L, "Anywhere", 1.0, 1.0, "wits-main", "ANY", null);

        Result<Route> result = useCase.execute(currentPosition, destination);

        assertTrue(result instanceof Result.Error);
    }
}
