package za.ac.wits.campusnavigator.domain.usecase;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.BuildingFootprint;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;

/**
 * Retrieves every seeded {@link BuildingFootprint} for rendering as filled outlines on the
 * Campus Map (Story 6.3). ViewModels call this, never the Repository directly
 * (ARCHITECTURE-SPINE.md AD-1). Mirrors {@link GetBuildingsUseCase} exactly -- no
 * {@code Result<T>} wrapper, since there is no meaningful expected-failure mode for a bulk
 * read (an empty list is a valid "no footprints seeded yet" state, not a failure).
 */
public final class GetBuildingFootprintsUseCase {

    private final BuildingRepository buildingRepository;

    public GetBuildingFootprintsUseCase(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<BuildingFootprint> execute() {
        return buildingRepository.getAllBuildingFootprints();
    }
}
