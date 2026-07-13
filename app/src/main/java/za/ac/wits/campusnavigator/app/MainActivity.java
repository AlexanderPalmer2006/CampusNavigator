package za.ac.wits.campusnavigator.app;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.search.SearchBuildingsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.FindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetCommonPickCategoriesUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetFavouritesUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetLandmarkPicksUseCase;
import za.ac.wits.campusnavigator.domain.usecase.IsFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.RemoveFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SaveFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.ui.buildinginfo.BuildingInfoFragment;
import za.ac.wits.campusnavigator.ui.commonpicks.CommonPicksFragment;
import za.ac.wits.campusnavigator.ui.favourites.FavouritesFragment;
import za.ac.wits.campusnavigator.ui.map.HasBottomNavigation;
import za.ac.wits.campusnavigator.ui.map.HasBuildingNavigation;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasFindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetCommonPickCategoriesUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetFavouritesUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetLandmarkPicksUseCase;
import za.ac.wits.campusnavigator.ui.map.HasIsFavouriteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasRemoveFavouriteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSaveFavouriteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSearchBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.MapFragment;
import za.ac.wits.campusnavigator.ui.settings.SettingsFragment;

/**
 * Hosts the 4-tab bottom navigation shell (EXPERIENCE.md Information Architecture, Story
 * 1.1 AC 3). Map has real content; Settings has real content since Story 3.1; Common Picks
 * has real content since Story 4.1; Favourites has real content since Story 5.1. Also
 * hosts the Building Info Page (Story 2.1) as a contextual, back-stacked destination
 * reached from the Map tab, not a nav tab itself.
 */
public final class MainActivity extends AppCompatActivity
        implements HasGetBuildingsUseCase, HasLocationProvider, HasSearchBuildingsUseCase,
        HasGetBuildingDetailsUseCase, HasBuildingNavigation, HasComputeRouteUseCase,
        HasGetAccessibilityPreferenceUseCase, HasSetAccessibilityPreferenceUseCase,
        HasGetLandmarkPicksUseCase, HasBottomNavigation, HasGetCommonPickCategoriesUseCase,
        HasFindNearestCategoryPickUseCase, HasGetFavouritesUseCase, HasIsFavouriteUseCase,
        HasSaveFavouriteUseCase, HasRemoveFavouriteUseCase, HasGetDarkModePreferenceUseCase,
        HasSetDarkModePreferenceUseCase {

    private BottomNavigationView bottomNavView;

    private int selectedNavId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            selectedNavId = R.id.nav_map;
            showFragment(new MapFragment(), false);
        }

        bottomNavView = findViewById(R.id.bottomNav);
        bottomNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedNavId) {
                return true;
            }
            if (id == R.id.nav_map) {
                selectedNavId = id;
                showFragment(new MapFragment(), false);
                return true;
            } else if (id == R.id.nav_settings) {
                selectedNavId = id;
                showFragment(new SettingsFragment(), false);
                return true;
            } else if (id == R.id.nav_common_picks) {
                selectedNavId = id;
                showFragment(new CommonPicksFragment(), false);
                return true;
            } else if (id == R.id.nav_favourites) {
                selectedNavId = id;
                showFragment(new FavouritesFragment(), false);
                return true;
            }
            return false;
        });
    }

    /**
     * Story 4.1: tapping a Common Pick tile starts navigation and must land the user on the
     * Map tab, where the route actually renders (Story 2.2 Task 6) -- reuses the same
     * selection logic (including selectedNavId bookkeeping and the already-selected no-op
     * guard above) rather than a Fragment performing its own FragmentTransaction.
     */
    @Override
    public void selectMapTab() {
        bottomNavView.setSelectedItemId(R.id.nav_map);
    }

    /**
     * @param addToBackStack Tab switches (Story 1.1) never use the back stack -- switching
     *                        tabs is not an undoable navigation event. The Building Info
     *                        Page (Story 2.1) is the one exception: it's a contextual,
     *                        tap-through destination the user should be able to Back out of.
     */
    private void showFragment(@NonNull Fragment fragment, boolean addToBackStack) {
        if (getSupportFragmentManager().isStateSaved()) {
            // A nav tap raced onSaveInstanceState (e.g. the app is backgrounding) --
            // commit() would throw IllegalStateException here.
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public GetBuildingsUseCase getGetBuildingsUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getGetBuildingsUseCase();
    }

    @Override
    public SearchBuildingsUseCase getSearchBuildingsUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getSearchBuildingsUseCase();
    }

    @Override
    public GetBuildingDetailsUseCase getGetBuildingDetailsUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getGetBuildingDetailsUseCase();
    }

    @Override
    public LocationProvider getLocationProvider() {
        return ((CampusNavigatorApplication) getApplication()).getLocationProvider();
    }

    @Override
    public ComputeRouteUseCase getComputeRouteUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getComputeRouteUseCase();
    }

    @Override
    public GetAccessibilityPreferenceUseCase getGetAccessibilityPreferenceUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getGetAccessibilityPreferenceUseCase();
    }

    @Override
    public SetAccessibilityPreferenceUseCase getSetAccessibilityPreferenceUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getSetAccessibilityPreferenceUseCase();
    }

    @Override
    public GetLandmarkPicksUseCase getGetLandmarkPicksUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getGetLandmarkPicksUseCase();
    }

    @Override
    public GetCommonPickCategoriesUseCase getGetCommonPickCategoriesUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getGetCommonPickCategoriesUseCase();
    }

    @Override
    public FindNearestCategoryPickUseCase getFindNearestCategoryPickUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getFindNearestCategoryPickUseCase();
    }

    @Override
    public GetFavouritesUseCase getGetFavouritesUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getGetFavouritesUseCase();
    }

    @Override
    public IsFavouriteUseCase getIsFavouriteUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getIsFavouriteUseCase();
    }

    @Override
    public SaveFavouriteUseCase getSaveFavouriteUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getSaveFavouriteUseCase();
    }

    @Override
    public RemoveFavouriteUseCase getRemoveFavouriteUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getRemoveFavouriteUseCase();
    }

    @Override
    public GetDarkModePreferenceUseCase getGetDarkModePreferenceUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getGetDarkModePreferenceUseCase();
    }

    @Override
    public SetDarkModePreferenceUseCase getSetDarkModePreferenceUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getSetDarkModePreferenceUseCase();
    }

    @Override
    public void showBuildingInfo(long buildingId) {
        // Guards against a rapid double-tap (on a map label or a search result) stacking
        // two BuildingInfoFragments on the back stack before the first commit() lands
        // (Review Findings).
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (current instanceof BuildingInfoFragment) {
            return;
        }
        showFragment(BuildingInfoFragment.newInstance(buildingId), true);
    }
}
