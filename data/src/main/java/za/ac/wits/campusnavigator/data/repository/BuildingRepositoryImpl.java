package za.ac.wits.campusnavigator.data.repository;

import java.util.ArrayList;
import java.util.List;
import za.ac.wits.campusnavigator.data.local.BuildingDao;
import za.ac.wits.campusnavigator.data.local.BuildingEntity;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;

/**
 * Implements the :domain-defined BuildingRepository against Room. Maps BuildingEntity
 * (Room, :data-only) to Building (plain domain model) -- :domain never sees a Room type.
 */
public final class BuildingRepositoryImpl implements BuildingRepository {

    private final BuildingDao buildingDao;

    public BuildingRepositoryImpl(BuildingDao buildingDao) {
        this.buildingDao = buildingDao;
    }

    @Override
    public List<Building> getAllBuildings() {
        List<BuildingEntity> entities = buildingDao.getAll();
        List<Building> buildings = new ArrayList<>(entities.size());
        for (BuildingEntity entity : entities) {
            buildings.add(new Building(entity.id, entity.name, entity.latitude, entity.longitude, entity.campusId));
        }
        return buildings;
    }
}
