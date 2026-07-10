package za.ac.wits.campusnavigator.domain.location;

import za.ac.wits.campusnavigator.domain.model.Position;

/**
 * Plain-Java listener-registration contract for live GPS position (ARCHITECTURE-SPINE.md
 * AD-11) -- no Android type, no LiveData, keeping :domain zero-Android-dependency per AD-5.
 * :ui implements this wrapping Android's location APIs. Exactly one instance is constructed
 * in the :app composition root (AD-10) and shared -- never re-instantiated per feature.
 */
public interface LocationProvider {

    /**
     * Begins delivering updates to registered listeners. If location permission is not
     * granted, notifies listeners via {@link Listener#onPermissionDenied()} instead of
     * registering for updates.
     */
    void start();

    /**
     * Stops delivering updates. Must be called when the caller no longer needs updates --
     * GPS keeps draining battery otherwise.
     */
    void stop();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {

        void onPositionUpdate(Position position);

        /**
         * Fired only on a degraded/not-degraded state transition, never on every update.
         */
        void onAccuracyChanged(boolean degraded);

        void onPermissionDenied();
    }
}
