package za.ac.wits.campusnavigator.ui.navigation;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.model.Route;
import za.ac.wits.campusnavigator.domain.navigation.NavigationSession;
import za.ac.wits.campusnavigator.domain.result.Result;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;

/**
 * Activity-scoped (AD-12 Dev Notes: NavigationSession lives in an Activity-scoped
 * ViewModel) so a route started from the Building Info Page survives the pop-back-stack
 * to the Map, where it's rendered. Same "resolved design" from the story's Dev Notes.
 *
 * <p>Passively caches the last position seen via {@link LocationProvider.Listener}
 * (mirroring MapViewModel's registration pattern) rather than driving the shared
 * LocationProvider's own start()/stop() lifecycle itself -- that lifecycle stays solely
 * owned by MapFragment (Story 1.2), which stops GPS whenever the Map tab isn't visible
 * (NFR3 battery). This means the cached position is only as fresh as the last fix
 * received while the Map was on screen -- acceptable for computing a route starting
 * point, same as real navigation apps starting from a "last known" fix. If no fix was
 * ever received (cold start, denied permission), {@link #startNavigation} reports that
 * so the caller can show the equivalent no-position messaging.</p>
 *
 * <p>All Room I/O (via ComputeRouteUseCase, reached through NavigationSession) is
 * dispatched off the main thread -- both the initial {@link #startNavigation} call and
 * every subsequent {@link #onPositionUpdate}-triggered recompute.</p>
 */
public final class NavigationViewModel extends ViewModel implements LocationProvider.Listener {

    private final MutableLiveData<Result<Route>> activeRoute = new MutableLiveData<>();
    private final NavigationSession navigationSession;
    private final LocationProvider locationProvider;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Position lastKnownPosition;

    public NavigationViewModel(@NonNull ComputeRouteUseCase computeRouteUseCase, @NonNull LocationProvider locationProvider) {
        this.navigationSession = new NavigationSession(computeRouteUseCase);
        this.locationProvider = locationProvider;
        locationProvider.addListener(this);
    }

    public LiveData<Result<Route>> getActiveRoute() {
        return activeRoute;
    }

    /**
     * @return false if no position is available yet -- the caller should show the
     * equivalent no-position messaging instead of starting navigation.
     */
    public boolean startNavigation(Building destination) {
        if (lastKnownPosition == null) {
            return false;
        }
        Position startPosition = lastKnownPosition;
        executorService.execute(() ->
                navigationSession.start(destination, startPosition, activeRoute::postValue));
        return true;
    }

    @Override
    public void onPositionUpdate(Position position) {
        lastKnownPosition = position;
        executorService.execute(() -> navigationSession.onPositionChanged(position));
    }

    @Override
    public void onAccuracyChanged(boolean degraded) {
        // Not a recompute trigger for this story -- only position deviation is (AD-12's
        // Accessibility-Preference trigger doesn't exist until Epic 3).
    }

    @Override
    public void onPermissionDenied() {
        // No longer have a valid position source -- next startNavigation() call correctly
        // reports "no position available" instead of using a stale, possibly-wrong fix.
        lastKnownPosition = null;
    }

    @Override
    protected void onCleared() {
        locationProvider.removeListener(this);
        executorService.shutdown();
    }
}
