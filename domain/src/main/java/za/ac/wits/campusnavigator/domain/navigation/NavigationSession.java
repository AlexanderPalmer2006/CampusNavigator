package za.ac.wits.campusnavigator.domain.navigation;

import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.model.Route;
import za.ac.wits.campusnavigator.domain.result.Result;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;
import za.ac.wits.campusnavigator.navigationengine.Haversine;

/**
 * Holds the active route and recomputes it when the user's position deviates more than
 * ~15m from the position it was last computed against (ARCHITECTURE-SPINE.md AD-12, PRD
 * FR-6's own flagged {@code [ASSUMPTION]}).
 *
 * <p>Deliberately does NOT implement {@code LocationProvider.Listener} itself: the real
 * {@code AndroidLocationProvider} (in :ui) delivers position updates on the main thread,
 * and {@link #onPositionChanged} may perform Room I/O via {@link ComputeRouteUseCase} --
 * the same main-thread-DB-query crash class Story 1.1 already hit once. The owning
 * :ui-scoped ViewModel is responsible for registering as the actual
 * {@code LocationProvider.Listener}, dispatching to a background thread, and calling
 * {@link #onPositionChanged} from there.</p>
 *
 * <p>Accessibility-Preference-triggered recompute (AD-12's other trigger) is not
 * implemented here -- that Setting doesn't exist until Epic 3. Not stubbed out
 * speculatively; nothing here precludes adding it later.</p>
 *
 * <p>Owned by an Activity-scoped ViewModel in :ui (Story 2.2 Dev Notes) -- this class
 * itself has no Android/ViewModel dependency, per AD-5.</p>
 */
public final class NavigationSession {

    private static final double RECOMPUTE_THRESHOLD_METERS = 15.0;

    /** Callback for route-state updates -- the :ui-scoped ViewModel implements this. */
    public interface Listener {
        void onRouteUpdated(Result<Route> result);
    }

    private final ComputeRouteUseCase computeRouteUseCase;

    private Building destination;
    private Position lastComputedPosition;
    private Listener listener;

    public NavigationSession(ComputeRouteUseCase computeRouteUseCase) {
        this.computeRouteUseCase = computeRouteUseCase;
    }

    /** Starts (or restarts, for a new destination) an active navigation session. */
    public void start(Building destination, Position currentPosition, Listener listener) {
        this.destination = destination;
        this.listener = listener;
        computeAndNotify(currentPosition);
    }

    /** Ends the session. The caller is responsible for deregistering from LocationProvider. */
    public void stop() {
        destination = null;
        lastComputedPosition = null;
        listener = null;
    }

    /**
     * Call with each new position while a session is active. May perform Room I/O --
     * callers MUST invoke this off the main thread.
     */
    public void onPositionChanged(Position position) {
        if (destination == null) {
            return;
        }
        if (lastComputedPosition == null) {
            computeAndNotify(position);
            return;
        }
        double deviation = Haversine.distanceMeters(
                lastComputedPosition.getLatitude(), lastComputedPosition.getLongitude(),
                position.getLatitude(), position.getLongitude());
        if (deviation > RECOMPUTE_THRESHOLD_METERS) {
            computeAndNotify(position);
        }
    }

    private void computeAndNotify(Position position) {
        lastComputedPosition = position;
        Result<Route> result = computeRouteUseCase.execute(position, destination);
        if (listener != null) {
            listener.onRouteUpdated(result);
        }
    }
}
