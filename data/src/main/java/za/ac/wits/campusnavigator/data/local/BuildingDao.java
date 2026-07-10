package za.ac.wits.campusnavigator.data.local;

import androidx.room.Dao;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BuildingDao {

    @Query("SELECT * FROM Building")
    List<BuildingEntity> getAll();
}
