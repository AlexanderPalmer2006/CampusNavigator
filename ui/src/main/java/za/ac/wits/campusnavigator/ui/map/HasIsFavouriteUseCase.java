package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.IsFavouriteUseCase;

/**
 * Implemented by the hosting Activity so BuildingInfoFragment can obtain its dependency
 * without :ui depending on :app (ARCHITECTURE-SPINE.md AD-10). Same seam shape as
 * HasGetBuildingDetailsUseCase (Story 2.1). Story 5.1.
 */
public interface HasIsFavouriteUseCase {
    IsFavouriteUseCase getIsFavouriteUseCase();
}
