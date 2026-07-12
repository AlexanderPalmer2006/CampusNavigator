package za.ac.wits.campusnavigator.ui.favourites;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.usecase.GetFavouritesUseCase;
import za.ac.wits.campusnavigator.domain.usecase.RemoveFavouriteUseCase;

public final class FavouritesViewModelFactory implements ViewModelProvider.Factory {

    private final GetFavouritesUseCase getFavouritesUseCase;
    private final RemoveFavouriteUseCase removeFavouriteUseCase;

    public FavouritesViewModelFactory(GetFavouritesUseCase getFavouritesUseCase, RemoveFavouriteUseCase removeFavouriteUseCase) {
        this.getFavouritesUseCase = getFavouritesUseCase;
        this.removeFavouriteUseCase = removeFavouriteUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FavouritesViewModel.class)) {
            return (T) new FavouritesViewModel(getFavouritesUseCase, removeFavouriteUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
