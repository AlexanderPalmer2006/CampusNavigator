package za.ac.wits.campusnavigator.ui.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.model.Position;

/**
 * Wraps {@link android.location.LocationManager} directly -- no Google Play Services /
 * FusedLocationProviderClient, consistent with this project's zero-Play-Services footprint
 * (ARCHITECTURE-SPINE.md Stack table references neither). Story 1.2 Dev Notes: "LocationProvider
 * Contract (resolved design)".
 */
public final class AndroidLocationProvider implements LocationProvider {

    // Adaptive polling (NFR3, battery efficiency): fast while moving, slower once the user
    // appears stationary. A judgment call, not a numeric spec requirement -- retune here if
    // real-world behavior suggests otherwise.
    private static final long FAST_INTERVAL_MS = 15_000L;
    private static final long SLOW_INTERVAL_MS = 45_000L;
    private static final float STATIONARY_DISTANCE_METERS = 5f;
    private static final int STATIONARY_FIX_COUNT_TO_SLOW_DOWN = 2;

    // [ASSUMPTION] 20m degraded-accuracy threshold -- open-sky GPS is typically 3-8m; the
    // PRD's "urban canyon" framing (Sec 5) implies a materially worse fix, not a marginal one.
    private static final float DEGRADED_ACCURACY_THRESHOLD_METERS = 20f;
    // Debounce: require 2 consecutive readings on the new side before flipping state, per
    // the PRD's flagged (unresolved, Sec 9) GPS-flicker/hysteresis concern.
    private static final int ACCURACY_TRANSITION_CONFIRM_COUNT = 2;

    private final Context context;
    private final LocationManager locationManager;
    private final List<Listener> listeners = new ArrayList<>();

    private Location lastFix;
    private int stationaryFixStreak;
    private boolean usingSlowInterval;

    private boolean hasDegradedState;
    private boolean lastDegraded;
    private int pendingTransitionSide; // -1 = pending "not degraded", 1 = pending "degraded", 0 = none
    private int pendingTransitionCount;

    private final LocationListener platformListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            handleNewFix(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Deprecated by the platform since API 29; no replacement signal needed --
            // accuracy degradation is already detected per-fix via Location.getAccuracy().
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public AndroidLocationProvider(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void start() {
        if (!hasLocationPermission()) {
            notifyPermissionDenied();
            return;
        }
        usingSlowInterval = false;
        stationaryFixStreak = 0;
        lastFix = null;
        // Reset accuracy-hysteresis state too, not just polling state -- otherwise a new
        // listener (fresh MapViewModel on a Map-tab revisit) never learns the current
        // accuracy state when it's unchanged from a stale prior session (Review Findings:
        // AC 3 regression fix). The first reading after any start() should unconditionally
        // establish baseline state for whoever is listening now.
        hasDegradedState = false;
        lastDegraded = false;
        pendingTransitionSide = 0;
        pendingTransitionCount = 0;
        requestUpdates(FAST_INTERVAL_MS);
    }

    @Override
    public void stop() {
        locationManager.removeUpdates(platformListener);
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void handleNewFix(Location location) {
        adaptPollingInterval(location);
        lastFix = location;

        // Iterate a defensive copy -- a listener that adds/removes another listener
        // mid-callback (plausible once Epic 2's NavigationSession also registers) must not
        // throw ConcurrentModificationException.
        for (Listener listener : new ArrayList<>(listeners)) {
            listener.onPositionUpdate(new Position(location.getLatitude(), location.getLongitude(), location.getAccuracy()));
        }
        handleAccuracyTransition(location.getAccuracy() > DEGRADED_ACCURACY_THRESHOLD_METERS);
    }

    private void adaptPollingInterval(Location newFix) {
        if (lastFix == null) {
            return;
        }
        boolean stationary = lastFix.distanceTo(newFix) < STATIONARY_DISTANCE_METERS;
        if (!stationary) {
            stationaryFixStreak = 0;
            if (usingSlowInterval) {
                requestUpdates(FAST_INTERVAL_MS);
            }
            return;
        }
        stationaryFixStreak++;
        if (!usingSlowInterval && stationaryFixStreak >= STATIONARY_FIX_COUNT_TO_SLOW_DOWN) {
            requestUpdates(SLOW_INTERVAL_MS);
        }
    }

    private void requestUpdates(long intervalMs) {
        if (!hasLocationPermission()) {
            return;
        }
        usingSlowInterval = intervalMs == SLOW_INTERVAL_MS;
        locationManager.removeUpdates(platformListener);
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, intervalMs, 0f, platformListener, Looper.getMainLooper());
        } catch (IllegalArgumentException e) {
            // GPS_PROVIDER not present on this device -- degrade gracefully rather than crash.
            notifyPermissionDenied();
        }
    }

    private void handleAccuracyTransition(boolean degradedNow) {
        if (!hasDegradedState) {
            hasDegradedState = true;
            lastDegraded = degradedNow;
            notifyAccuracyChanged(degradedNow);
            return;
        }
        if (degradedNow == lastDegraded) {
            pendingTransitionCount = 0;
            pendingTransitionSide = 0;
            return;
        }
        int side = degradedNow ? 1 : -1;
        if (pendingTransitionSide != side) {
            pendingTransitionSide = side;
            pendingTransitionCount = 1;
        } else {
            pendingTransitionCount++;
        }
        if (pendingTransitionCount >= ACCURACY_TRANSITION_CONFIRM_COUNT) {
            lastDegraded = degradedNow;
            pendingTransitionCount = 0;
            pendingTransitionSide = 0;
            notifyAccuracyChanged(degradedNow);
        }
    }

    /**
     * {@code GPS_PROVIDER} specifically requires ACCESS_FINE_LOCATION -- ACCESS_COARSE_LOCATION
     * alone throws SecurityException on registration. A user who grants only "Approximate"
     * cannot be served via this GPS-only implementation, so that case is treated the same as
     * denied (Review Findings: coarse-only crash fix).
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void notifyPermissionDenied() {
        for (Listener listener : new ArrayList<>(listeners)) {
            listener.onPermissionDenied();
        }
    }

    private void notifyAccuracyChanged(boolean degraded) {
        for (Listener listener : new ArrayList<>(listeners)) {
            listener.onAccuracyChanged(degraded);
        }
    }
}
