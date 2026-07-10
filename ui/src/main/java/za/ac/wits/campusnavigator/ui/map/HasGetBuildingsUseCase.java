package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;

/**
 * Implemented by the hosting Activity (in :app) so MapFragment can obtain its dependency
 * without :ui depending on :app or on any DI framework (ARCHITECTURE-SPINE.md AD-10:
 * manual DI via a composition root). This is the module-boundary-respecting seam a
 * framework-free multi-module app needs.
 */
public interface HasGetBuildingsUseCase {
    GetBuildingsUseCase getGetBuildingsUseCase();
}
