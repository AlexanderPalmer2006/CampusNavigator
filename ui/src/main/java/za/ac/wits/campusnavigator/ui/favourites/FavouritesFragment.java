package za.ac.wits.campusnavigator.ui.favourites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.FavouriteItem;
import za.ac.wits.campusnavigator.ui.R;
import za.ac.wits.campusnavigator.ui.map.HasBottomNavigation;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasFindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetFavouritesUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasRemoveFavouriteUseCase;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModel;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModelFactory;

/**
 * The Favourites tab (FR-9, Story 5.1): the saved-Buildings list (AC 3), reachable in one
 * tap from the bottom nav. Tapping a resolved row starts navigation directly, reusing the
 * exact same Activity-scoped {@link NavigationViewModel}/{@code startNavigation} mechanism
 * every other pick-and-go surface in this app uses (Story 4.1's Landmark Pick tiles, this
 * story's own Building Info Page toggle) -- this is the fifth Fragment to reach into it.
 * Unsaving a row (AC 4) removes it and refreshes the list immediately.
 */
public final class FavouritesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HasGetFavouritesUseCase favouritesProvider = (HasGetFavouritesUseCase) requireActivity();
        HasRemoveFavouriteUseCase removeFavouriteProvider = (HasRemoveFavouriteUseCase) requireActivity();
        FavouritesViewModelFactory viewModelFactory = new FavouritesViewModelFactory(
                favouritesProvider.getGetFavouritesUseCase(), removeFavouriteProvider.getRemoveFavouriteUseCase());
        FavouritesViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(FavouritesViewModel.class);

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
        // it's actually rendered (same resolved design as Story 2.2/3.1/4.1/4.2).
        NavigationViewModel navigationViewModel =
                new ViewModelProvider(requireActivity(), navigationFactory).get(NavigationViewModel.class);

        HasBottomNavigation bottomNavigation = (HasBottomNavigation) requireActivity();

        ListView listView = view.findViewById(R.id.favouritesList);
        TextView emptyView = view.findViewById(R.id.emptyFavouritesText);

        FavouritesAdapter adapter = new FavouritesAdapter(requireContext(), new FavouritesAdapter.OnFavouriteInteractionListener() {
            @Override
            public void onFavouriteRowTapped(Building building) {
                boolean started = navigationViewModel.startNavigation(building);
                if (started) {
                    // Same "no intermediate confirmation screen" outcome as a Landmark
                    // Pick tile tap (Story 4.1) -- straight to the Map.
                    bottomNavigation.selectMapTab();
                } else {
                    Snackbar.make(view, R.string.navigation_no_position_available, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onUnsaveTapped(long buildingId) {
                // AC 4: removed from the list immediately. FavouritesViewModel performs
                // the delete and the reload back to back on its own background executor
                // (not here) -- both must stay off the main thread, and running them on
                // the same executor guarantees the reload can't race ahead of the delete.
                viewModel.unsaveFavourite(buildingId);
            }
        });
        listView.setAdapter(adapter);

        viewModel.getFavourites().observe(getViewLifecycleOwner(), (List<FavouriteItem> items) -> {
            boolean empty = items == null || items.isEmpty();
            listView.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            adapter.submitItems(items);
        });
    }
}
