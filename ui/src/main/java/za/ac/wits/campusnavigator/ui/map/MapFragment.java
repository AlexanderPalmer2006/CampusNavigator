package za.ac.wits.campusnavigator.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.model.Route;
import za.ac.wits.campusnavigator.domain.result.Result;
import za.ac.wits.campusnavigator.domain.search.BuildingSearchResult;
import za.ac.wits.campusnavigator.ui.R;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModel;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModelFactory;
import za.ac.wits.campusnavigator.ui.search.BuildingSearchAdapter;

/**
 * The Map (home) surface -- FR1, FR2, FR3, FR4. Renders a fully offline map with Building
 * labels, the live "you are here" marker, and Building Search (Story 1.1 AC 1, Story 1.2,
 * Story 2.1) visible at default zoom.
 *
 * All seven MapView lifecycle forwards are mandatory in Java -- skipping onStop/onDestroy
 * leaks the native renderer (see Story 1.1 Dev Notes / web research).
 *
 * <p>Building labels, the location marker, and the Walking Route (Story 2.2 Task 6) are
 * all drawn as native {@link View}s overlaid on the MapView and repositioned on every
 * camera move, rather than as MapLibre map layers. A SymbolLayer's text-field requires a
 * "glyphs" font-PBF source this offline-only style doesn't have -- with no glyphs source,
 * SymbolLayer text renders nothing, silently, only caught by an actual on-device run
 * (Story 1.1). The Walking Route hit the same class of silent failure with a plain
 * LineLayer/GeoJsonSource -- confirmed via a deliberately oversized, high-contrast
 * on-device test (not assumed) before falling back to the same native-overlay approach,
 * consistent with why the location marker already uses it too: proven reliability against
 * this app's minimal offline style.json, plus genuine TalkBack semantics "for free" for the
 * marker (Story 1.2 Dev Notes: Marker Rendering Approach) -- MapLibre's built-in
 * LocationComponent was evaluated and not used.</p>
 */
public final class MapFragment extends Fragment {

    private static final int DEFAULT_ZOOM = 16;
    private static final int CAMERA_BOUNDS_PADDING_PX = 96;
    private static final float TOUCH_TARGET_DP = 48f;

    private MapView mapView;
    private FrameLayout labelOverlay;
    private MapLibreMap map;
    private boolean cameraPositioned;
    private final List<LabelView> labelViews = new ArrayList<>();

    private LocationProvider locationProvider;
    private HasBuildingNavigation buildingNavigator;
    private MapViewModel viewModel;
    private NavigationViewModel navigationViewModel;
    private LocationMarkerView locationMarkerView;
    private RouteLineView routeLineView;
    private TextView accessibleRouteLabelView;
    private LatLng lastMarkerPosition;
    private List<LatLng> currentRouteWaypoints = new ArrayList<>();
    private boolean currentRouteIsAccessible;
    private Boolean lastAnnouncedDegraded;
    private Result.ErrorType lastRouteErrorType;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    private EditText searchBar;
    private ListView searchSuggestionsList;
    private BuildingSearchAdapter searchAdapter;

    private static final class LabelView {
        final LatLng position;
        final TextView view;

        LabelView(LatLng position, TextView view) {
            this.position = position;
            this.view = view;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Must be registered before the Fragment reaches STARTED -- onCreate is the
        // standard place for this (Story 1.2 Task 4).
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), this::onLocationPermissionResult);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Reset per-view state explicitly: when this Fragment is restored via the back
        // stack (Story 2.1's Building Info Page -> Back), FragmentManager reuses this SAME
        // Fragment object -- it does not construct a new MapFragment -- so instance fields
        // like cameraPositioned survive from the previous view unless cleared here. Without
        // this reset, the brand-new MapView/MapLibreMap for this view creation never gets
        // its camera positioned (the one-shot flag is already "spent"), and every building
        // label projects onto MapLibre's un-positioned default camera, collapsing onto the
        // same pixel. Found via on-device back-navigation testing, not caught by any
        // "fresh MapFragment per tab visit" assumption from Stories 1.1/1.2, which held for
        // plain tab switches (always `new MapFragment()`) but not for back-stack restores.
        cameraPositioned = false;
        lastMarkerPosition = null;
        currentRouteWaypoints = new ArrayList<>();
        currentRouteIsAccessible = false;
        lastAnnouncedDegraded = null;
        lastRouteErrorType = null;

        mapView = view.findViewById(R.id.mapView);
        labelOverlay = view.findViewById(R.id.buildingLabelOverlay);
        mapView.onCreate(savedInstanceState);
        mapView.setContentDescription(getString(R.string.map_attribution));

        HasGetBuildingsUseCase buildingsProvider = (HasGetBuildingsUseCase) requireActivity();
        HasLocationProvider locationProviderHost = (HasLocationProvider) requireActivity();
        HasSearchBuildingsUseCase searchProvider = (HasSearchBuildingsUseCase) requireActivity();
        HasComputeRouteUseCase computeRouteProvider = (HasComputeRouteUseCase) requireActivity();
        HasGetAccessibilityPreferenceUseCase accessibilityPreferenceProvider =
                (HasGetAccessibilityPreferenceUseCase) requireActivity();
        HasFindNearestCategoryPickUseCase categoryPickProvider = (HasFindNearestCategoryPickUseCase) requireActivity();
        buildingNavigator = (HasBuildingNavigation) requireActivity();
        locationProvider = locationProviderHost.getLocationProvider();

        MapViewModelFactory factory = new MapViewModelFactory(buildingsProvider.getGetBuildingsUseCase(),
                locationProvider, searchProvider.getSearchBuildingsUseCase());
        viewModel = new ViewModelProvider(this, factory).get(MapViewModel.class);

        // Activity-scoped -- the same instance a route may already have been started on
        // from BuildingInfoFragment (Task 5); observing it here just picks up whatever
        // state it's already in (Story 2.2 Dev Notes: "Resolved Design: NavigationViewModel").
        NavigationViewModelFactory navigationFactory =
                new NavigationViewModelFactory(computeRouteProvider.getComputeRouteUseCase(), locationProvider,
                        accessibilityPreferenceProvider.getGetAccessibilityPreferenceUseCase(),
                        categoryPickProvider.getFindNearestCategoryPickUseCase());
        navigationViewModel = new ViewModelProvider(requireActivity(), navigationFactory).get(NavigationViewModel.class);

        // Added before the location marker so the route line renders underneath it (and
        // underneath the building labels added in renderBuildingLabels) -- match_parent
        // since it draws an arbitrary path across the whole overlay, not a fixed small box.
        routeLineView = createRouteLineView();
        labelOverlay.addView(routeLineView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // AC 2: persistent "Accessible route" label near the route's start, native View
        // overlay same as the route line itself (MapLibre SymbolLayer text confirmed twice
        // not to render against this app's offline style.json -- Story 1.1, Story 2.2).
        accessibleRouteLabelView = createAccessibleRouteLabelView();
        labelOverlay.addView(accessibleRouteLabelView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        accessibleRouteLabelView.setVisibility(View.INVISIBLE);

        locationMarkerView = createLocationMarkerView();
        int touchTargetPx = (int) dpToPx(TOUCH_TARGET_DP);
        labelOverlay.addView(locationMarkerView, new FrameLayout.LayoutParams(touchTargetPx, touchTargetPx));
        // Hidden until a real position places it -- otherwise it flashes at (0,0), same
        // reasoning as the building-label flash fix in Story 1.1's review.
        locationMarkerView.setVisibility(View.INVISIBLE);

        setUpSearch(view);

        mapView.getMapAsync(mapLibreMap -> {
            if (getView() == null) {
                // Fragment view was destroyed (e.g. fast tab switch) before MapLibre's
                // async init finished -- getViewLifecycleOwner() below would throw.
                return;
            }
            map = mapLibreMap;
            map.addOnCameraIdleListener(this::repositionLabels);
            map.addOnCameraMoveListener(this::repositionLabels);
            map.setStyle(new Style.Builder().fromUri(currentMapStyleUri()), style -> {
                if (getView() == null) {
                    return;
                }
                viewModel.getBuildings().observe(getViewLifecycleOwner(), this::renderBuildingLabels);
                viewModel.getLocationState().observe(getViewLifecycleOwner(), this::renderLocationState);
                navigationViewModel.getActiveRoute().observe(getViewLifecycleOwner(), this::renderRoute);
            });
        });
    }

    /**
     * Story 5.2: the offline base style's background layer is a MapLibre-rendered
     * canvas, not an Android {@code View} -- it does not participate in
     * {@code Theme.Material3.DayNight}'s resource-qualifier resolution the rest of this
     * app's UI gets "for free." Without this, toggling Dark Mode would leave the Map
     * tab's background hardcoded light (`style.json`'s `#FAFAF8`) while every other
     * screen goes dark, violating AC 2 ("...across every screen") on the single most
     * visible surface in the app. Reads the currently *resolved* night mode from
     * {@code Configuration} (the same source {@code values-night/} resource qualifiers
     * themselves resolve against) rather than re-reading the persisted Setting directly --
     * this Fragment is always freshly recreated after a Dark Mode toggle (that toggle
     * calls {@code AppCompatDelegate.setDefaultNightMode()}, which recreates the hosting
     * Activity), so the current Configuration is already authoritative at the point this
     * runs; no separate Room read or live-update path is needed here.
     */
    private String currentMapStyleUri() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        // Anything other than the explicit YES flag (including UI_MODE_NIGHT_UNDEFINED,
        // on devices/OS versions that never populate it) falls through to the light
        // style -- intentional, not a side effect of the ternary: light is this app's
        // own explicit default (EXPERIENCE.md: "not default-on"), so an unresolved/
        // undefined signal should never be treated as an implicit request for dark.
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
                ? "asset://style_dark.json"
                : "asset://style.json";
    }

    private void setUpSearch(@NonNull View view) {
        searchBar = view.findViewById(R.id.searchBar);
        searchSuggestionsList = view.findViewById(R.id.searchSuggestions);

        searchAdapter = new BuildingSearchAdapter(requireContext(), result -> {
            searchSuggestionsList.setVisibility(View.GONE);
            buildingNavigator.showBuildingInfo(result.getBuilding().getId());
        });
        searchSuggestionsList.setAdapter(searchAdapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No debounce -- the seed dataset is tiny (5 buildings, local/offline),
                // computing fuzzy match on every keystroke is cheap (Story 2.1 Task 4).
                viewModel.search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), this::renderSearchResults);
    }

    private void renderSearchResults(@Nullable List<BuildingSearchResult> results) {
        searchAdapter.submitResults(results);
        boolean hasQuery = searchBar.getText() != null && searchBar.getText().length() > 0;
        boolean hasResults = results != null && !results.isEmpty();
        searchSuggestionsList.setVisibility(hasQuery && hasResults ? View.VISIBLE : View.GONE);
    }

    private LocationMarkerView createLocationMarkerView() {
        int accentColor = getResources().getColor(R.color.accent, requireContext().getTheme());
        int outlineColor = getResources().getColor(R.color.surface_raised, requireContext().getTheme());
        LocationMarkerView markerView = new LocationMarkerView(requireContext(), accentColor, outlineColor);
        markerView.setContentDescription(getString(R.string.location_marker_description));
        return markerView;
    }

    private RouteLineView createRouteLineView() {
        int accentColor = getResources().getColor(R.color.accent, requireContext().getTheme());
        int outlineColor = getResources().getColor(R.color.surface_raised, requireContext().getTheme());
        return new RouteLineView(requireContext(), accentColor, outlineColor);
    }

    /**
     * DESIGN.md walking-route component: "Accessible Routing active -> persistent
     * 'Accessible route' text/icon label near the route's start." Reuses existing tokens
     * (surface_raised pill via bg_accessible_route_label, ink_primary text) rather than
     * inventing new colors -- same convention as the Start Navigation button (Story 2.2).
     */
    private TextView createAccessibleRouteLabelView() {
        TextView label = new TextView(requireContext());
        label.setText(R.string.route_accessible_label);
        label.setTextSize(12f);
        label.setTextColor(getResources().getColor(R.color.ink_primary, requireContext().getTheme()));
        label.setBackgroundResource(R.drawable.bg_accessible_route_label);
        int paddingH = (int) dpToPx(8f);
        int paddingV = (int) dpToPx(4f);
        label.setPadding(paddingH, paddingV, paddingH, paddingV);
        // Not independently tappable (no AC/Interaction Primitive calls for it), but still
        // a real informational element TalkBack must announce (Accessibility Floor) --
        // explicit IMPORTANT_FOR_ACCESSIBILITY_YES (code review fix, 2026-07-12: matches
        // RouteLineView's own Story 2.2 review-fixed posture exactly, rather than relying
        // on a plain TextView's default accessibility-tree participation).
        label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        label.setClickable(false);
        return label;
    }

    private void renderBuildingLabels(@Nullable List<Building> buildings) {
        if (buildings == null || buildings.isEmpty() || map == null) {
            return;
        }

        labelOverlay.removeAllViews();
        // Route line first so it renders underneath the location marker and the building
        // labels added below it (removeAllViews() wipes every child, so all three need
        // re-adding here every time this method runs, not just on the first call).
        labelOverlay.addView(routeLineView);
        labelOverlay.addView(accessibleRouteLabelView);
        labelOverlay.addView(locationMarkerView);
        labelViews.clear();
        int touchTargetPx = (int) dpToPx(TOUCH_TARGET_DP);
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (Building building : buildings) {
            LatLng position = new LatLng(building.getLatitude(), building.getLongitude());
            boundsBuilder.include(position);

            TextView label = new TextView(requireContext());
            label.setText(building.getName());
            label.setTextSize(12f);
            label.setTextColor(getResources().getColor(R.color.ink_primary, requireContext().getTheme()));
            // AC 4 (Story 2.1): map-pin tap also opens the Building Info Page. Tap target
            // stretched to 48dp minimum in both dimensions (DESIGN.md: "never smaller than
            // 48dp regardless of visual size") via minHeight/minWidth + vertical centering
            // -- the visible text stays its normal size, only the tappable footprint grows,
            // same "small visible mark, larger touch box" idea as the location marker
            // (Story 1.2).
            label.setMinHeight(touchTargetPx);
            label.setMinWidth(touchTargetPx);
            label.setGravity(Gravity.CENTER);
            label.setPadding((int) dpToPx(4f), 0, (int) dpToPx(4f), 0);
            label.setContentDescription(building.getName());
            label.setOnClickListener(v -> buildingNavigator.showBuildingInfo(building.getId()));
            // Hidden until the first repositionLabels() call places it correctly --
            // otherwise it flashes at the FrameLayout's default (0,0) for one frame.
            label.setVisibility(View.INVISIBLE);
            labelOverlay.addView(label, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            labelViews.add(new LabelView(position, label));
        }

        // Without this, the map's default camera (0,0 / zoom 0) never shows the Wits
        // buildings the labels above are drawn at -- caught only by an on-device run.
        // GPS position (renderLocationState) takes priority when it arrives first --
        // both paths share the cameraPositioned one-shot flag (Story 1.2 Task 6).
        if (!cameraPositioned) {
            cameraPositioned = true;
            if (buildings.size() == 1) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        labelViews.get(0).position, DEFAULT_ZOOM));
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(), CAMERA_BOUNDS_PADDING_PX));
            }
        }
        repositionLabels();
    }

    private void renderLocationState(@Nullable LocationUiState state) {
        if (state == null || map == null) {
            return;
        }

        Position position = state.getPosition();
        if (state.isPermissionDenied() || position == null) {
            locationMarkerView.setVisibility(View.INVISIBLE);
            lastMarkerPosition = null;
            // Also clear the announcement-debounce memory -- otherwise a deny-then-regrant
            // cycle while accuracy was already degraded silently suppresses the next
            // genuinely-new "Location accuracy reduced" announcement (Review Findings).
            lastAnnouncedDegraded = null;
            return;
        }

        lastMarkerPosition = new LatLng(position.getLatitude(), position.getLongitude());
        locationMarkerView.setAccuracyDegraded(state.isAccuracyDegraded());
        locationMarkerView.setContentDescription(getString(state.isAccuracyDegraded()
                ? R.string.location_marker_description_degraded
                : R.string.location_marker_description));

        // TalkBack announces only on the false-to-true transition, never on every
        // subsequent degraded reading (Story 1.2 Task 2's debounce note; AC 3).
        // announceForAccessibility() is deprecated (found via -Xlint:deprecation during
        // this story's build) but deliberately kept: AC 3 / EXPERIENCE.md require the
        // exact one-off phrase "Location accuracy reduced," independent of the marker's
        // resting contentDescription -- there is no non-deprecated API for an arbitrary
        // transient announcement not tied to a focused node's own text.
        boolean degraded = state.isAccuracyDegraded();
        if (degraded && !Boolean.TRUE.equals(lastAnnouncedDegraded)) {
            locationMarkerView.announceForAccessibility(getString(R.string.location_accuracy_reduced));
        }
        lastAnnouncedDegraded = degraded;

        // GPS position takes priority over the building-bounds fallback above when both
        // are available -- whichever arrives first consumes the one-shot flag (Task 6).
        if (!cameraPositioned) {
            cameraPositioned = true;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastMarkerPosition, DEFAULT_ZOOM));
        }
        repositionLocationMarker();
    }

    /**
     * Observes the Activity-scoped NavigationViewModel (Task 5/6) -- null means no
     * navigation has been started yet (nothing to draw); {@code Result.Success} draws the
     * line (plus the persistent "Accessible route" label per AC 2 when {@code
     * route.isAccessible()}); {@code Result.Error} clears any existing line and shows the
     * exact honest-failure microcopy for the specific error (EXPERIENCE.md Voice and Tone)
     * -- {@code NO_ROUTE_AVAILABLE} vs {@code NO_ACCESSIBLE_ROUTE} (AC 3) are user-facing
     * distinct, so the debounce below is keyed on the actual {@link Result.ErrorType}, not
     * a plain boolean -- otherwise a transition between the two error reasons while an
     * error was already showing would be silently swallowed. {@code Result.NotFound} also
     * clears the line silently (no Snackbar) -- used when the position source itself goes
     * away (permission revoked) rather than a routing failure.
     */
    private void renderRoute(@Nullable Result<Route> result) {
        if (result instanceof Result.Success) {
            Route route = ((Result.Success<Route>) result).getValue();
            List<LatLng> waypoints = new ArrayList<>(route.getWaypoints().size());
            for (Position waypoint : route.getWaypoints()) {
                waypoints.add(new LatLng(waypoint.getLatitude(), waypoint.getLongitude()));
            }
            currentRouteWaypoints = waypoints;
            currentRouteIsAccessible = route.isAccessible();
            lastRouteErrorType = null;
            String destinationName = navigationViewModel.getActiveDestinationName();
            routeLineView.setContentDescription(destinationName == null
                    ? null : getString(R.string.navigation_route_description, destinationName));
            repositionRouteLine();
            return;
        }

        // No route to draw either way: Error(NO_ROUTE_AVAILABLE/NO_ACCESSIBLE_ROUTE),
        // NotFound (permission revoked), or no navigation started yet (result == null) --
        // AC 2 requires no broken/empty line ever rendered.
        currentRouteWaypoints = new ArrayList<>();
        currentRouteIsAccessible = false;
        routeLineView.setContentDescription(null);
        repositionRouteLine();

        if (result instanceof Result.Error) {
            Result.ErrorType errorType = ((Result.Error<Route>) result).getErrorType();
            if (errorType != lastRouteErrorType) {
                int messageRes = errorType == Result.ErrorType.NO_ACCESSIBLE_ROUTE
                        ? R.string.navigation_no_accessible_route
                        : R.string.navigation_no_route_available;
                View root = getView();
                if (root != null) {
                    Snackbar.make(root, messageRes, Snackbar.LENGTH_LONG).show();
                    root.announceForAccessibility(getString(messageRes));
                }
            }
            lastRouteErrorType = errorType;
        } else {
            lastRouteErrorType = null;
        }
    }

    /**
     * Called on every camera move/idle so labels and the location marker track their
     * real-world position as the user pans/zooms (AC 2) without lagging behind mid-gesture,
     * and once right after the initial moveCamera above.
     */
    private void repositionLabels() {
        if (map == null) {
            return;
        }
        for (LabelView labelView : labelViews) {
            PointF screenPoint = map.getProjection().toScreenLocation(labelView.position);
            labelView.view.setX(screenPoint.x);
            labelView.view.setY(screenPoint.y);
            labelView.view.setVisibility(View.VISIBLE);
        }
        repositionLocationMarker();
        repositionRouteLine();
    }

    /**
     * Re-projects the active route's waypoints to screen space on every camera move, and
     * repositions the "Accessible route" label (AC 2) alongside it.
     *
     * <p>Code review fix (2026-07-12): the label anchors to {@code
     * navigationViewModel.getRouteOriginPosition()} -- the fixed point navigation began
     * from -- not to {@code currentRouteWaypoints.get(0)}. The route's own first waypoint
     * is the *current* position at computation time (see {@code ComputeRouteUseCase}), so
     * it moves on every ~15m deviation-triggered recompute; anchoring the label there made
     * it visibly re-jump toward the user's live location on every recompute instead of
     * staying "near the route's start" as AC 2 and DESIGN.md's walking-route component
     * both specify. Reading through the Activity-scoped ViewModel (rather than caching the
     * origin locally in this Fragment) also means the correct origin survives this
     * Fragment's view being destroyed and recreated across a Building-Info-Page round trip
     * (Story 2.1's established back-stack-restore risk class), instead of an approximation
     * reset on every {@code onViewCreated()}.</p>
     */
    private void repositionRouteLine() {
        if (map == null || routeLineView == null) {
            return;
        }
        if (currentRouteWaypoints.isEmpty()) {
            routeLineView.setPoints(Collections.emptyList());
            accessibleRouteLabelView.setVisibility(View.INVISIBLE);
            return;
        }
        List<PointF> screenPoints = new ArrayList<>(currentRouteWaypoints.size());
        for (LatLng waypoint : currentRouteWaypoints) {
            screenPoints.add(map.getProjection().toScreenLocation(waypoint));
        }
        routeLineView.setPoints(screenPoints);

        Position origin = currentRouteIsAccessible ? navigationViewModel.getRouteOriginPosition() : null;
        if (origin != null) {
            PointF originScreenPoint = map.getProjection().toScreenLocation(
                    new LatLng(origin.getLatitude(), origin.getLongitude()));
            // Centered horizontally on the anchor point -- same convention as the building
            // labels (Gravity.CENTER + min touch target) rather than left-aligning the
            // pill's edge on the point (code review fix, 2026-07-12).
            float labelWidth = accessibleRouteLabelView.getWidth();
            accessibleRouteLabelView.setX(originScreenPoint.x - labelWidth / 2f);
            accessibleRouteLabelView.setY(originScreenPoint.y - dpToPx(32f));
            accessibleRouteLabelView.setVisibility(View.VISIBLE);
        } else {
            accessibleRouteLabelView.setVisibility(View.INVISIBLE);
        }
    }

    private void repositionLocationMarker() {
        if (map == null || lastMarkerPosition == null) {
            return;
        }
        PointF screenPoint = map.getProjection().toScreenLocation(lastMarkerPosition);
        float halfTouchTargetPx = dpToPx(TOUCH_TARGET_DP) / 2f;
        locationMarkerView.setX(screenPoint.x - halfTouchTargetPx);
        locationMarkerView.setY(screenPoint.y - halfTouchTargetPx);
        locationMarkerView.setVisibility(View.VISIBLE);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        startLocationUpdatesOrRequestPermission();
    }

    private void startLocationUpdatesOrRequestPermission() {
        if (hasLocationPermission()) {
            locationProvider.start();
            return;
        }
        // Standard Android pattern: explain why before asking again once the user has
        // already declined once (but not permanently) -- EXPERIENCE.md's exact microcopy.
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            View root = getView();
            if (root != null) {
                Snackbar.make(root, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.location_permission_rationale_action, v -> requestLocationPermission())
                        .show();
                return;
            }
        }
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void onLocationPermissionResult(Map<String, Boolean> grantResults) {
        if (getView() == null) {
            return;
        }
        boolean granted = Boolean.TRUE.equals(grantResults.get(Manifest.permission.ACCESS_FINE_LOCATION))
                || Boolean.TRUE.equals(grantResults.get(Manifest.permission.ACCESS_COARSE_LOCATION));
        if (granted) {
            locationProvider.start();
        } else {
            // locationProvider.start() is never called on this path, so the shared
            // instance itself will never report denial -- tell the ViewModel directly so
            // state updates immediately (Story 1.2 Task 4).
            viewModel.onPermissionDenied();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        locationProvider.stop();
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        // Prevent a late camera-idle/-move callback from touching the map after
        // mapView.onDestroy() tears down the native renderer below.
        map = null;
        mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
