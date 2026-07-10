package za.ac.wits.campusnavigator.ui.map;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;

public final class MapViewModelFactory implements ViewModelProvider.Factory {

    private final GetBuildingsUseCase getBuildingsUseCase;

    public MapViewModelFactory(GetBuildingsUseCase getBuildingsUseCase) {
        this.getBuildingsUseCase = getBuildingsUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MapViewModel.class)) {
            return (T) new MapViewModel(getBuildingsUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
