package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.repository.FakeBuildingRepository;

public class GetBuildingsUseCaseTest {

    @Test
    public void execute_returnsAllBuildingsFromRepository() {
        List<Building> seed = new ArrayList<>();
        seed.add(new Building(1L, "FNB Building", -26.1908, 28.0261, "wits-main", "FNB", "Accountancy", false));
        seed.add(new Building(2L, "Robert Sobukwe Block", -26.1912, 28.0298, "wits-main", "RSB", null, false));
        GetBuildingsUseCase useCase = new GetBuildingsUseCase(new FakeBuildingRepository(seed));

        List<Building> result = useCase.execute();

        assertEquals(2, result.size());
        assertEquals("FNB Building", result.get(0).getName());
        assertEquals("wits-main", result.get(0).getCampusId());
    }

    @Test
    public void execute_returnsEmptyListNotNull_whenNoBuildingsExist() {
        GetBuildingsUseCase useCase = new GetBuildingsUseCase(new FakeBuildingRepository(Collections.emptyList()));

        List<Building> result = useCase.execute();

        assertNotNull("Must return an empty list, never null, when no buildings exist", result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void building_equality_isBasedOnIdOnly() {
        Building a = new Building(1L, "FNB Building", -26.1908, 28.0261, "wits-main", "FNB", null, false);
        Building b = new Building(1L, "Different Name Somehow", 0.0, 0.0, "wits-main", null, null, true);

        assertEquals("Buildings with the same id must be equal regardless of other fields", a, b);
    }
}
