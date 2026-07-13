package za.ac.wits.campusnavigator.app;

import android.util.Log;

/**
 * Demo flavor only (portfolio/visual-pitch artifact, not tracked product scope). Wired in
 * via app/src/demo/AndroidManifest.xml's {@code tools:replace="android:name"} -- this class
 * exists only in the demo variant's compiled sources, never the production variant's, so
 * there is no code path by which the production build could end up running it.
 *
 * <p>Extends the real {@link CampusNavigatorApplication} rather than duplicating its DI
 * wiring -- {@code MainActivity} and every Fragment in this app cast
 * {@code getApplication()} to {@code CampusNavigatorApplication} (see e.g.
 * {@code MainActivity.getGetBuildingsUseCase()}), so the demo flavor needs a real subtype,
 * not a parallel class, or those casts would throw {@code ClassCastException} at runtime.
 * This is the one behavior-neutral change made to the production/main source set for this
 * feature: {@code CampusNavigatorApplication} had its {@code final} modifier removed
 * (nothing else about it changed) purely to permit this subclass to exist.</p>
 *
 * <p>Adds exactly one thing on top of the real app: a startup log line if
 * {@link BuildConfig#MAPTILER_API_KEY} is empty, since Alex does not have a real MapTiler
 * key yet (see app/build.gradle's top comment for exactly where to paste one into
 * local.properties). This is the "graceful failure" path called for by the demo flavor's
 * design -- with an empty key, MapTiler's tile server will simply reject the demo map's
 * tile requests (visible as a blank/background tile area on-screen), never a crash; this
 * log line just makes the reason legible in logcat instead of a silent blank map.</p>
 */
public class DemoCampusNavigatorApplication extends CampusNavigatorApplication {

    private static final String TAG = "CampusNavigatorDemo";

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.MAPTILER_API_KEY == null || BuildConfig.MAPTILER_API_KEY.isEmpty()) {
            Log.w(TAG, "MAPTILER_API_KEY is empty -- no real key has been pasted into "
                    + "local.properties yet (see app/build.gradle's top comment for exactly "
                    + "where). The demo map will load with a blank/placeholder satellite "
                    + "tile area until a real key is added; this is expected, not a bug.");
        }
    }
}
