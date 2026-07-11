package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.search.SearchBuildingsUseCase;

/**
 * Implemented by the hosting Activity (in :app) so MapFragment can obtain its dependency
 * without :ui depending on :app or on any DI framework (ARCHITECTURE-SPINE.md AD-10). Same
 * seam shape as HasGetBuildingsUseCase (Story 1.1). Story 2.1.
 */
public interface HasSearchBuildingsUseCase {
    SearchBuildingsUseCase getSearchBuildingsUseCase();
}
