package za.ac.wits.campusnavigator.ui.commonpicks;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.usecase.GetLandmarkPicksUseCase;

public final class CommonPicksViewModelFactory implements ViewModelProvider.Factory {

    private final GetLandmarkPicksUseCase getLandmarkPicksUseCase;

    public CommonPicksViewModelFactory(GetLandmarkPicksUseCase getLandmarkPicksUseCase) {
        this.getLandmarkPicksUseCase = getLandmarkPicksUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CommonPicksViewModel.class)) {
            return (T) new CommonPicksViewModel(getLandmarkPicksUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
