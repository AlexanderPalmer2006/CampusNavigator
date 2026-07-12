package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Edge;
import za.ac.wits.campusnavigator.domain.model.Node;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.repository.FakeBuildingRepository;
import za.ac.wits.campusnavigator.domain.repository.FakeRoutingRepository;
import za.ac.wits.campusnavigator.domain.result.Result;

public class FindNearestCategoryPickUseCaseTest {

    @Test
    public void resolvesByRealWalkingDistance_notStraightLine() {
        // Building A sits at Node2, straight-line-closer to the start (0,0) than Building
        // B at Node4 -- but A is only reachable via a detour node (ND), making its real
        // walked path longer than B's direct real path. Verified with plain Haversine
        // arithmetic before writing this test: straight-line A=222m, B=389m; real path
        // A (via detour)=401m, B (direct)=389m -- so the *real* nearest is B, the
        // opposite of what straight-line comparison would pick. This is the one test
        // that actually proves AC 1's "not straight-line" requirement.
        Building buildingA = new Building(1L, "Building A", 0.0, 0.0020, "wits-main", "A", null, false);
        Building buildingB = new Building(2L, "Building B", 0.0, 0.0035, "wits-main", "B", null, false);

        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1L, 0.0, 0.0, "wits-main", "OUTDOOR"));       // start node
        nodes.add(new Node(2L, 0.0, 0.0020, "wits-main", "OUTDOOR"));    // Building A
        nodes.add(new Node(3L, 0.0015, 0.0010, "wits-main", "OUTDOOR")); // detour node
        nodes.add(new Node(4L, 0.0, 0.0035, "wits-main", "OUTDOOR"));    // Building B

        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge(1L, 1L, 3L, 200.5, false)); // start -> detour
        edges.add(new Edge(2L, 3L, 2L, 200.5, false)); // detour -> Building A
        edges.add(new Edge(3L, 1L, 4L, 389.2, false)); // start -> Building B (direct)

        Map<String, List<Building>> byCategory = new HashMap<>();
        byCategory.put("bathroom", Arrays.asList(buildingA, buildingB));

        FindNearestCategoryPickUseCase useCase = new FindNearestCategoryPickUseCase(
                new FakeBuildingRepository(Collections.emptyList(), byCategory, Collections.emptyList()),
                new ComputeRouteUseCase(new FakeRoutingRepository(nodes, edges)));

        Result<Building> result = useCase.execute(new Position(0.0, 0.0, 8.0f), "bathroom", false);

        assertTrue(result instanceof Result.Success);
        assertEquals(buildingB, ((Result.Success<Building>) result).getValue());
    }

    @Test
    public void noBuildingCarriesCategory_returnsNoCategoryMatch() {
        FindNearestCategoryPickUseCase useCase = new FindNearestCategoryPickUseCase(
                new FakeBuildingRepository(Collections.emptyList(), Collections.emptyMap(), Collections.emptyList()),
                new ComputeRouteUseCase(new FakeRoutingRepository(new ArrayList<>(), new ArrayList<>())));

        Result<Building> result = useCase.execute(new Position(0.0, 0.0, 8.0f), "atm", false);

        assertTrue(result instanceof Result.Error);
        assertEquals(Result.ErrorType.NO_CATEGORY_MATCH, ((Result.Error<Building>) result).getErrorType());
    }

    @Test
    public void candidatesExistButUnreachable_returnsNoCategoryMatch() {
        Building buildingA = new Building(1L, "Building A", 1.0, 1.0, "wits-main", "A", null, false);
        Map<String, List<Building>> byCategory = new HashMap<>();
        byCategory.put("bathroom", Collections.singletonList(buildingA));

        // Empty routing graph -- ComputeRouteUseCase always returns Error for every
        // candidate, so resolution must degrade to NO_CATEGORY_MATCH, not throw or return
        // a different ErrorType (per this use case's own documented "same outcome either
        // way" design decision).
        FindNearestCategoryPickUseCase useCase = new FindNearestCategoryPickUseCase(
                new FakeBuildingRepository(Collections.emptyList(), byCategory, Collections.emptyList()),
                new ComputeRouteUseCase(new FakeRoutingRepository(new ArrayList<>(), new ArrayList<>())));

        Result<Building> result = useCase.execute(new Position(0.0, 0.0, 8.0f), "bathroom", false);

        assertTrue(result instanceof Result.Error);
        assertEquals(Result.ErrorType.NO_CATEGORY_MATCH, ((Result.Error<Building>) result).getErrorType());
    }

    @Test
    public void sameCategory_resolvesDifferentlyFromDifferentPositions() {
        // Building X and Building Y both carry "cafeteria," sitting on two far-apart
        // nodes -- AC 3: tapping the same Category Pick from two different locations may
        // correctly resolve to two different Buildings.
        Building buildingX = new Building(1L, "Building X", 0.0, 0.0, "wits-main", "X", null, false);
        Building buildingY = new Building(2L, "Building Y", 0.0, 0.01, "wits-main", "Y", null, false);

        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1L, 0.0, 0.0, "wits-main", "OUTDOOR"));
        nodes.add(new Node(2L, 0.0, 0.01, "wits-main", "OUTDOOR"));
        List<Edge> edges = Collections.singletonList(new Edge(1L, 1L, 2L, 1113.0, false));

        Map<String, List<Building>> byCategory = new HashMap<>();
        byCategory.put("cafeteria", Arrays.asList(buildingX, buildingY));

        FindNearestCategoryPickUseCase useCase = new FindNearestCategoryPickUseCase(
                new FakeBuildingRepository(Collections.emptyList(), byCategory, Collections.emptyList()),
                new ComputeRouteUseCase(new FakeRoutingRepository(nodes, edges)));

        Result<Building> resultNearX = useCase.execute(new Position(0.0, 0.0, 8.0f), "cafeteria", false);
        Result<Building> resultNearY = useCase.execute(new Position(0.0, 0.01, 8.0f), "cafeteria", false);

        assertTrue(resultNearX instanceof Result.Success);
        assertTrue(resultNearY instanceof Result.Success);
        assertEquals(buildingX, ((Result.Success<Building>) resultNearX).getValue());
        assertEquals(buildingY, ((Result.Success<Building>) resultNearY).getValue());
    }
}
