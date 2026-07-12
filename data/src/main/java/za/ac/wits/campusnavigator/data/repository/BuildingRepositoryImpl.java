package za.ac.wits.campusnavigator.data.repository;

import java.util.ArrayList;
import java.util.List;
import za.ac.wits.campusnavigator.data.local.BuildingDao;
import za.ac.wits.campusnavigator.data.local.BuildingEntity;
import za.ac.wits.campusnavigator.data.local.CategoryTagEntity;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.BuildingDetails;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;

/**
 * Implements the :domain-defined BuildingRepository against Room. Maps every Room entity
 * (Room, :data-only) to its plain domain counterpart -- :domain never sees a Room type.
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
            buildings.add(toDomain(entity));
        }
        return buildings;
    }

    @Override
    public List<Building> getLandmarkPicks() {
        List<BuildingEntity> entities = buildingDao.getLandmarkPicks();
        List<Building> buildings = new ArrayList<>(entities.size());
        for (BuildingEntity entity : entities) {
            buildings.add(toDomain(entity));
        }
        return buildings;
    }

    @Override
    public BuildingDetails getBuildingDetails(long buildingId) {
        BuildingEntity entity = buildingDao.getById(buildingId);
        if (entity == null) {
            // Unknown/stale id -- matches FakeBuildingRepository's contract (Review
            // Findings): an expected "not found" outcome, not an exceptional one.
            return null;
        }
        List<CategoryTagEntity> tagEntities = buildingDao.getCategoryTagsForBuilding(buildingId);
        List<CategoryTag> tags = new ArrayList<>(tagEntities.size());
        for (CategoryTagEntity tagEntity : tagEntities) {
            tags.add(toDomain(tagEntity));
        }
        boolean hasPhoto = buildingDao.getPhotoCountForBuilding(buildingId) > 0;
        return new BuildingDetails(toDomain(entity), tags, hasPhoto);
    }

    @Override
    public List<Building> getBuildingsByCategory(String categoryName) {
        List<BuildingEntity> entities = buildingDao.getBuildingsByCategory(categoryName);
        List<Building> buildings = new ArrayList<>(entities.size());
        for (BuildingEntity entity : entities) {
            buildings.add(toDomain(entity));
        }
        return buildings;
    }

    @Override
    public List<CategoryTag> getCommonPickCategories() {
        List<CategoryTagEntity> entities = buildingDao.getCommonPickCategories();
        List<CategoryTag> tags = new ArrayList<>(entities.size());
        for (CategoryTagEntity entity : entities) {
            tags.add(toDomain(entity));
        }
        return tags;
    }

    @Override
    public Building getBuildingById(long buildingId) {
        BuildingEntity entity = buildingDao.getById(buildingId);
        return entity == null ? null : toDomain(entity);
    }

    private static Building toDomain(BuildingEntity entity) {
        return new Building(entity.id, entity.name, entity.latitude, entity.longitude,
                entity.campusId, entity.code, entity.facultyDepartment, entity.isLandmarkPick);
    }

    private static CategoryTag toDomain(CategoryTagEntity entity) {
        return new CategoryTag(entity.id, entity.name, entity.isCommonPickCategory);
    }
}
