package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import za.ac.wits.campusnavigator.domain.repository.FakeSettingsRepository;

public class AccessibilityPreferenceUseCaseTest {

    @Test
    public void get_defaultsToFalse_whenNeverSet() {
        GetAccessibilityPreferenceUseCase useCase =
                new GetAccessibilityPreferenceUseCase(new FakeSettingsRepository());

        assertFalse(useCase.execute());
    }

    @Test
    public void set_thenGet_reflectsThePersistedValue() {
        FakeSettingsRepository repository = new FakeSettingsRepository();
        GetAccessibilityPreferenceUseCase getUseCase = new GetAccessibilityPreferenceUseCase(repository);
        SetAccessibilityPreferenceUseCase setUseCase = new SetAccessibilityPreferenceUseCase(repository);

        setUseCase.execute(true);

        assertTrue(getUseCase.execute());
    }

    @Test
    public void set_canToggleBackOff() {
        FakeSettingsRepository repository = new FakeSettingsRepository(true);
        GetAccessibilityPreferenceUseCase getUseCase = new GetAccessibilityPreferenceUseCase(repository);
        SetAccessibilityPreferenceUseCase setUseCase = new SetAccessibilityPreferenceUseCase(repository);

        setUseCase.execute(false);

        assertFalse(getUseCase.execute());
    }
}
