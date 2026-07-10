package za.ac.wits.campusnavigator.ui.map;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.List;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.ui.R;

/**
 * The Map (home) surface -- FR1, FR2 partial (location arrives in Story 1.2). Renders a
 * fully offline map with Building labels visible at default zoom, per Story 1.1 AC 1.
 *
 * All seven MapView lifecycle forwards are mandatory in Java -- skipping onStop/onDestroy
 * leaks the native renderer (see Story 1.1 Dev Notes / web research).
 *
 * <p>Building labels are drawn as native {@link TextView}s overlaid on the MapView and
 * repositioned on every camera move, rather than as a MapLibre SymbolLayer. A SymbolLayer's
 * text-field requires a "glyphs" font-PBF source in the style, which this offline-only style
 * doesn't have (no bundled glyph assets exist) -- with no glyphs source, SymbolLayer text
 * renders nothing, silently. This was only caught by an actual on-device run: the build and
 * unit tests give no signal that label text never appears. The native-overlay approach needs
 * no font assets and was verified to actually render on screen.</p>
 */
public final class MapFragment extends Fragment {

    private static final int DEFAULT_ZOOM = 16;
    private static final int CAMERA_BOUNDS_PADDING_PX = 96;

    private MapView mapView;
    private FrameLayout labelOverlay;
    private MapLibreMap map;
    private boolean cameraPositioned;
    private final List<LabelView> labelViews = new ArrayList<>();

    private static final class LabelView {
        final LatLng position;
        final TextView view;

        LabelView(LatLng position, TextView view) {
            this.position = position;
            this.view = view;
        }
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

        mapView = view.findViewById(R.id.mapView);
        labelOverlay = view.findViewById(R.id.buildingLabelOverlay);
        mapView.onCreate(savedInstanceState);
        mapView.setContentDescription(getString(R.string.map_attribution));

        HasGetBuildingsUseCase provider = (HasGetBuildingsUseCase) requireActivity();
        MapViewModelFactory factory = new MapViewModelFactory(provider.getGetBuildingsUseCase());
        MapViewModel viewModel = new ViewModelProvider(this, factory).get(MapViewModel.class);

        mapView.getMapAsync(mapLibreMap -> {
            map = mapLibreMap;
            map.addOnCameraIdleListener(this::repositionLabels);
            map.setStyle(new Style.Builder().fromUri("asset://style.json"),
                    style -> viewModel.getBuildings().observe(getViewLifecycleOwner(),
                            this::renderBuildingLabels));
        });
    }

    private void renderBuildingLabels(@Nullable List<Building> buildings) {
        if (buildings == null || buildings.isEmpty() || map == null) {
            return;
        }

        labelOverlay.removeAllViews();
        labelViews.clear();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (Building building : buildings) {
            LatLng position = new LatLng(building.getLatitude(), building.getLongitude());
            boundsBuilder.include(position);

            TextView label = new TextView(requireContext());
            label.setText(building.getName());
            label.setTextSize(12f);
            label.setTextColor(getResources().getColor(R.color.ink_primary, requireContext().getTheme()));
            labelOverlay.addView(label, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            labelViews.add(new LabelView(position, label));
        }

        // Without this, the map's default camera (0,0 / zoom 0) never shows the Wits
        // buildings the labels above are drawn at -- caught only by an on-device run.
        if (!cameraPositioned) {
            cameraPositioned = true;
            if (buildings.size() == 1) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        labelViews.get(0).position, DEFAULT_ZOOM));
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(), CAMERA_BOUNDS_PADDING_PX));
            }
        } else {
            repositionLabels();
        }
    }

    /**
     * Called on every camera idle so labels track their real-world position as the user
     * pans/zooms (AC 2), and once right after the initial moveCamera above.
     */
    private void repositionLabels() {
        if (map == null) {
            return;
        }
        for (LabelView labelView : labelViews) {
            PointF screenPoint = map.getProjection().toScreenLocation(labelView.position);
            labelView.view.setX(screenPoint.x);
            labelView.view.setY(screenPoint.y);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
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
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
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
