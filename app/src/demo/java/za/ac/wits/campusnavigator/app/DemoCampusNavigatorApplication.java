package za.ac.wits.campusnavigator.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;

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
 *
 * <p>Also wires the MapTiler logo's tap-to-open-maptiler.com behavior (required by
 * MapTiler's free-plan terms alongside the text attribution already in this flavor's
 * strings.xml -- see app/src/demo/res/layout/activity_main.xml's own comment for the
 * requirement citation) via {@link Application.ActivityLifecycleCallbacks} rather than
 * subclassing {@code MainActivity} -- {@code MainActivity} is {@code final} and shared
 * with the production build; this keeps that file, and every other shared/production
 * source file, completely untouched by this feature.</p>
 */
public class DemoCampusNavigatorApplication extends CampusNavigatorApplication {

    private static final String TAG = "CampusNavigatorDemo";
    private static final String MAPTILER_HOMEPAGE_URL = "https://www.maptiler.com";

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.MAPTILER_API_KEY == null || BuildConfig.MAPTILER_API_KEY.isEmpty()) {
            Log.w(TAG, "MAPTILER_API_KEY is empty -- no real key has been pasted into "
                    + "local.properties yet (see app/build.gradle's top comment for exactly "
                    + "where). The demo map will load with a blank/placeholder satellite "
                    + "tile area until a real key is added; this is expected, not a bug.");
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                // Deliberately NOT wired here: onActivityCreated fires from inside the
                // base Activity.onCreate(), before MainActivity's own onCreate() body
                // (and its setContentView() call) has run -- findViewById would always
                // return null at this point. onActivityResumed (below) is the first
                // callback guaranteed to fire after onCreate() has fully completed.
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                // findViewById returns null on any Activity that doesn't inflate the
                // demo activity_main.xml layout (there is only ever one in this app,
                // MainActivity, but this guard costs nothing and avoids a hard
                // same-Activity-class assumption). setOnClickListener() is idempotent
                // to call again on every resume (e.g. after a tab switch/back-stack
                // restore) -- it just replaces the listener with an equivalent one.
                View logo = activity.findViewById(R.id.maptilerLogo);
                if (logo != null) {
                    logo.setOnClickListener(v -> activity.startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(MAPTILER_HOMEPAGE_URL))));
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }
}
