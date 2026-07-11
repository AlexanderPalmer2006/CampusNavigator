package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.GetBuildingDetailsUseCase;

/**
 * Implemented by the hosting Activity so BuildingInfoFragment can obtain its dependency
 * without :ui depending on :app (ARCHITECTURE-SPINE.md AD-10). Same seam shape as
 * HasGetBuildingsUseCase (Story 1.1). Story 2.1.
 */
public interface HasGetBuildingDetailsUseCase {
    GetBuildingDetailsUseCase getGetBuildingDetailsUseCase();
}
