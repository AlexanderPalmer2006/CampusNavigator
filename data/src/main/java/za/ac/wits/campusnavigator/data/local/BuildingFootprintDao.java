package za.ac.wits.campusnavigator.data.local;

import androidx.room.Dao;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BuildingFootprintDao {

    /**
     * Every footprint ring, across every Building -- the map renders every visible fill at
     * once, the same "whole-map" read shape {@link BuildingDao#getAll()} already uses for
     * Building labels, not a per-Building filtered read.
     */
    @Query("SELECT * FROM BuildingFootprint")
    List<BuildingFootprintEntity> getAll();
}
