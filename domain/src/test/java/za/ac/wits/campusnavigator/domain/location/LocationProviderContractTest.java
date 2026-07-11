package za.ac.wits.campusnavigator.domain.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Position;

/**
 * Verifies the {@link LocationProvider} listener-registration contract against
 * {@link FakeLocationProvider} -- the real implementation ({@code AndroidLocationProvider}
 * in :ui) wraps Android's location APIs and is verified on-device instead (see story Dev
 * Notes: Testing Standards).
 */
public class LocationProviderContractTest {

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
