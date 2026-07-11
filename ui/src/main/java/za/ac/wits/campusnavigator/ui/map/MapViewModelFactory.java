package za.ac.wits.campusnavigator.ui.map;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.search.SearchBuildingsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;

public final class MapViewModelFactory implements ViewModelProvider.Factory {

    private final GetBuildingsUseCase getBuildingsUseCase;
    private final LocationProvider locationProvider;
    private final SearchBuildingsUseCase searchBuildingsUseCase;

    public MapViewModelFactory(GetBuildingsUseCase getBuildingsUseCase, LocationProvider locationProvider,
                                SearchBuildingsUseCase searchBuildingsUseCase) {
        this.getBuildingsUseCase = getBuildingsUseCase;
        this.locationProvider = locationProvider;
        this.searchBuildingsUseCase = searchBuildingsUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MapViewModel.class)) {
            return (T) new MapViewModel(getBuildingsUseCase, locationProvider, searchBuildingsUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
