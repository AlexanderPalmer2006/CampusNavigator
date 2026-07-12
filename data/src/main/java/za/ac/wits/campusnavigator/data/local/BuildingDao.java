package za.ac.wits.campusnavigator.data.local;

import androidx.room.Dao;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BuildingDao {

    @Query("SELECT * FROM Building")
    List<BuildingEntity> getAll();

    @Query("SELECT * FROM Building WHERE id = :buildingId")
    BuildingEntity getById(long buildingId);

    @Query("SELECT * FROM Building WHERE is_landmark_pick = 1")
    List<BuildingEntity> getLandmarkPicks();

    @Query("SELECT CategoryTag.* FROM CategoryTag "
            + "INNER JOIN BuildingCategoryCrossRef ON CategoryTag.id = BuildingCategoryCrossRef.category_tag_id "
            + "WHERE BuildingCategoryCrossRef.building_id = :buildingId")
    List<CategoryTagEntity> getCategoryTagsForBuilding(long buildingId);

    @Query("SELECT COUNT(*) FROM BuildingPhoto WHERE building_id = :buildingId")
    int getPhotoCountForBuilding(long buildingId);

    /**
     * Story 4.2 (AD-7): every Building carrying the given category tag, by tag name.
     * {@code ORDER BY Building.id} (Review Findings, 2026-07-12) -- without it, SQLite
     * gives no ordering guarantee, so {@code FindNearestCategoryPickUseCase}'s exact-tie
     * tie-break (its strict {@code <} comparison keeps the first candidate seen at the
     * minimum distance) would be nondeterministic across runs/DB rebuilds instead of a
     * defined, reproducible "lowest id wins a tie" rule.
     */
    @Query("SELECT Building.* FROM Building "
            + "INNER JOIN BuildingCategoryCrossRef ON Building.id = BuildingCategoryCrossRef.building_id "
            + "INNER JOIN CategoryTag ON CategoryTag.id = BuildingCategoryCrossRef.category_tag_id "
            + "WHERE CategoryTag.name = :categoryName "
            + "ORDER BY Building.id")
    List<BuildingEntity> getBuildingsByCategory(String categoryName);

    /**
     * Story 4.2: every CategoryTag curated as a "Category Pick" tile on the Common Picks
     * tab. {@code ORDER BY name} (Review Findings, 2026-07-12) so tile order in the grid
     * is stable/reproducible across runs rather than left to SQLite's unspecified default.
     */
    @Query("SELECT * FROM CategoryTag WHERE is_common_pick_category = 1 ORDER BY name")
    List<CategoryTagEntity> getCommonPickCategories();
}
