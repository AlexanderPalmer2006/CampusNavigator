package za.ac.wits.campusnavigator.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FavouriteDao {

    /**
     * {@code OnConflictStrategy.IGNORE} (not {@code REPLACE}) is deliberate (Story 5.1,
     * AC 2): {@code REPLACE} would still perform a write and could silently null out a
     * future {@code scheduleDay} value on a re-save; {@code IGNORE} leaves an
     * already-favourited row completely untouched -- a true no-op, not just "no visible
     * duplicate."
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(FavouriteEntity entity);

    @Query("DELETE FROM FavouriteEntry WHERE building_id = :buildingId")
    void delete(long buildingId);

    @Query("SELECT EXISTS(SELECT 1 FROM FavouriteEntry WHERE building_id = :buildingId)")
    boolean exists(long buildingId);

    // Code review fix (2026-07-13): without an explicit ORDER BY, SQLite gives no
    // ordering guarantee -- the exact nondeterminism BuildingDao.getBuildingsByCategory/
    // getCommonPickCategories were fixed for in Story 4.2's own review. Ordered by
    // building_id (insertion order has no durable meaning once a row can be deleted and
    // re-added) so the Favourites list order is stable across loads.
    @Query("SELECT building_id FROM FavouriteEntry ORDER BY building_id")
    List<Long> getAllBuildingIds();
}
