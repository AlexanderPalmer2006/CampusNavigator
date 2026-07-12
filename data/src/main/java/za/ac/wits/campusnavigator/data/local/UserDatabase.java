package za.ac.wits.campusnavigator.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The user-generated-data database (ARCHITECTURE-SPINE.md AD-6), independent from the
 * bundled/read-mostly {@link CampusDatabase}. Unlike {@code CampusDatabase}, this is
 * genuinely empty at first launch -- no {@code createFromAsset}, no seed data, no
 * cold-start concern (AD-13 governs the bundled data's load path, not this one). Holds
 * {@code Setting} rows (Story 3.1); {@code FavouriteEntry} (Story 5.1) will join it later,
 * with its own independent migration path from this one -- Room cannot enforce a foreign
 * key across the two separate database files, so any cross-database consistency is
 * handled explicitly at the repository layer, never assumed.
 */
@Database(
        entities = {
                SettingEntity.class
        },
        version = 1,
        exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    private static volatile UserDatabase instance;

    public abstract SettingDao settingDao();

    public static UserDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (UserDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    UserDatabase.class,
                                    "user-data.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
