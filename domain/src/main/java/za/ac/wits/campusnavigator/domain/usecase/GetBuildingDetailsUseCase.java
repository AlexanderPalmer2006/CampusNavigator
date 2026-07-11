package za.ac.wits.campusnavigator.domain.usecase;

import za.ac.wits.campusnavigator.domain.model.BuildingDetails;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;

/**
 * Retrieves the Building Info Page aggregate for one Building (FR-5, Story 2.1).
 * ViewModels call this, never the Repository directly (ARCHITECTURE-SPINE.md AD-1).
 */
public final class GetBuildingDetailsUseCase {

    private final BuildingRepository buildingRepository;

    public GetBuildingDetailsUseCase(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public BuildingDetails execute(long buildingId) {
        return buildingRepository.getBuildingDetails(buildingId);
    }
}
