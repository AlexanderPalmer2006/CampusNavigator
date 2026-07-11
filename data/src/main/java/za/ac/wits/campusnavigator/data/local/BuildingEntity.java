package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for the bundled, read-mostly campus database (ARCHITECTURE-SPINE.md AD-6).
 * Extended in Story 2.1 with `code`/`facultyDepartment` for Building Search and the
 * Building Info Page. Node and Edge are still absent -- Story 2.2's responsibility.
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

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "faculty_department")
    public String facultyDepartment;
}
