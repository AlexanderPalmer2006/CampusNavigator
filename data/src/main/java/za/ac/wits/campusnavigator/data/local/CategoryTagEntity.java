package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A campus-wide category (e.g. "library") a Building can be tagged with -- many-to-many
 * via {@link BuildingCategoryCrossRef} (ARCHITECTURE-SPINE.md ER diagram). Story 2.1.
 */
@Entity(tableName = "CategoryTag")
public class CategoryTagEntity {

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "name")
    public String name;
}
