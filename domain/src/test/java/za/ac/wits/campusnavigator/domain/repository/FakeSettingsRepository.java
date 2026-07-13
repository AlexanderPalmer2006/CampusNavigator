package za.ac.wits.campusnavigator.domain.repository;

/** Plain in-memory fake, shared across :domain tests -- mirrors FakeRoutingRepository. */
public final class FakeSettingsRepository implements SettingsRepository {

    private boolean accessibilityPreferenceEnabled;
    private boolean darkModeEnabled;

    public FakeSettingsRepository() {
        this(false);
    }

    public FakeSettingsRepository(boolean initialAccessibilityPreference) {
        this.accessibilityPreferenceEnabled = initialAccessibilityPreference;
    }

    @Override
    public boolean isAccessibilityPreferenceEnabled() {
        return accessibilityPreferenceEnabled;
    }

    @Override
    public void setAccessibilityPreferenceEnabled(boolean enabled) {
        this.accessibilityPreferenceEnabled = enabled;
    }

    @Override
    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    @Override
    public void setDarkModeEnabled(boolean enabled) {
        this.darkModeEnabled = enabled;
    }
}
