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

    @Query("SELECT CategoryTag.* FROM CategoryTag "
            + "INNER JOIN BuildingCategoryCrossRef ON CategoryTag.id = BuildingCategoryCrossRef.category_tag_id "
            + "WHERE BuildingCategoryCrossRef.building_id = :buildingId")
    List<CategoryTagEntity> getCategoryTagsForBuilding(long buildingId);

    @Query("SELECT COUNT(*) FROM BuildingPhoto WHERE building_id = :buildingId")
    int getPhotoCountForBuilding(long buildingId);
}
