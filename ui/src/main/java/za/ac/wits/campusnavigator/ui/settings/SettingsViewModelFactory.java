package za.ac.wits.campusnavigator.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import za.ac.wits.campusnavigator.domain.usecase.GetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetDarkModePreferenceUseCase;

public final class SettingsViewModelFactory implements ViewModelProvider.Factory {

    private final GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase;
    private final SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase;
    private final GetDarkModePreferenceUseCase getDarkModePreferenceUseCase;
    private final SetDarkModePreferenceUseCase setDarkModePreferenceUseCase;

    public SettingsViewModelFactory(GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase,
                                     SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase,
                                     GetDarkModePreferenceUseCase getDarkModePreferenceUseCase,
                                     SetDarkModePreferenceUseCase setDarkModePreferenceUseCase) {
        this.getAccessibilityPreferenceUseCase = getAccessibilityPreferenceUseCase;
        this.setAccessibilityPreferenceUseCase = setAccessibilityPreferenceUseCase;
        this.getDarkModePreferenceUseCase = getDarkModePreferenceUseCase;
        this.setDarkModePreferenceUseCase = setDarkModePreferenceUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(getAccessibilityPreferenceUseCase, setAccessibilityPreferenceUseCase,
                    getDarkModePreferenceUseCase, setDarkModePreferenceUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}
