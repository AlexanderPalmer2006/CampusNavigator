package za.ac.wits.campusnavigator.domain.navigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Edge;
import za.ac.wits.campusnavigator.domain.model.Node;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.model.Route;
import za.ac.wits.campusnavigator.domain.repository.FakeRoutingRepository;
import za.ac.wits.campusnavigator.domain.result.Result;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;

/**
 * Exercises the ~15m recompute-on-deviation logic (AD-12, PRD FR-6). NavigationSession no
 * longer registers with a LocationProvider itself (see its class javadoc for why -- the
 * owning :ui ViewModel dispatches position updates to it off the main thread instead), so
 * these tests call {@link NavigationSession#onPositionChanged} directly, simulating that
 * dispatch.
 */
public class NavigationSessionTest {

    private static final double BASE_LAT = -26.1908;
    private static final double BASE_LON = 28.0261;

    private static final class RecordingListener implements NavigationSession.Listener {
        int updateCount;
        Result<Route> lastResult;

        @Override
        public void onRouteUpdated(Result<Route> result) {
            updateCount++;
            lastResult = result;
        }
    }

    private ComputeRouteUseCase newComputeRouteUseCase() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1L, BASE_LAT, BASE_LON, "wits-main", "OUTDOOR"));
        nodes.add(new Node(2L, -26.1912, 28.0298, "wits-main", "OUTDOOR"));
        List<Edge> edges = Collections.singletonList(new Edge(1L, 1L, 2L, 371.8, false));
        return new ComputeRouteUseCase(new FakeRoutingRepository(nodes, edges));
    }

    private Building destination() {
        return new Building(2L, "Robert Sobukwe Block", -26.1912, 28.0298, "wits-main", "RSB", null);
    }

    /** A direct stair edge plus a step-free detour, so toggling avoidStairs changes the result. */
    private ComputeRouteUseCase newComputeRouteUseCaseWithStairEdge() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1L, BASE_LAT, BASE_LON, "wits-main", "OUTDOOR"));
        nodes.add(new Node(2L, -26.1912, 28.0298, "wits-main", "OUTDOOR"));
        nodes.add(new Node(3L, -26.1910, 28.0280, "wits-main", "OUTDOOR"));
        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge(1L, 1L, 2L, 50.0, true));
        edges.add(new Edge(2L, 1L, 3L, 100.0, false));
        edges.add(new Edge(3L, 3L, 2L, 100.0, false));
        return new ComputeRouteUseCase(new FakeRoutingRepository(nodes, edges));
    }

    @Test
    public void start_computesRouteImmediately() {
        NavigationSession session = new NavigationSession(newComputeRouteUseCase());
        RecordingListener listener = new RecordingListener();

        session.start(destination(), new Position(BASE_LAT, BASE_LON, 8.0f), false, listener);

        assertEquals(1, listener.updateCount);
        assertTrue(listener.lastResult instanceof Result.Success);
    }

    @Test
    public void positionUpdateWithinThreshold_doesNotRecompute() {
        NavigationSession session = new NavigationSession(newComputeRouteUseCase());
        RecordingListener listener = new RecordingListener();
        session.start(destination(), new Position(BASE_LAT, BASE_LON, 8.0f), false, listener);

        // ~5.6m away -- under the 15m threshold.
        session.onPositionChanged(new Position(BASE_LAT + 0.00005, BASE_LON, 8.0f));

        assertEquals("No recompute for a sub-threshold deviation", 1, listener.updateCount);
    }

    @Test
    public void positionUpdateBeyondThreshold_recomputes() {
        NavigationSession session = new NavigationSession(newComputeRouteUseCase());
        RecordingListener listener = new RecordingListener();
        session.start(destination(), new Position(BASE_LAT, BASE_LON, 8.0f), false, listener);

        // ~33.4m away -- over the 15m threshold.
        session.onPositionChanged(new Position(BASE_LAT + 0.0003, BASE_LON, 8.0f));

        assertEquals("Must recompute once the deviation exceeds ~15m", 2, listener.updateCount);
    }

    @Test
    public void stop_stopsReceivingFurtherUpdates() {
        NavigationSession session = new NavigationSession(newComputeRouteUseCase());
        RecordingListener listener = new RecordingListener();
        session.start(destination(), new Position(BASE_LAT, BASE_LON, 8.0f), false, listener);

        session.stop();
        session.onPositionChanged(new Position(BASE_LAT + 0.0003, BASE_LON, 8.0f));

        assertEquals("stop() must ignore updates once stopped", 1, listener.updateCount);
    }

    @Test
    public void accessibilityPreferenceToggled_whileRouteActive_recomputesImmediately() {
        NavigationSession session = new NavigationSession(newComputeRouteUseCaseWithStairEdge());
        RecordingListener listener = new RecordingListener();
        session.start(destination(), new Position(BASE_LAT, BASE_LON, 8.0f), false, listener);
        assertEquals("Precondition: initial route uses the direct (stair) edge",
                4, ((Result.Success<Route>) listener.lastResult).getValue().getWaypoints().size());

        session.onAccessibilityPreferenceChanged(true);

        assertEquals("Toggling with an active route must recompute immediately, no new position needed",
                2, listener.updateCount);
        assertTrue(listener.lastResult instanceof Result.Success);
        Route recomputed = ((Result.Success<Route>) listener.lastResult).getValue();
        assertTrue("Must be marked accessible after toggling on", recomputed.isAccessible());
        assertEquals("Must detour around the stair edge", 5, recomputed.getWaypoints().size());
    }

    @Test
    public void accessibilityPreferenceToggled_withNoActiveDestination_doesNotNotify() {
        NavigationSession session = new NavigationSession(newComputeRouteUseCaseWithStairEdge());
        RecordingListener listener = new RecordingListener();
        // Never started -- no destination active.

        session.onAccessibilityPreferenceChanged(true);

        assertEquals("No active route -- nothing to recompute or notify", 0, listener.updateCount);
    }

    @Test
    public void accessibilityPreference_carriesIntoSubsequentDeviationRecompute() {
        NavigationSession session = new NavigationSession(newComputeRouteUseCaseWithStairEdge());
        RecordingListener listener = new RecordingListener();
        session.start(destination(), new Position(BASE_LAT, BASE_LON, 8.0f), false, listener);

        session.onAccessibilityPreferenceChanged(true); // update #2, accessible detour
        // ~33.4m away -- over the 15m threshold, triggers a third recompute.
        session.onPositionChanged(new Position(BASE_LAT + 0.0003, BASE_LON, 8.0f));

        assertEquals("Toggle-triggered state must carry into the next deviation-triggered recompute",
                3, listener.updateCount);
        Route thirdRoute = ((Result.Success<Route>) listener.lastResult).getValue();
        assertTrue("The deviation-triggered recompute must still honor the earlier toggle",
                thirdRoute.isAccessible());
    }
}
