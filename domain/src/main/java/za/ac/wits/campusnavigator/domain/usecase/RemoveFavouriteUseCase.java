package za.ac.wits.campusnavigator.domain.usecase;

import za.ac.wits.campusnavigator.domain.repository.FavouritesRepository;

/**
 * Removes a Building from Favourites (FR-9, Story 5.1, AC 4) -- either from the Building
 * Info Page toggle or the Favourites list's own unsave icon, both call this same use case.
 * A no-op if it isn't currently favourited. Not {@code Result<T>}, same reasoning as
 * {@link SaveFavouriteUseCase}.
 */
public final class RemoveFavouriteUseCase {

    private final FavouritesRepository favouritesRepository;

    public RemoveFavouriteUseCase(FavouritesRepository favouritesRepository) {
        this.favouritesRepository = favouritesRepository;
    }

    public void execute(long buildingId) {
        favouritesRepository.removeFavourite(buildingId);
    }
}
