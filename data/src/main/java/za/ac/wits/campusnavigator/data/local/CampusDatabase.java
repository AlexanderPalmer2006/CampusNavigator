package za.ac.wits.campusnavigator.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The bundled, read-mostly campus database (ARCHITECTURE-SPINE.md AD-6, AD-13).
 *
 * Loaded via {@code createFromAsset} so the pre-built seed data ships inside the APK and
 * loads directly -- no first-run parsing/import step, no DAO insert calls at startup. This
 * is what actually satisfies AD-13's cold-start rule; populating rows via DAO inserts on
 * first launch would violate it just as surely as an un-copied mbtiles file would for the
 * map tiles.
 */
@Database(entities = {BuildingEntity.class}, version = 1, exportSchema = false)
public abstract class CampusDatabase extends RoomDatabase {

    private static volatile CampusDatabase instance;

    public abstract BuildingDao buildingDao();

    public static CampusDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (CampusDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    CampusDatabase.class,
                                    "campus.db")
                            .createFromAsset("database/campus.db")
                            .build();
                }
            }
        }
        return instance;
    }
}
