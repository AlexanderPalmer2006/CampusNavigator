package za.ac.wits.campusnavigator.ui.commonpicks;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.usecase.GetCommonPickCategoriesUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetLandmarkPicksUseCase;

public final class CommonPicksViewModelFactory implements ViewModelProvider.Factory {

    private final GetLandmarkPicksUseCase getLandmarkPicksUseCase;
    private final GetCommonPickCategoriesUseCase getCommonPickCategoriesUseCase;

    public CommonPicksViewModelFactory(GetLandmarkPicksUseCase getLandmarkPicksUseCase,
                                        GetCommonPickCategoriesUseCase getCommonPickCategoriesUseCase) {
        this.getLandmarkPicksUseCase = getLandmarkPicksUseCase;
        this.getCommonPickCategoriesUseCase = getCommonPickCategoriesUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CommonPicksViewModel.class)) {
            return (T) new CommonPicksViewModel(getLandmarkPicksUseCase, getCommonPickCategoriesUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
