package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.GetFavouritesUseCase;

/**
 * Implemented by the hosting Activity so FavouritesFragment can obtain its dependency
 * without :ui depending on :app (ARCHITECTURE-SPINE.md AD-10). Same seam shape as
 * HasGetLandmarkPicksUseCase (Story 4.1). Story 5.1.
 */
public interface HasGetFavouritesUseCase {
    GetFavouritesUseCase getGetFavouritesUseCase();
}
