package za.ac.wits.campusnavigator.domain.usecase;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;

/**
 * Retrieves every Building curated as a "Landmark Pick" for the Common Picks tab (FR-8,
 * Story 4.1). ViewModels call this, never the Repository directly (ARCHITECTURE-SPINE.md
 * AD-1). Plain list return, not {@code Result<T>} -- an empty list ("no Landmark Picks
 * curated") is a valid state, not an expected-failure case, same reasoning as
 * {@link GetBuildingsUseCase}.
 */
public final class GetLandmarkPicksUseCase {

    private final BuildingRepository buildingRepository;

    public GetLandmarkPicksUseCase(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<Building> execute() {
        return buildingRepository.getLandmarkPicks();
    }
}
