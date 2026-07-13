package za.ac.wits.campusnavigator.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.ui.R;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasFindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasSetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModel;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModelFactory;

/**
 * The Settings screen (FR-10, FR-11): the Dark Mode and Accessibility Preference
 * ("Always avoid stairs") rows. Common Picks and Favourites tabs have their own real
 * content since Stories 4.1/5.1. Story 3.1, extended in Story 5.2 for Dark Mode.
 */
public final class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HasGetAccessibilityPreferenceUseCase getProvider = (HasGetAccessibilityPreferenceUseCase) requireActivity();
        HasSetAccessibilityPreferenceUseCase setProvider = (HasSetAccessibilityPreferenceUseCase) requireActivity();
        HasGetDarkModePreferenceUseCase getDarkModeProvider = (HasGetDarkModePreferenceUseCase) requireActivity();
        HasSetDarkModePreferenceUseCase setDarkModeProvider = (HasSetDarkModePreferenceUseCase) requireActivity();
        SettingsViewModelFactory factory = new SettingsViewModelFactory(
                getProvider.getGetAccessibilityPreferenceUseCase(), setProvider.getSetAccessibilityPreferenceUseCase(),
                getDarkModeProvider.getGetDarkModePreferenceUseCase(), setDarkModeProvider.getSetDarkModePreferenceUseCase());
        SettingsViewModel settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);

        // Activity-scoped -- the same instance MapFragment/BuildingInfoFragment observe
        // (Story 2.2's established "two Fragments, one Activity-scoped instance" bridge).
        // Toggling here must reach whatever route is currently active (AC 4), wherever it
        // was started from. Dark Mode has no NavigationSession/route interaction (Story
        // 5.2 Dev Notes) -- this wiring exists solely for the accessibility switch below.
        HasComputeRouteUseCase computeRouteProvider = (HasComputeRouteUseCase) requireActivity();
        HasLocationProvider locationProviderHost = (HasLocationProvider) requireActivity();
        HasFindNearestCategoryPickUseCase categoryPickProvider = (HasFindNearestCategoryPickUseCase) requireActivity();
        LocationProvider locationProvider = locationProviderHost.getLocationProvider();
        NavigationViewModelFactory navigationFactory = new NavigationViewModelFactory(
                computeRouteProvider.getComputeRouteUseCase(), locationProvider,
                getProvider.getGetAccessibilityPreferenceUseCase(), categoryPickProvider.getFindNearestCategoryPickUseCase());
        NavigationViewModel navigationViewModel =
                new ViewModelProvider(requireActivity(), navigationFactory).get(NavigationViewModel.class);

        SwitchMaterial darkModeSwitchView = view.findViewById(R.id.darkModePreferenceSwitch);
        View darkModeRow = view.findViewById(R.id.darkModePreferenceRow);
        darkModeRow.setOnClickListener(v -> darkModeSwitchView.toggle());

        // Same read-then-attach-listener order as the accessibility switch below (avoids
        // the same redundant-re-persist-on-initial-setChecked gotcha its own comment
        // documents).
        settingsViewModel.getDarkModePreference().observe(getViewLifecycleOwner(), enabled -> {
            darkModeSwitchView.setOnCheckedChangeListener(null);
            darkModeSwitchView.setChecked(Boolean.TRUE.equals(enabled));
            darkModeSwitchView.setOnCheckedChangeListener((button, isChecked) -> {
                // Code review fix (2026-07-13): setDefaultNightMode() (below) recreates
                // the Activity, which destroys this Fragment/ViewModel -- deferring it
                // until the write actually lands (via onPersisted) avoids a race against
                // the recreated screen's brand-new ViewModel reading back the pre-toggle
                // value on its own, unrelated executor (SettingsViewModel's own Javadoc).
                settingsViewModel.setDarkModePreference(isChecked, () -> {
                    // Dark Mode is a pure theme switch, unlike the accessibility
                    // preference -- no NavigationViewModel/route-recompute interaction
                    // (Story 5.2 Dev Notes). Applying this triggers a standard
                    // AppCompatActivity recreate to pick up the new theme resources
                    // (AC 1: "switches immediately").
                    AppCompatDelegate.setDefaultNightMode(
                            isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                });
            });
        });

        SwitchMaterial switchView = view.findViewById(R.id.accessibilityPreferenceSwitch);
        View row = view.findViewById(R.id.accessibilityPreferenceRow);

        // Code review fix (2026-07-12): Task 4 specifies the whole row is tappable/48dp,
        // not just the switch thumb -- toggling the switch here (rather than duplicating
        // the persist-and-notify logic) reuses switchView's own listener as the single
        // source of truth for "the preference changed," same as tapping the switch
        // directly would.
        row.setOnClickListener(v -> switchView.toggle());

        // Observe the persisted value first, set the switch's initial state, and only then
        // attach the change listener -- otherwise this programmatic setChecked() call would
        // immediately re-invoke the listener and needlessly re-persist the just-read value
        // (standard CompoundButton gotcha; harmless outcome here since it writes back the
        // same value, but wasteful and worth avoiding).
        settingsViewModel.getAccessibilityPreference().observe(getViewLifecycleOwner(), enabled -> {
            switchView.setOnCheckedChangeListener(null);
            switchView.setChecked(Boolean.TRUE.equals(enabled));
            switchView.setOnCheckedChangeListener((button, isChecked) -> {
                settingsViewModel.setAccessibilityPreference(isChecked);
                navigationViewModel.onAccessibilityPreferenceChanged(isChecked);
            });
        });
    }
}
