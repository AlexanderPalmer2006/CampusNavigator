package za.ac.wits.campusnavigator.domain.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.BuildingDetails;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;

/**
 * Plain in-memory fake, shared across :domain tests -- :domain has no Android/Room
 * dependency to mock against. {@link #getBuildingDetails(long)} does a simple linear
 * lookup with empty tags/no photo; extend if a future story's tests need richer details.
 *
 * <p>The 1-arg constructor (unchanged since Story 1.1) defaults category-pick fixture
 * data to empty -- most existing tests don't need it. Story 4.2's tests use the 3-arg
 * constructor to supply {@code buildingsByCategory}/{@code commonPickCategories}.</p>
 */
public final class FakeBuildingRepository implements BuildingRepository {

    private final List<Building> buildings;
    private final Map<String, List<Building>> buildingsByCategory;
    private final List<CategoryTag> commonPickCategories;

    public FakeBuildingRepository(List<Building> buildings) {
        this(buildings, Collections.emptyMap(), Collections.emptyList());
    }

    public FakeBuildingRepository(List<Building> buildings, Map<String, List<Building>> buildingsByCategory,
                                   List<CategoryTag> commonPickCategories) {
        this.buildings = buildings;
        this.buildingsByCategory = buildingsByCategory;
        this.commonPickCategories = commonPickCategories;
    }

    @Override
    public List<Building> getAllBuildings() {
        return buildings;
    }

    @Override
    public List<Building> getLandmarkPicks() {
        List<Building> picks = new ArrayList<>();
        for (Building building : buildings) {
            if (building.isLandmarkPick()) {
                picks.add(building);
            }
        }
        return picks;
    }

    @Override
    public BuildingDetails getBuildingDetails(long buildingId) {
        for (Building building : buildings) {
            if (building.getId() == buildingId) {
                return new BuildingDetails(building, Collections.emptyList(), false);
            }
        }
        return null;
    }

    @Override
    public List<Building> getBuildingsByCategory(String categoryName) {
        return buildingsByCategory.getOrDefault(categoryName, Collections.emptyList());
    }

    @Override
    public List<CategoryTag> getCommonPickCategories() {
        return commonPickCategories;
    }
}
