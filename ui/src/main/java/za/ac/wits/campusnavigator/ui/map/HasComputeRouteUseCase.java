package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;

/**
 * Implemented by the hosting Activity so BuildingInfoFragment/MapFragment can obtain their
 * dependency without :ui depending on :app (ARCHITECTURE-SPINE.md AD-10). Same seam shape
 * as HasGetBuildingsUseCase (Story 1.1). Story 2.2.
 */
public interface HasComputeRouteUseCase {
    ComputeRouteUseCase getComputeRouteUseCase();
}
