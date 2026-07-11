package za.ac.wits.campusnavigator.ui.buildinginfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingDetailsUseCase;

public final class BuildingInfoViewModelFactory implements ViewModelProvider.Factory {

    private final GetBuildingDetailsUseCase getBuildingDetailsUseCase;
    private final long buildingId;

    public BuildingInfoViewModelFactory(GetBuildingDetailsUseCase getBuildingDetailsUseCase, long buildingId) {
        this.getBuildingDetailsUseCase = getBuildingDetailsUseCase;
        this.buildingId = buildingId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BuildingInfoViewModel.class)) {
            return (T) new BuildingInfoViewModel(getBuildingDetailsUseCase, buildingId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
