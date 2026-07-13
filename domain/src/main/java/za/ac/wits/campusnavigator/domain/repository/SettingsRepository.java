package za.ac.wits.campusnavigator.domain.repository;

/**
 * Repository seam for user preferences, stored in the user-data database
 * (ARCHITECTURE-SPINE.md AD-6, AD-1, AD-2). Defined here in :domain; implemented in
 * :data against Room. Plain synchronous methods -- I/O dispatched off the main thread by
 * the caller, same shape as {@link RoutingRepository}. Story 3.1.
 */
public interface SettingsRepository {

    /** Defaults to {@code false} (off) when no preference has ever been saved. */
    boolean isAccessibilityPreferenceEnabled();

    void setAccessibilityPreferenceEnabled(boolean enabled);

    /**
     * Defaults to {@code false} (light mode, forced) when no preference has ever been
     * saved -- EXPERIENCE.md: "Light Mode is the default surface... not default-on"
     * (Story 5.2).
     */
    boolean isDarkModeEnabled();

    void setDarkModeEnabled(boolean enabled);
}
