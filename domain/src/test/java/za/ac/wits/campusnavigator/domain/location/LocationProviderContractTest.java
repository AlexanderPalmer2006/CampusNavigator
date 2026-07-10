package za.ac.wits.campusnavigator.domain.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Position;

/**
 * Verifies the {@link LocationProvider} listener-registration contract against a plain
 * in-memory fake -- the real implementation ({@code AndroidLocationProvider} in :ui) wraps
 * Android's location APIs and is verified on-device instead (see story Dev Notes: Testing
 * Standards). This fake also gives future :domain consumers (e.g. Epic 2's NavigationSession)
 * a ready test double, mirroring FakeBuildingRepository's role for GetBuildingsUseCaseTest.
 */
public class LocationProviderContractTest {

    /** Minimal fake: no real GPS, just list-based fan-out to registered listeners. */
    static final class FakeLocationProvider implements LocationProvider {
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

        void emitPosition(Position position) {
            for (Listener listener : listeners) {
                listener.onPositionUpdate(position);
            }
        }

        void emitAccuracyChanged(boolean degraded) {
            for (Listener listener : listeners) {
                listener.onAccuracyChanged(degraded);
            }
        }

        void emitPermissionDenied() {
            for (Listener listener : listeners) {
                listener.onPermissionDenied();
            }
        }

        boolean isStarted() {
            return started;
        }
    }

    private static final class RecordingListener implements LocationProvider.Listener {
        Position lastPosition;
        Boolean lastDegraded;
        boolean permissionDenied;
        int positionUpdateCount;

        @Override
        public void onPositionUpdate(Position position) {
            lastPosition = position;
            positionUpdateCount++;
        }

        @Override
        public void onAccuracyChanged(boolean degraded) {
            lastDegraded = degraded;
        }

        @Override
        public void onPermissionDenied() {
            permissionDenied = true;
        }
    }

    @Test
    public void addedListener_receivesPositionUpdates() {
        FakeLocationProvider provider = new FakeLocationProvider();
        RecordingListener listener = new RecordingListener();
        provider.addListener(listener);

        provider.emitPosition(new Position(-26.19, 28.03, 8.0f));

        assertEquals(1, listener.positionUpdateCount);
        assertEquals(-26.19, listener.lastPosition.getLatitude(), 0.0);
    }

    @Test
    public void removedListener_receivesNoFurtherUpdates() {
        FakeLocationProvider provider = new FakeLocationProvider();
        RecordingListener listener = new RecordingListener();
        provider.addListener(listener);
        provider.removeListener(listener);

        provider.emitPosition(new Position(-26.19, 28.03, 8.0f));

        assertEquals(0, listener.positionUpdateCount);
    }

    @Test
    public void multipleListeners_allReceiveTheSameUpdate() {
        FakeLocationProvider provider = new FakeLocationProvider();
        RecordingListener first = new RecordingListener();
        RecordingListener second = new RecordingListener();
        provider.addListener(first);
        provider.addListener(second);

        provider.emitPosition(new Position(-26.19, 28.03, 8.0f));

        assertEquals(1, first.positionUpdateCount);
        assertEquals(1, second.positionUpdateCount);
    }

    @Test
    public void accuracyChanged_deliversDegradedState() {
        FakeLocationProvider provider = new FakeLocationProvider();
        RecordingListener listener = new RecordingListener();
        provider.addListener(listener);

        provider.emitAccuracyChanged(true);

        assertTrue(listener.lastDegraded);
    }

    @Test
    public void permissionDenied_isDeliveredToListener() {
        FakeLocationProvider provider = new FakeLocationProvider();
        RecordingListener listener = new RecordingListener();
        provider.addListener(listener);

        provider.emitPermissionDenied();

        assertTrue(listener.permissionDenied);
    }

    @Test
    public void start_and_stop_trackStartedState() {
        FakeLocationProvider provider = new FakeLocationProvider();

        provider.start();
        assertTrue(provider.isStarted());

        provider.stop();
        assertFalse(provider.isStarted());
    }
}
