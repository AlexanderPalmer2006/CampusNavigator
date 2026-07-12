package za.ac.wits.campusnavigator.domain.repository;

/** Plain in-memory fake, shared across :domain tests -- mirrors FakeRoutingRepository. */
public final class FakeSettingsRepository implements SettingsRepository {

    private boolean accessibilityPreferenceEnabled;

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
}
