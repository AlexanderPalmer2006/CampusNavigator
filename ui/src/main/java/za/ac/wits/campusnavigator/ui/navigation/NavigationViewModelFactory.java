package za.ac.wits.campusnavigator.ui.navigation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;

public final class NavigationViewModelFactory implements ViewModelProvider.Factory {

    private final ComputeRouteUseCase computeRouteUseCase;
    private final LocationProvider locationProvider;

    public NavigationViewModelFactory(ComputeRouteUseCase computeRouteUseCase, LocationProvider locationProvider) {
        this.computeRouteUseCase = computeRouteUseCase;
        this.locationProvider = locationProvider;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NavigationViewModel.class)) {
            return (T) new NavigationViewModel(computeRouteUseCase, locationProvider);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
