package za.ac.wits.campusnavigator.data.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import za.ac.wits.campusnavigator.data.local.BuildingDao;
import za.ac.wits.campusnavigator.data.local.BuildingEntity;
import za.ac.wits.campusnavigator.data.local.BuildingFootprintDao;
import za.ac.wits.campusnavigator.data.local.BuildingFootprintEntity;
import za.ac.wits.campusnavigator.data.local.CategoryTagEntity;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.BuildingDetails;
import za.ac.wits.campusnavigator.domain.model.BuildingFootprint;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;

/**
 * Implements the :domain-defined BuildingRepository against Room. Maps every Room entity
 * (Room, :data-only) to its plain domain counterpart -- :domain never sees a Room type.
 */
public final class BuildingRepositoryImpl implements BuildingRepository {

    private final BuildingDao buildingDao;
    private final BuildingFootprintDao buildingFootprintDao;

    public BuildingRepositoryImpl(BuildingDao buildingDao, BuildingFootprintDao buildingFootprintDao) {
        this.buildingDao = buildingDao;
        this.buildingFootprintDao = buildingFootprintDao;
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

    /**
     * Story 6.3: groups the flat list of ring rows by {@code building_id} into one
     * {@link BuildingFootprint} per Building (a Building with multiple rings gets one
     * {@code BuildingFootprint} whose {@code rings} list has more than one entry) --
     * {@code LinkedHashMap} preserves first-seen order, matching the row order Room returns
     * rather than an arbitrary re-sort. A row whose {@code ring_geojson} fails to parse is
     * skipped for that one row rather than aborting the whole read (a single malformed seed
     * row must not blank out every other Building's fill) -- logged nowhere further since
     * this is bundled, author-controlled seed data, not user input; a real parse failure
     * here is a seed-data bug to fix at the source, not a runtime condition to handle
     * gracefully forever.
     */
    @Override
    public List<BuildingFootprint> getAllBuildingFootprints() {
        List<BuildingFootprintEntity> entities = buildingFootprintDao.getAll();
        Map<Long, List<List<Position>>> ringsByBuilding = new LinkedHashMap<>();
        for (BuildingFootprintEntity entity : entities) {
            List<Position> ring = parseRing(entity.ringGeoJson);
            if (ring == null) {
                continue;
            }
            ringsByBuilding.computeIfAbsent(entity.buildingId, id -> new ArrayList<>()).add(ring);
        }
        List<BuildingFootprint> footprints = new ArrayList<>(ringsByBuilding.size());
        for (Map.Entry<Long, List<List<Position>>> entry : ringsByBuilding.entrySet()) {
            footprints.add(new BuildingFootprint(entry.getKey(), entry.getValue()));
        }
        return footprints;
    }

    /**
     * Parses a flat {@code "[[lon,lat],[lon,lat],...]"} JSON array into an ordered vertex
     * list. Hand-rolled with {@code org.json} (already on the Android platform classpath,
     * no new dependency) rather than a full JSON library -- this is one column's worth of
     * simple, flat, author-controlled data, not a general-purpose parsing need.
     */
    private static List<Position> parseRing(String ringGeoJson) {
        try {
            JSONArray points = new JSONArray(ringGeoJson);
            List<Position> ring = new ArrayList<>(points.length());
            for (int i = 0; i < points.length(); i++) {
                JSONArray point = points.getJSONArray(i);
                double lon = point.getDouble(0);
                double lat = point.getDouble(1);
                // accuracyMeters = 0: not a GPS reading, same reuse-of-Position convention
                // Route.java's own Javadoc already documents for waypoints.
                ring.add(new Position(lat, lon, 0f));
            }
            return ring;
        } catch (JSONException e) {
            return null;
        }
    }

    private static Building toDomain(BuildingEntity entity) {
        return new Building(entity.id, entity.name, entity.latitude, entity.longitude,
                entity.campusId, entity.code, entity.facultyDepartment, entity.isLandmarkPick);
    }

    private static CategoryTag toDomain(CategoryTagEntity entity) {
        return new CategoryTag(entity.id, entity.name, entity.isCommonPickCategory);
    }
}
