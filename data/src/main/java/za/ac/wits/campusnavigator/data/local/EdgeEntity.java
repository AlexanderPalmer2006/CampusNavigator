package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * An undirected walkable connection between two {@link NodeEntity} rows. {@code isStairs}
 * is reserved for Epic 3's AD-8 (accessible routing) -- unused by this story's routing
 * logic, default {@code false} for every V1 row, schema-ready so Epic 3 needs no
 * migration of its own (Story 2.2 Task 3).
 */
@Entity(tableName = "Edge")
public class EdgeEntity {

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "from_node_id")
    public long fromNodeId;

    @ColumnInfo(name = "to_node_id")
    public long toNodeId;

    @ColumnInfo(name = "distance_meters")
    public double distanceMeters;

    @ColumnInfo(name = "is_stairs")
    public boolean isStairs;
}
