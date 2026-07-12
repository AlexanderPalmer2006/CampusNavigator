package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.SaveFavouriteUseCase;

/**
 * Implemented by the hosting Activity so BuildingInfoFragment can obtain its dependency
 * without :ui depending on :app (ARCHITECTURE-SPINE.md AD-10). Story 5.1.
 */
public interface HasSaveFavouriteUseCase {
    SaveFavouriteUseCase getSaveFavouriteUseCase();
}
