package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A routing-graph vertex in the bundled campus database (ARCHITECTURE-SPINE.md AD-6,
 * Structural Seed). Story 2.2.
 */
@Entity(tableName = "Node")
public class NodeEntity {

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "campus_id")
    public String campusId;

    /** {@code OUTDOOR} sentinel for every V1 row -- never a raw null (Structural Seed). */
    @ColumnInfo(name = "level_id")
    public String levelId;
}
