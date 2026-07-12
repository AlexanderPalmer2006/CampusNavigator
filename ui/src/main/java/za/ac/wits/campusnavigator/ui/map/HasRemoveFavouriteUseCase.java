package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.RemoveFavouriteUseCase;

/**
 * Implemented by the hosting Activity so BuildingInfoFragment/FavouritesFragment can
 * obtain this dependency without :ui depending on :app (ARCHITECTURE-SPINE.md AD-10).
 * Story 5.1.
 */
public interface HasRemoveFavouriteUseCase {
    RemoveFavouriteUseCase getRemoveFavouriteUseCase();
}
