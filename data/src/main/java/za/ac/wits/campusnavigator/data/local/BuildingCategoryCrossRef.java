package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Many-to-many join row between {@link BuildingEntity} and {@link CategoryTagEntity}.
 * Story 2.1.
 */
@Entity(tableName = "BuildingCategoryCrossRef", primaryKeys = {"building_id", "category_tag_id"})
public class BuildingCategoryCrossRef {

    @ColumnInfo(name = "building_id")
    public long buildingId;

    @ColumnInfo(name = "category_tag_id")
    public long categoryTagId;
}
