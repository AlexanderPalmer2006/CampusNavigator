package za.ac.wits.campusnavigator.domain.usecase;

import za.ac.wits.campusnavigator.domain.repository.FavouritesRepository;

/**
 * Saves a Building as a Favourite (FR-9, Story 5.1, AC 1/2) -- a no-op if it's already
 * favourited (enforced at the schema level, {@code FavouriteDao}'s {@code IGNORE} conflict
 * strategy). Not {@code Result<T>} -- no meaningful expected-failure mode, same reasoning
 * as {@code GetAccessibilityPreferenceUseCase}/{@code SetAccessibilityPreferenceUseCase}.
 */
public final class SaveFavouriteUseCase {

    private final FavouritesRepository favouritesRepository;

    public SaveFavouriteUseCase(FavouritesRepository favouritesRepository) {
        this.favouritesRepository = favouritesRepository;
    }

    public void execute(long buildingId) {
        favouritesRepository.saveFavourite(buildingId);
    }
}
