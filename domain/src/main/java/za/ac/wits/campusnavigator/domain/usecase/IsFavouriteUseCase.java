package za.ac.wits.campusnavigator.domain.usecase;

import za.ac.wits.campusnavigator.domain.repository.FavouritesRepository;

/**
 * Reads whether a Building is currently favourited (Story 5.1) -- drives the Building Info
 * Page's Save/Unsave toggle initial state. Not {@code Result<T>}, same reasoning as
 * {@code GetAccessibilityPreferenceUseCase}.
 */
public final class IsFavouriteUseCase {

    private final FavouritesRepository favouritesRepository;

    public IsFavouriteUseCase(FavouritesRepository favouritesRepository) {
        this.favouritesRepository = favouritesRepository;
    }

    public boolean execute(long buildingId) {
        return favouritesRepository.isFavourite(buildingId);
    }
}
