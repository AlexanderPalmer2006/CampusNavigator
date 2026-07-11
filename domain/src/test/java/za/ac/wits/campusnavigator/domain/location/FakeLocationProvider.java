package za.ac.wits.campusnavigator.domain.location;

import java.util.ArrayList;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Position;

/**
 * Minimal fake, shared across :domain tests -- no real GPS, just list-based fan-out to
 * registered listeners. Extracted from {@code LocationProviderContractTest} (Story 1.2)
 * for reuse by Story 2.2's {@code NavigationSessionTest}, mirroring
 * {@code FakeBuildingRepository}'s extraction in Story 2.1.
 */
public final class FakeLocationProvider implements LocationProvider {

    private final List<Listener> listeners = new ArrayList<>();
    private boolean started;

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void emitPosition(Position position) {
        for (Listener listener : listeners) {
            listener.onPositionUpdate(position);
        }
    }

    public void emitAccuracyChanged(boolean degraded) {
        for (Listener listener : listeners) {
            listener.onAccuracyChanged(degraded);
        }
    }

    public void emitPermissionDenied() {
        for (Listener listener : listeners) {
            listener.onPermissionDenied();
        }
    }

    public boolean isStarted() {
        return started;
    }
}
