package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.GetBuildingFootprintsUseCase;

/**
 * Implemented by the hosting Activity (in :app) so MapFragment can obtain its dependency
 * without :ui depending on :app or on any DI framework (ARCHITECTURE-SPINE.md AD-10:
 * manual DI via a composition root). Same two-hop delegation shape as every other {@code
 * Has*} interface in this app (Story 6.3).
 */
public interface HasGetBuildingFootprintsUseCase {
    GetBuildingFootprintsUseCase getGetBuildingFootprintsUseCase();
}
