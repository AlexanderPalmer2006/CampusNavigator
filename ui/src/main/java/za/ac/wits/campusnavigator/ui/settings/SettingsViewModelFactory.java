package za.ac.wits.campusnavigator.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.usecase.GetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetAccessibilityPreferenceUseCase;

public final class SettingsViewModelFactory implements ViewModelProvider.Factory {

    private final GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase;
    private final SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase;

    public SettingsViewModelFactory(GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase,
                                     SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase) {
        this.getAccessibilityPreferenceUseCase = getAccessibilityPreferenceUseCase;
        this.setAccessibilityPreferenceUseCase = setAccessibilityPreferenceUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(getAccessibilityPreferenceUseCase, setAccessibilityPreferenceUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
