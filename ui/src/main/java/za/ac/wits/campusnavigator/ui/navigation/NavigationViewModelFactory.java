package za.ac.wits.campusnavigator.ui.navigation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.FindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetAccessibilityPreferenceUseCase;

public final class NavigationViewModelFactory implements ViewModelProvider.Factory {

    private final ComputeRouteUseCase computeRouteUseCase;
    private final LocationProvider locationProvider;
    private final GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase;
    private final FindNearestCategoryPickUseCase findNearestCategoryPickUseCase;

    public NavigationViewModelFactory(ComputeRouteUseCase computeRouteUseCase, LocationProvider locationProvider,
                                       GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase,
                                       FindNearestCategoryPickUseCase findNearestCategoryPickUseCase) {
        this.computeRouteUseCase = computeRouteUseCase;
        this.locationProvider = locationProvider;
        this.getAccessibilityPreferenceUseCase = getAccessibilityPreferenceUseCase;
        this.findNearestCategoryPickUseCase = findNearestCategoryPickUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NavigationViewModel.class)) {
            return (T) new NavigationViewModel(computeRouteUseCase, locationProvider, getAccessibilityPreferenceUseCase,
                    findNearestCategoryPickUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
