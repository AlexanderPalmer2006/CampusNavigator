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
 *
 * <p><strong>Deliberately no {@code .fallbackToDestructiveMigration()}</strong> --
 * unlike {@code CampusDatabase} (a bundled, pre-release reference dataset with nothing
 * irreplaceable to lose), this database holds durable, safety-relevant user state (the
 * Accessibility Preference this story adds, {@code FavouriteEntry} next). Falling back to
 * a destructive migration here would silently wipe a user's saved preferences on the very
 * first future schema bump with no warning -- a real, foreseeable regression for someone
 * who depends on "Always avoid stairs" staying on. Harmless to omit today (version 1, no
 * prior version to migrate from), and it forces whoever bumps this schema next (Story
 * 5.1) to write a real {@code Migration} rather than silently getting away with data loss
 * -- failing loud (a crash) is the correct failure mode for irreplaceable user data,
 * not failing silent (a wipe). Found in code review (2026-07-12).</p>
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
                            .build();
                }
            }
        }
        return instance;
    }
}
