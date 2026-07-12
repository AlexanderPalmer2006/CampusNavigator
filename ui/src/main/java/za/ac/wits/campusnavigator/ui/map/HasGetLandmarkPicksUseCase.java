package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.GetLandmarkPicksUseCase;

/**
 * Implemented by the hosting Activity so CommonPicksFragment can obtain its dependency
 * without :ui depending on :app (ARCHITECTURE-SPINE.md AD-10). Same seam shape as
 * HasGetBuildingsUseCase (Story 1.1). Story 4.1.
 */
public interface HasGetLandmarkPicksUseCase {
    GetLandmarkPicksUseCase getGetLandmarkPicksUseCase();
}
