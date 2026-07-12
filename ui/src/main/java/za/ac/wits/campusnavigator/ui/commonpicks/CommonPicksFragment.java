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
import java.util.ArrayList;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;
import za.ac.wits.campusnavigator.domain.result.Result;
import za.ac.wits.campusnavigator.ui.R;
import za.ac.wits.campusnavigator.ui.common.Event;
import za.ac.wits.campusnavigator.ui.map.HasBottomNavigation;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasFindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetCommonPickCategoriesUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetLandmarkPicksUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModel;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModelFactory;

/**
 * The Common Picks tab (FR-8): a curated grid of Landmark Pick tiles (Story 4.1) and
 * Category Pick tiles (Story 4.2), reachable without any prior search (AC 1). Tapping a
 * Landmark tile starts navigation directly to that Building; tapping a Category tile
 * resolves it to the nearest Building carrying that category by real walking distance
 * (AD-7) and then starts navigation the same way -- either way, no intermediate
 * confirmation screen (AC 2), reusing the exact same Activity-scoped
 * {@link NavigationViewModel} mechanism BuildingInfoFragment's "Start Navigation" button
 * already established (Story 2.2/3.1's "two Fragments, one Activity-scoped instance"
 * pattern).
 */
public final class CommonPicksFragment extends Fragment {

    /**
     * Set right before calling {@code startNavigationToNearestCategoryPick}, read when
     * the resulting {@link Event} fires, to build the "No {category} found nearby"
     * message. A rapid second tap on a *different* Category Pick tile before the first's
     * async resolution completes overwrites this and could misattribute the eventual
     * first message to the second category's name -- a known, accepted, low-likelihood
     * gap (see the story's Dev Notes and deferred-work.md), not a correctness issue with
     * which Building navigation actually starts to.
     */
    @Nullable
    private CategoryTag pendingCategoryResolution;

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
        HasGetCommonPickCategoriesUseCase categoryPicksProvider = (HasGetCommonPickCategoriesUseCase) requireActivity();
        CommonPicksViewModelFactory viewModelFactory = new CommonPicksViewModelFactory(
                landmarkPicksProvider.getGetLandmarkPicksUseCase(), categoryPicksProvider.getGetCommonPickCategoriesUseCase());
        CommonPicksViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(CommonPicksViewModel.class);

        HasComputeRouteUseCase computeRouteProvider = (HasComputeRouteUseCase) requireActivity();
        HasLocationProvider locationProviderHost = (HasLocationProvider) requireActivity();
        HasGetAccessibilityPreferenceUseCase accessibilityPreferenceProvider =
                (HasGetAccessibilityPreferenceUseCase) requireActivity();
        HasFindNearestCategoryPickUseCase categoryPickProvider = (HasFindNearestCategoryPickUseCase) requireActivity();
        NavigationViewModelFactory navigationFactory = new NavigationViewModelFactory(
                computeRouteProvider.getComputeRouteUseCase(), locationProviderHost.getLocationProvider(),
                accessibilityPreferenceProvider.getGetAccessibilityPreferenceUseCase(),
                categoryPickProvider.getFindNearestCategoryPickUseCase());
        // Activity-scoped: a route started here must survive the tab switch to Map, where
        // it's actually rendered (same resolved design as Story 2.2/3.1).
        NavigationViewModel navigationViewModel =
                new ViewModelProvider(requireActivity(), navigationFactory).get(NavigationViewModel.class);

        HasBottomNavigation bottomNavigation = (HasBottomNavigation) requireActivity();

        GridView gridView = view.findViewById(R.id.commonPicksGrid);
        CommonPicksAdapter adapter = new CommonPicksAdapter(requireContext(), new CommonPicksAdapter.OnPickTappedListener() {
            @Override
            public void onLandmarkPickTapped(Building building) {
                boolean started = navigationViewModel.startNavigation(building);
                if (started) {
                    // AC 2: no intermediate confirmation screen -- straight to the Map,
                    // where the route Task 6 (Story 2.2) already renders picks up immediately.
                    bottomNavigation.selectMapTab();
                } else {
                    Snackbar.make(view, R.string.navigation_no_position_available, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCategoryPickTapped(CategoryTag category) {
                pendingCategoryResolution = category;
                boolean started = navigationViewModel.startNavigationToNearestCategoryPick(category);
                if (!started) {
                    // Same no-position contract/message as the Landmark path -- reused,
                    // not a second "no position" message.
                    Snackbar.make(view, R.string.navigation_no_position_available, Snackbar.LENGTH_LONG).show();
                }
                // If started, the outcome arrives asynchronously via categoryPickResolution
                // (observed below) -- nothing more to do here.
            }
        });
        gridView.setAdapter(adapter);

        viewModel.getCommonPicksData().observe(getViewLifecycleOwner(), (CommonPicksData data) -> {
            List<CommonPickTile> tiles = new ArrayList<>();
            for (Building building : data.getLandmarkPicks()) {
                tiles.add(CommonPickTile.forLandmark(requireContext(), building));
            }
            for (CategoryTag category : data.getCategoryPicks()) {
                tiles.add(CommonPickTile.forCategory(requireContext(), category));
            }
            adapter.submitPicks(tiles);
        });

        // Registered unconditionally, same as every other LiveData observation in this
        // Fragment -- the Event wrapper (not conditional registration) is what prevents a
        // mere tab revisit from re-firing a stale resolution (see Event's Javadoc and the
        // story's Dev Notes).
        navigationViewModel.getCategoryPickResolution().observe(getViewLifecycleOwner(), (Event<Result<Building>> event) -> {
            Result<Building> resolution = event == null ? null : event.getContentIfNotHandled();
            if (resolution == null) {
                return;
            }
            if (resolution instanceof Result.Success) {
                // AC 1: resolved to the nearest Building by real walking distance --
                // land on the Map where the route renders, same outcome as a Landmark tap.
                bottomNavigation.selectMapTab();
            } else if (resolution instanceof Result.Error) {
                // AC 2: honest "none found nearby" message, category-specific (UJ-3).
                String categoryName = pendingCategoryResolution != null ? pendingCategoryResolution.getName() : "";
                Snackbar.make(view, getString(R.string.common_pick_category_no_match, categoryName), Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }
}
