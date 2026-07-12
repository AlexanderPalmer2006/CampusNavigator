package za.ac.wits.campusnavigator.ui.buildinginfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.IsFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.RemoveFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SaveFavouriteUseCase;

public final class BuildingInfoViewModelFactory implements ViewModelProvider.Factory {

    private final GetBuildingDetailsUseCase getBuildingDetailsUseCase;
    private final IsFavouriteUseCase isFavouriteUseCase;
    private final SaveFavouriteUseCase saveFavouriteUseCase;
    private final RemoveFavouriteUseCase removeFavouriteUseCase;
    private final long buildingId;

    public BuildingInfoViewModelFactory(GetBuildingDetailsUseCase getBuildingDetailsUseCase,
                                         IsFavouriteUseCase isFavouriteUseCase, SaveFavouriteUseCase saveFavouriteUseCase,
                                         RemoveFavouriteUseCase removeFavouriteUseCase, long buildingId) {
        this.getBuildingDetailsUseCase = getBuildingDetailsUseCase;
        this.isFavouriteUseCase = isFavouriteUseCase;
        this.saveFavouriteUseCase = saveFavouriteUseCase;
        this.removeFavouriteUseCase = removeFavouriteUseCase;
        this.buildingId = buildingId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BuildingInfoViewModel.class)) {
            return (T) new BuildingInfoViewModel(getBuildingDetailsUseCase, isFavouriteUseCase, saveFavouriteUseCase,
                    removeFavouriteUseCase, buildingId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
