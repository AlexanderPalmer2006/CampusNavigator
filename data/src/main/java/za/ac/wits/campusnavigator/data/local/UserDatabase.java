package za.ac.wits.campusnavigator.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * The user-generated-data database (ARCHITECTURE-SPINE.md AD-6), independent from the
 * bundled/read-mostly {@link CampusDatabase}. Unlike {@code CampusDatabase}, this is
 * genuinely empty at first launch -- no {@code createFromAsset}, no seed data, no
 * cold-start concern (AD-13 governs the bundled data's load path, not this one). Holds
 * {@code Setting} rows (Story 3.1) and, since version 2 (Story 5.1), {@code FavouriteEntry}
 * rows -- Room cannot enforce a foreign key across the two separate database files, so any
 * cross-database consistency (does a favourited id still resolve to a real Building) is
 * handled explicitly at the use-case layer ({@code GetFavouritesUseCase}), never assumed.
 *
 * <p><strong>Deliberately no {@code .fallbackToDestructiveMigration()}</strong> --
 * unlike {@code CampusDatabase} (a bundled, pre-release reference dataset with nothing
 * irreplaceable to lose), this database holds durable, safety-relevant user state (the
 * Accessibility Preference, and now Favourites). Falling back to a destructive migration
 * here would silently wipe a user's saved preferences/Favourites on a schema bump with no
 * warning. Version 1 had no prior version to migrate from, so this was harmless to omit
 * then; version 2 (Story 5.1) is the first real schema bump, and per that story's own
 * Dev Notes this is exactly the moment the omission was meant to force -- a real
 * {@link Migration} below, not a second destructive-fallback shortcut. Found in code
 * review (2026-07-12, Story 3.1); paid off here (Story 5.1).</p>
 */
@Database(
        entities = {
                SettingEntity.class,
                FavouriteEntity.class
        },
        version = 2,
        exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    /**
     * This project's first real Room {@link Migration} (every prior schema bump was
     * against the bundled, destructively-migrated {@link CampusDatabase}) -- adds the
     * {@code FavouriteEntry} table introduced by {@link FavouriteEntity}, preserving every
     * existing {@code Setting} row untouched. A future schema bump on this database has a
     * real precedent to copy now, not just this class's own cautionary Javadoc.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `FavouriteEntry` "
                    + "(`building_id` INTEGER NOT NULL, `schedule_day` TEXT, PRIMARY KEY(`building_id`))");
        }
    };

    private static volatile UserDatabase instance;

    public abstract SettingDao settingDao();

    public abstract FavouriteDao favouriteDao();

    public static UserDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (UserDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    UserDatabase.class,
                                    "user-data.db")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return instance;
    }
}
