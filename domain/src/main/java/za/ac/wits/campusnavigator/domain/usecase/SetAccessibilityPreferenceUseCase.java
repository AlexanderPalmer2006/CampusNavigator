package za.ac.wits.campusnavigator.domain.usecase;

import za.ac.wits.campusnavigator.domain.repository.SettingsRepository;

/**
 * Persists the Accessibility Preference ("Always avoid stairs") value (FR-11). Not
 * {@code Result<T>} -- same reasoning as {@link GetAccessibilityPreferenceUseCase}, this
 * is a plain persisted-preference write with no expected-failure mode. Story 3.1.
 */
public final class SetAccessibilityPreferenceUseCase {

    private final SettingsRepository settingsRepository;

    public SetAccessibilityPreferenceUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void execute(boolean enabled) {
        settingsRepository.setAccessibilityPreferenceEnabled(enabled);
    }
}
