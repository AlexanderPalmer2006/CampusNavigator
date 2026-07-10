package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for the bundled, read-mostly campus database (ARCHITECTURE-SPINE.md AD-6).
 * Only Building lives here for Story 1.1 -- CategoryTag, BuildingPhoto, Node, and Edge are
 * added later, in Epic 2, by the story that first needs them.
 */
@Entity(tableName = "Building")
public class BuildingEntity {

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "campus_id")
    public String campusId;
}
