package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.BuildingDetails;
import za.ac.wits.campusnavigator.domain.repository.FakeBuildingRepository;

public class GetBuildingDetailsUseCaseTest {

    @Test
    public void execute_returnsDetailsForKnownBuilding() {
        List<Building> seed = new ArrayList<>();
        seed.add(new Building(1L, "Central Library", -26.1922, 28.0302, "wits-main", "LIB", null, true));
        GetBuildingDetailsUseCase useCase = new GetBuildingDetailsUseCase(new FakeBuildingRepository(seed));

        BuildingDetails details = useCase.execute(1L);

        assertEquals("Central Library", details.getBuilding().getName());
        assertFalse("Fake repository seeds no photos", details.hasPhoto());
    }

    @Test
    public void execute_returnsNull_whenBuildingUnknown() {
        GetBuildingDetailsUseCase useCase = new GetBuildingDetailsUseCase(new FakeBuildingRepository(new ArrayList<>()));

        assertNull(useCase.execute(999L));
    }
}
