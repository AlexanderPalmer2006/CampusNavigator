package za.ac.wits.campusnavigator.domain.usecase;

import za.ac.wits.campusnavigator.domain.repository.SettingsRepository;

/**
 * Reads the current Accessibility Preference ("Always avoid stairs") value (FR-11). Not
 * {@code Result<T>} -- reading a boolean preference has no meaningful expected-failure
 * mode to report (AD-9 governs expected-failure cases, not every use case
 * unconditionally), same reasoning that kept {@code GetBuildingsUseCase}/
 * {@code SearchBuildingsUseCase} plain in Story 2.2. Story 3.1.
 */
public final class GetAccessibilityPreferenceUseCase {

    private final SettingsRepository settingsRepository;

    public GetAccessibilityPreferenceUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public boolean execute() {
        return settingsRepository.isAccessibilityPreferenceEnabled();
    }
}
