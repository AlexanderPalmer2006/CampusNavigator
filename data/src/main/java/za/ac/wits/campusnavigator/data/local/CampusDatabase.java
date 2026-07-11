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
 *
 * Version 2 (Story 2.1) adds CategoryTag/BuildingCategoryCrossRef/BuildingPhoto. Version 3
 * (Story 2.2) adds Node/Edge (the walkway routing graph). No Migration class -- this is a
 * bundled, pre-release reference dataset with no user-generated data to preserve, so a
 * schema bump destructively rebuilds it from the fresh bundled asset (Review Findings:
 * fixes a real `IllegalStateException` on any device with a prior version already
 * installed, e.g. `adb install -r` without an uninstall first).
 */
@Database(
        entities = {
                BuildingEntity.class,
                CategoryTagEntity.class,
                BuildingCategoryCrossRef.class,
                BuildingPhotoEntity.class,
                NodeEntity.class,
                EdgeEntity.class
        },
        version = 3,
        exportSchema = false)
public abstract class CampusDatabase extends RoomDatabase {

    private static volatile CampusDatabase instance;

    public abstract BuildingDao buildingDao();

    public abstract RoutingDao routingDao();

    public static CampusDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (CampusDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    CampusDatabase.class,
                                    "campus.db")
                            .createFromAsset("database/campus.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
