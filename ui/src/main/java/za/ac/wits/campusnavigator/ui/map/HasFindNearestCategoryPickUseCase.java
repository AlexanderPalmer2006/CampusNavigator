package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.FindNearestCategoryPickUseCase;

/**
 * Implemented by the hosting Activity so NavigationViewModelFactory's construction sites
 * can obtain this dependency without :ui depending on :app (ARCHITECTURE-SPINE.md AD-10).
 * Same seam shape as HasComputeRouteUseCase (Story 2.2). Story 4.2.
 */
public interface HasFindNearestCategoryPickUseCase {
    FindNearestCategoryPickUseCase getFindNearestCategoryPickUseCase();
}
