package za.ac.wits.campusnavigator.ui.navigation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.model.Route;
import za.ac.wits.campusnavigator.domain.navigation.NavigationSession;
import za.ac.wits.campusnavigator.domain.result.Result;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.FindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.common.Event;

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
 *
 * <p>Story 3.1: {@link #startNavigation} always re-reads the currently persisted
 * Accessibility Preference via {@link GetAccessibilityPreferenceUseCase} before starting a
 * session -- Room is the single source of truth (AC 1's "persists across app sessions"),
 * not any value cached here. {@link #onAccessibilityPreferenceChanged} is the live wiring
 * for AC 4 (toggled while a route is active): SettingsFragment calls it directly on this
 * same Activity-scoped instance after persisting the new value, the same
 * two-Fragments-one-instance bridge Story 2.2 established for starting navigation itself.</p>
 *
 * <p>{@link #getRouteOriginPosition()} (code review fix, 2026-07-12) is the fixed point a
 * route began from, set once per {@link #startNavigation} call and never overwritten by a
 * later recompute -- deliberately tracked here rather than read off {@code Route}'s own
 * first waypoint, which is the *current* position at computation time and therefore moves
 * on every ~15m deviation-triggered recompute. AC 2's "persistent... label near the
 * route's start" means the point navigation began, not wherever the user currently is.
 * Living here (Activity-scoped) rather than in MapFragment also means it survives
 * MapFragment's view being destroyed/recreated across a Building-Info-Page round trip
 * (Story 2.1's established back-stack-restore gotcha), unlike a MapFragment-local field
 * would.</p>
 *
 * <p>Story 4.2: {@link #startNavigationToNearestCategoryPick} resolves a Category Pick to
 * its nearest Building (real walking distance, AD-7) and, on success, starts navigation to
 * it through this exact same {@code NavigationSession}/{@code ComputeRouteUseCase} path --
 * no separate ad hoc route-rendering logic for Category Picks (ARCHITECTURE-SPINE.md's
 * Common Picks Capability Map row). The result is published via
 * {@link #getCategoryPickResolution()}, wrapped in {@link Event} rather than plain
 * {@code LiveData} -- see that class's Javadoc for why a one-shot resolution result needs
 * different redelivery semantics than {@link #getActiveRoute()}'s persistent state.</p>
 */
public final class NavigationViewModel extends ViewModel implements LocationProvider.Listener {

    private final MutableLiveData<Result<Route>> activeRoute = new MutableLiveData<>();
    private final MutableLiveData<Event<Result<Building>>> categoryPickResolution = new MutableLiveData<>();
    private final NavigationSession navigationSession;
    private final LocationProvider locationProvider;
    private final GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase;
    private final FindNearestCategoryPickUseCase findNearestCategoryPickUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Position lastKnownPosition;
    private String activeDestinationName;
    private Position routeOriginPosition;

    public NavigationViewModel(@NonNull ComputeRouteUseCase computeRouteUseCase, @NonNull LocationProvider locationProvider,
                                @NonNull GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase,
                                @NonNull FindNearestCategoryPickUseCase findNearestCategoryPickUseCase) {
        this.navigationSession = new NavigationSession(computeRouteUseCase);
        this.locationProvider = locationProvider;
        this.getAccessibilityPreferenceUseCase = getAccessibilityPreferenceUseCase;
        this.findNearestCategoryPickUseCase = findNearestCategoryPickUseCase;
        locationProvider.addListener(this);
    }

    public LiveData<Result<Route>> getActiveRoute() {
        return activeRoute;
    }

    /** One-shot Category Pick resolution result -- see {@link Event}'s Javadoc. */
    public LiveData<Event<Result<Building>>> getCategoryPickResolution() {
        return categoryPickResolution;
    }

    /** Destination name of the active/last-started navigation, for accessibility labeling. */
    @Nullable
    public String getActiveDestinationName() {
        return activeDestinationName;
    }

    /**
     * The fixed point the active/last-started navigation began from -- for the
     * "Accessible route" label (AC 2), which must anchor near where the walk started, not
     * wherever a later recompute's current position happens to be.
     */
    @Nullable
    public Position getRouteOriginPosition() {
        return routeOriginPosition;
    }

    /**
     * @return false if no position is available yet -- the caller should show the
     * equivalent no-position messaging instead of starting navigation.
     */
    public boolean startNavigation(Building destination) {
        if (lastKnownPosition == null) {
            return false;
        }
        activeDestinationName = destination.getName();
        routeOriginPosition = lastKnownPosition;
        Position startPosition = lastKnownPosition;
        executorService.execute(() -> {
            boolean avoidStairs = getAccessibilityPreferenceUseCase.execute();
            navigationSession.start(destination, startPosition, avoidStairs, activeRoute::postValue);
        });
        return true;
    }

    /**
     * Resolves {@code category} to its nearest Building by real walking distance (AD-7)
     * and, on success, starts navigation to it -- same {@code false}-if-no-position
     * contract as {@link #startNavigation} (reuses the exact same no-position messaging,
     * no separate message for this path). On success, deliberately re-implements
     * {@code startNavigation}'s "set activeDestinationName/routeOriginPosition, call
     * navigationSession.start(...)" sequence inline rather than calling
     * {@code startNavigation(nearest)} as a second step, to avoid a second
     * {@code getAccessibilityPreferenceUseCase.execute()} Room read and a second
     * (redundant, already-known-true) position check.
     *
     * @return false if no position is available yet.
     */
    public boolean startNavigationToNearestCategoryPick(CategoryTag category) {
        if (lastKnownPosition == null) {
            return false;
        }
        Position startPosition = lastKnownPosition;
        executorService.execute(() -> {
            boolean avoidStairs = getAccessibilityPreferenceUseCase.execute();
            Result<Building> resolution =
                    findNearestCategoryPickUseCase.execute(startPosition, category.getName(), avoidStairs);
            if (resolution instanceof Result.Success) {
                Building nearest = ((Result.Success<Building>) resolution).getValue();
                activeDestinationName = nearest.getName();
                routeOriginPosition = startPosition;
                navigationSession.start(nearest, startPosition, avoidStairs, activeRoute::postValue);
            }
            categoryPickResolution.postValue(new Event<>(resolution));
        });
        return true;
    }

    /**
     * Called by SettingsFragment (via this same Activity-scoped instance) whenever the
     * Accessibility Preference toggle changes, after it's been persisted. AC 4: if a route
     * is currently active, NavigationSession recomputes it immediately.
     */
    public void onAccessibilityPreferenceChanged(boolean avoidStairs) {
        executorService.execute(() -> navigationSession.onAccessibilityPreferenceChanged(avoidStairs));
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
        // A route already in progress can never recompute again without a position source
        // -- clear it rather than leaving a stale line rendered with no path to updating.
        // notFound() (not error()) deliberately: the route isn't "unavailable" (a routing
        // failure), permission was revoked -- a distinct cause with its own existing
        // messaging elsewhere (Story 1.2's permission-rationale prompt), so this silently
        // clears the line rather than showing a misleading "no route available" Snackbar.
        if (activeRoute.getValue() != null) {
            activeRoute.postValue(Result.notFound());
        }
        // Code review fix (2026-07-12): also end the NavigationSession itself, not just
        // clear the rendered state. Without this, NavigationSession kept its destination
        // and lastComputedPosition cached internally -- a later Accessibility Preference
        // toggle (AD-12's other recompute trigger) would still see an "active" session and
        // recompute against that stale, no-longer-current position, silently resurrecting
        // a route the app had just told the user it couldn't show. Dispatched through the
        // same single-thread executor as every other NavigationSession mutation, not
        // called directly here, to preserve the existing no-concurrent-field-access
        // invariant (this callback itself runs on the main thread).
        executorService.execute(navigationSession::stop);
    }

    @Override
    protected void onCleared() {
        locationProvider.removeListener(this);
        executorService.shutdown();
    }
}
