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

    @Query("SELECT building_id FROM FavouriteEntry")
    List<Long> getAllBuildingIds();
}
