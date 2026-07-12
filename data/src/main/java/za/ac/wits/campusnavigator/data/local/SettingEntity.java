package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * A single user preference row in the user-data database (ARCHITECTURE-SPINE.md AD-6).
 * Key-value shape (not a single fixed-column boolean row) so a future Setting (e.g. Story
 * 5.2's Dark Mode) can be added without its own schema migration -- same "absorb-growth
 * seed" reasoning already used for {@code Node}/{@code Edge}'s {@code is_stairs} and
 * {@code Building}'s {@code isLandmarkPick}. Story 3.1's only key is
 * {@code "accessibility_preference"} (see {@code SettingsRepositoryImpl}), value
 * {@code "true"}/{@code "false"}.
 */
@Entity(tableName = "Setting")
public class SettingEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    public String key = "";

    @ColumnInfo(name = "value")
    public String value;
}
