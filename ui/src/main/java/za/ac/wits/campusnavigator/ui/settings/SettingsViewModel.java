package za.ac.wits.campusnavigator.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.usecase.GetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetAccessibilityPreferenceUseCase;

/**
 * Fragment-scoped (unlike NavigationViewModel, nothing needs this ViewModel's own state to
 * survive a tab switch -- the *persisted* Setting is what survives, via Room). Reads the
 * current persisted value off the main thread at construction so the Settings row reflects
 * reality rather than always starting "off" (AC 1). Story 3.1.
 */
public final class SettingsViewModel extends ViewModel {

    private final SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> accessibilityPreference = new MutableLiveData<>();

    public SettingsViewModel(@NonNull GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase,
                              @NonNull SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase) {
        this.setAccessibilityPreferenceUseCase = setAccessibilityPreferenceUseCase;
        executorService.execute(() -> accessibilityPreference.postValue(getAccessibilityPreferenceUseCase.execute()));
    }

    public LiveData<Boolean> getAccessibilityPreference() {
        return accessibilityPreference;
    }

    /** Persists the new value off the main thread. Instant effect, no confirmation step (AC 1). */
    public void setAccessibilityPreference(boolean enabled) {
        accessibilityPreference.setValue(enabled);
        executorService.execute(() -> setAccessibilityPreferenceUseCase.execute(enabled));
    }

    @Override
    protected void onCleared() {
        executorService.shutdown();
    }
}
