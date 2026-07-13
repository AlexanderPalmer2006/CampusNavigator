package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import za.ac.wits.campusnavigator.domain.repository.FakeSettingsRepository;

public class DarkModePreferenceUseCaseTest {

    @Test
    public void get_defaultsToFalse_whenNeverSet() {
        GetDarkModePreferenceUseCase useCase = new GetDarkModePreferenceUseCase(new FakeSettingsRepository());

        assertFalse(useCase.execute());
    }

    @Test
    public void set_thenGet_reflectsThePersistedValue() {
        FakeSettingsRepository repository = new FakeSettingsRepository();
        GetDarkModePreferenceUseCase getUseCase = new GetDarkModePreferenceUseCase(repository);
        SetDarkModePreferenceUseCase setUseCase = new SetDarkModePreferenceUseCase(repository);

        setUseCase.execute(true);

        assertTrue(getUseCase.execute());
    }

    @Test
    public void set_canToggleBackOff() {
        FakeSettingsRepository repository = new FakeSettingsRepository();
        GetDarkModePreferenceUseCase getUseCase = new GetDarkModePreferenceUseCase(repository);
        SetDarkModePreferenceUseCase setUseCase = new SetDarkModePreferenceUseCase(repository);
        setUseCase.execute(true);

        setUseCase.execute(false);

        assertFalse(getUseCase.execute());
    }
}
