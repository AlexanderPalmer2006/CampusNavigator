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

    /**
     * Curated flag (Story 4.2) marking this tag as a "Category Pick" tile on the Common
     * Picks tab (e.g. "bathroom", "cafeteria") -- separate from the purely descriptive
     * tags shown on the Building Info Page (e.g. "library", "museum"). Mirrors
     * {@link BuildingEntity#isLandmarkPick}'s exact same curation-flag pattern, applied
     * one level over. A tag can be Category-Pick-curated, descriptive, both, or neither.
     */
    @ColumnInfo(name = "is_common_pick_category")
    public boolean isCommonPickCategory;
}
