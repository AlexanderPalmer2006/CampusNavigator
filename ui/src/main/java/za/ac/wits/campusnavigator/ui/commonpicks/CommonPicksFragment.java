package za.ac.wits.campusnavigator.ui.commonpicks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.ui.R;
import za.ac.wits.campusnavigator.ui.map.HasBottomNavigation;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetLandmarkPicksUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModel;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModelFactory;

/**
 * The Common Picks tab (FR-8, Story 4.1): a curated grid of Landmark Pick tiles, reachable
 * without any prior search (AC 1). Tapping a tile starts navigation directly -- no
 * intermediate confirmation screen (AC 2), reusing the exact same Activity-scoped
 * {@link NavigationViewModel}/{@code startNavigation} mechanism BuildingInfoFragment's
 * "Start Navigation" button already established (Story 2.2/3.1's "two Fragments, one
 * Activity-scoped instance" pattern) -- this is the third Fragment to reach into it.
 */
public final class CommonPicksFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_common_picks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HasGetLandmarkPicksUseCase landmarkPicksProvider = (HasGetLandmarkPicksUseCase) requireActivity();
        CommonPicksViewModelFactory viewModelFactory =
                new CommonPicksViewModelFactory(landmarkPicksProvider.getGetLandmarkPicksUseCase());
        CommonPicksViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(CommonPicksViewModel.class);

        HasComputeRouteUseCase computeRouteProvider = (HasComputeRouteUseCase) requireActivity();
        HasLocationProvider locationProviderHost = (HasLocationProvider) requireActivity();
        HasGetAccessibilityPreferenceUseCase accessibilityPreferenceProvider =
                (HasGetAccessibilityPreferenceUseCase) requireActivity();
        NavigationViewModelFactory navigationFactory = new NavigationViewModelFactory(
                computeRouteProvider.getComputeRouteUseCase(), locationProviderHost.getLocationProvider(),
                accessibilityPreferenceProvider.getGetAccessibilityPreferenceUseCase());
        // Activity-scoped: a route started here must survive the tab switch to Map, where
        // it's actually rendered (same resolved design as Story 2.2/3.1).
        NavigationViewModel navigationViewModel =
                new ViewModelProvider(requireActivity(), navigationFactory).get(NavigationViewModel.class);

        HasBottomNavigation bottomNavigation = (HasBottomNavigation) requireActivity();

        GridView gridView = view.findViewById(R.id.commonPicksGrid);
        CommonPicksAdapter adapter = new CommonPicksAdapter(requireContext(), building -> {
            boolean started = navigationViewModel.startNavigation(building);
            if (started) {
                // AC 2: no intermediate confirmation screen -- straight to the Map, where
                // the route Task 6 (Story 2.2) already renders picks up immediately.
                bottomNavigation.selectMapTab();
            } else {
                Snackbar.make(view, R.string.navigation_no_position_available, Snackbar.LENGTH_LONG).show();
            }
        });
        gridView.setAdapter(adapter);

        viewModel.getLandmarkPicks().observe(getViewLifecycleOwner(), (List<Building> picks) ->
                adapter.submitLandmarkPicks(picks));
    }
}
