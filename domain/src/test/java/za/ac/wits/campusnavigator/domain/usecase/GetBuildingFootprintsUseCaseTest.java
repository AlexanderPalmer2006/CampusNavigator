package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.BuildingFootprint;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.repository.FakeBuildingRepository;

public class GetBuildingFootprintsUseCaseTest {

    @Test
    public void execute_returnsAllFootprintsFromRepository() {
        List<Position> ring = new ArrayList<>();
        ring.add(new Position(-26.1904, 28.0285, 0f));
        ring.add(new Position(-26.1905, 28.0286, 0f));
        ring.add(new Position(-26.1906, 28.0285, 0f));
        List<BuildingFootprint> seed = Collections.singletonList(new BuildingFootprint(3L, Collections.singletonList(ring)));
        GetBuildingFootprintsUseCase useCase =
                new GetBuildingFootprintsUseCase(new FakeBuildingRepository(Collections.emptyList(),
                        Collections.emptyMap(), Collections.emptyList(), seed));

        List<BuildingFootprint> result = useCase.execute();

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getBuildingId());
        assertEquals(1, result.get(0).getRings().size());
        assertEquals(3, result.get(0).getRings().get(0).size());
    }

    @Test
    public void execute_returnsEmptyListNotNull_whenNoFootprintsSeeded() {
        GetBuildingFootprintsUseCase useCase =
                new GetBuildingFootprintsUseCase(new FakeBuildingRepository(Collections.emptyList()));

        List<BuildingFootprint> result = useCase.execute();

        assertNotNull("Must return an empty list, never null, when no footprints exist", result);
        assertTrue(result.isEmpty());
    }
}
