package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.repository.FakeBuildingRepository;

public class GetLandmarkPicksUseCaseTest {

    @Test
    public void execute_returnsOnlyBuildingsFlaggedAsLandmarkPicks() {
        List<Building> seed = new ArrayList<>();
        seed.add(new Building(1L, "FNB Building", -26.1908, 28.0261, "wits-main", "FNB", "Accountancy", false));
        seed.add(new Building(2L, "Robert Sobukwe Block", -26.1912, 28.0298, "wits-main", "RSB", null, false));
        seed.add(new Building(3L, "Great Hall", -26.1904, 28.0285, "wits-main", "GH", null, true));
        seed.add(new Building(4L, "Central Library", -26.1922, 28.0302, "wits-main", "LIB", null, true));
        GetLandmarkPicksUseCase useCase = new GetLandmarkPicksUseCase(new FakeBuildingRepository(seed));

        List<Building> result = useCase.execute();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Building::isLandmarkPick));
        assertTrue(result.stream().anyMatch(b -> b.getName().equals("Great Hall")));
        assertTrue(result.stream().anyMatch(b -> b.getName().equals("Central Library")));
    }

    @Test
    public void execute_returnsEmptyListNotNull_whenNoLandmarkPicksCurated() {
        List<Building> seed = new ArrayList<>();
        seed.add(new Building(1L, "FNB Building", -26.1908, 28.0261, "wits-main", "FNB", "Accountancy", false));
        GetLandmarkPicksUseCase useCase = new GetLandmarkPicksUseCase(new FakeBuildingRepository(seed));

        List<Building> result = useCase.execute();

        assertTrue(result.isEmpty());
    }
}
