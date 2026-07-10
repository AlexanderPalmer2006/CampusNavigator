package za.ac.wits.campusnavigator.domain.usecase;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;

/**
 * Retrieves every Building for rendering on the Campus Map (FR1). ViewModels call this,
 * never the Repository directly (ARCHITECTURE-SPINE.md AD-1).
 */
public final class GetBuildingsUseCase {

    private final BuildingRepository buildingRepository;

    public GetBuildingsUseCase(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<Building> execute() {
        return buildingRepository.getAllBuildings();
    }
}
