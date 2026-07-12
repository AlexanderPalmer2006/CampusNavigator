package za.ac.wits.campusnavigator.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.ui.R;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasSetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModel;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModelFactory;

/**
 * The Settings screen (FR-11): the Accessibility Preference ("Always avoid stairs") row.
 * Common Picks and Favourites tabs are untouched, still PlaceholderFragment (Epics 4/5,
 * not this story). Story 3.1.
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
        SettingsViewModelFactory factory = new SettingsViewModelFactory(
                getProvider.getGetAccessibilityPreferenceUseCase(), setProvider.getSetAccessibilityPreferenceUseCase());
        SettingsViewModel settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);

        // Activity-scoped -- the same instance MapFragment/BuildingInfoFragment observe
        // (Story 2.2's established "two Fragments, one Activity-scoped instance" bridge).
        // Toggling here must reach whatever route is currently active (AC 4), wherever it
        // was started from.
        HasComputeRouteUseCase computeRouteProvider = (HasComputeRouteUseCase) requireActivity();
        HasLocationProvider locationProviderHost = (HasLocationProvider) requireActivity();
        LocationProvider locationProvider = locationProviderHost.getLocationProvider();
        NavigationViewModelFactory navigationFactory = new NavigationViewModelFactory(
                computeRouteProvider.getComputeRouteUseCase(), locationProvider,
                getProvider.getGetAccessibilityPreferenceUseCase());
        NavigationViewModel navigationViewModel =
                new ViewModelProvider(requireActivity(), navigationFactory).get(NavigationViewModel.class);

        SwitchMaterial switchView = view.findViewById(R.id.accessibilityPreferenceSwitch);

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
