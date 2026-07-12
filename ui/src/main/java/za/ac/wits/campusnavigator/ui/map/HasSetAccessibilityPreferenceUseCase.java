package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.SetAccessibilityPreferenceUseCase;

/**
 * Implemented by the hosting Activity so any Fragment can obtain its dependency without
 * :ui depending on :app (ARCHITECTURE-SPINE.md AD-10). Same seam shape as
 * HasComputeRouteUseCase (Story 2.2). Story 3.1.
 */
public interface HasSetAccessibilityPreferenceUseCase {
    SetAccessibilityPreferenceUseCase getSetAccessibilityPreferenceUseCase();
}
