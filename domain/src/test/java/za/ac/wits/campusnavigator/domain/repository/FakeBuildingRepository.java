package za.ac.wits.campusnavigator.domain.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.BuildingDetails;

/**
 * Plain in-memory fake, shared across :domain tests -- :domain has no Android/Room
 * dependency to mock against. {@link #getBuildingDetails(long)} does a simple linear
 * lookup with empty tags/no photo; extend if a future story's tests need richer details.
 */
public final class FakeBuildingRepository implements BuildingRepository {

    private final List<Building> buildings;

    public FakeBuildingRepository(List<Building> buildings) {
        this.buildings = buildings;
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
}
