package za.ac.wits.campusnavigator.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.usecase.GetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetDarkModePreferenceUseCase;

/**
 * Fragment-scoped (unlike NavigationViewModel, nothing needs this ViewModel's own state to
 * survive a tab switch -- the *persisted* Setting is what survives, via Room). Reads the
 * current persisted value off the main thread at construction so the Settings row reflects
 * reality rather than always starting "off" (AC 1). Story 3.1, extended in Story 5.2 for
 * the Dark Mode preference -- one Fragment-scoped ViewModel for all of this screen's
 * preferences, not a separate ViewModel per row (same reasoning Story 5.1 used for adding
 * the Favourite-state use cases to BuildingInfoViewModel rather than a new one).
 */
public final class SettingsViewModel extends ViewModel {

    private final SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase;
    private final SetDarkModePreferenceUseCase setDarkModePreferenceUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> accessibilityPreference = new MutableLiveData<>();
    private final MutableLiveData<Boolean> darkModePreference = new MutableLiveData<>();

    public SettingsViewModel(@NonNull GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase,
                              @NonNull SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase,
                              @NonNull GetDarkModePreferenceUseCase getDarkModePreferenceUseCase,
                              @NonNull SetDarkModePreferenceUseCase setDarkModePreferenceUseCase) {
        this.setAccessibilityPreferenceUseCase = setAccessibilityPreferenceUseCase;
        this.setDarkModePreferenceUseCase = setDarkModePreferenceUseCase;
        executorService.execute(() -> {
            accessibilityPreference.postValue(getAccessibilityPreferenceUseCase.execute());
            darkModePreference.postValue(getDarkModePreferenceUseCase.execute());
        });
    }

    public LiveData<Boolean> getAccessibilityPreference() {
        return accessibilityPreference;
    }

    public LiveData<Boolean> getDarkModePreference() {
        return darkModePreference;
    }

    /** Persists the new value off the main thread. Instant effect, no confirmation step (AC 1). */
    public void setAccessibilityPreference(boolean enabled) {
        accessibilityPreference.setValue(enabled);
        executorService.execute(() -> setAccessibilityPreferenceUseCase.execute(enabled));
    }

    /**
     * Persists the new value off the main thread. Instant effect, no confirmation step
     * (AC 1). Optimistic {@code setValue} first (main thread -- this method is always
     * called from the switch's click listener) -- same shape as
     * {@link #setAccessibilityPreference(boolean)}, and closes the exact rapid-double-tap
     * race Story 5.1's code review found and fixed in
     * {@code BuildingInfoViewModel.toggleFavourite()}: without it, two rapid taps could
     * both read the same stale pre-write LiveData value.
     */
    public void setDarkModePreference(boolean enabled) {
        darkModePreference.setValue(enabled);
        executorService.execute(() -> setDarkModePreferenceUseCase.execute(enabled));
    }

    @Override
    protected void onCleared() {
        executorService.shutdown();
    }
}
