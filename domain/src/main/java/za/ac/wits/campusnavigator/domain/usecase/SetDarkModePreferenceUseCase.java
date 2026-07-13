package za.ac.wits.campusnavigator.domain.usecase;

import za.ac.wits.campusnavigator.domain.repository.SettingsRepository;

/**
 * Persists the Dark Mode preference (FR-10). Not {@code Result<T>} -- same reasoning as
 * {@link GetDarkModePreferenceUseCase}, a plain persisted-preference write with no
 * expected-failure mode. Story 5.2.
 */
public final class SetDarkModePreferenceUseCase {

    private final SettingsRepository settingsRepository;

    public SetDarkModePreferenceUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void execute(boolean enabled) {
        settingsRepository.setDarkModeEnabled(enabled);
    }
}
