package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for the bundled, read-mostly campus database (ARCHITECTURE-SPINE.md AD-6).
 * Extended in Story 2.1 with `code`/`facultyDepartment` for Building Search and the
 * Building Info Page. Node and Edge are still absent -- Story 2.2's responsibility.
 * Extended in Story 4.1 with `isLandmarkPick` for the Common Picks tab -- a real schema
 * addition, not a pre-existing reserved column despite ARCHITECTURE-SPINE.md's Structural
 * Seed section describing it as already reserved back in Story 1.1 (verified false against
 * this file and the live campus.db schema before this story added it for real).
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

    /**
     * Curation flag for the Common Picks tab (Story 4.1, FR-8) -- independent of the
     * descriptive "landmark" {@link CategoryTagEntity} some Buildings also carry.
     */
    @ColumnInfo(name = "is_landmark_pick", defaultValue = "0")
    public boolean isLandmarkPick;
}
