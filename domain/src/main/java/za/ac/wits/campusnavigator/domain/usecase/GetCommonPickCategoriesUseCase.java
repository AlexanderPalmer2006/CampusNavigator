package za.ac.wits.campusnavigator.domain.usecase;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;

/**
 * Retrieves every {@link CategoryTag} curated as a "Category Pick" tile for the Common
 * Picks tab (FR-8, Story 4.2). ViewModels call this, never the Repository directly
 * (ARCHITECTURE-SPINE.md AD-1). Plain list return, not {@code Result<T>} -- an empty list
 * ("no Category Picks curated") is a valid state, not an expected-failure case, same
 * reasoning and shape as {@link GetLandmarkPicksUseCase}.
 */
public final class GetCommonPickCategoriesUseCase {

    private final BuildingRepository buildingRepository;

    public GetCommonPickCategoriesUseCase(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<CategoryTag> execute() {
        return buildingRepository.getCommonPickCategories();
    }
}
