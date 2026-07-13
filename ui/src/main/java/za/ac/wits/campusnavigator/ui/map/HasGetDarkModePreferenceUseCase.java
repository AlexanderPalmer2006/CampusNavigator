package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.usecase.GetDarkModePreferenceUseCase;

/**
 * Implemented by the hosting Activity so any Fragment can obtain its dependency without
 * :ui depending on :app (ARCHITECTURE-SPINE.md AD-10). Same seam shape as
 * HasGetAccessibilityPreferenceUseCase (Story 3.1). Story 5.2.
 */
public interface HasGetDarkModePreferenceUseCase {
    GetDarkModePreferenceUseCase getGetDarkModePreferenceUseCase();
}
