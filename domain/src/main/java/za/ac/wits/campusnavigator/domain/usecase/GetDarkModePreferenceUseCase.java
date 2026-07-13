package za.ac.wits.campusnavigator.domain.usecase;

import za.ac.wits.campusnavigator.domain.repository.SettingsRepository;

/**
 * Reads the current Dark Mode preference (FR-10). Not {@code Result<T>} -- reading a
 * boolean preference has no meaningful expected-failure mode to report, same reasoning
 * that kept {@link GetAccessibilityPreferenceUseCase} plain. Story 5.2.
 */
public final class GetDarkModePreferenceUseCase {

    private final SettingsRepository settingsRepository;

    public GetDarkModePreferenceUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public boolean execute() {
        return settingsRepository.isDarkModeEnabled();
    }
}
