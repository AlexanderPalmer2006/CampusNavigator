package za.ac.wits.campusnavigator.data.repository;

import za.ac.wits.campusnavigator.data.local.SettingDao;
import za.ac.wits.campusnavigator.data.local.SettingEntity;
import za.ac.wits.campusnavigator.domain.repository.SettingsRepository;

/**
 * Implements the :domain-defined SettingsRepository against Room's user-data database.
 * Story 3.1, extended in Story 5.2 for the Dark Mode preference -- same key-value
 * {@code Setting} table, no schema change (see {@code SettingEntity}'s own Javadoc).
 */
public final class SettingsRepositoryImpl implements SettingsRepository {

    private static final String KEY_ACCESSIBILITY_PREFERENCE = "accessibility_preference";
    private static final String KEY_DARK_MODE_PREFERENCE = "dark_mode_preference";
    private static final String VALUE_TRUE = "true";
    private static final String VALUE_FALSE = "false";

    private final SettingDao settingDao;

    public SettingsRepositoryImpl(SettingDao settingDao) {
        this.settingDao = settingDao;
    }

    @Override
    public boolean isAccessibilityPreferenceEnabled() {
        SettingEntity entity = settingDao.getByKey(KEY_ACCESSIBILITY_PREFERENCE);
        return entity != null && VALUE_TRUE.equals(entity.value);
    }

    @Override
    public void setAccessibilityPreferenceEnabled(boolean enabled) {
        SettingEntity entity = new SettingEntity();
        entity.key = KEY_ACCESSIBILITY_PREFERENCE;
        entity.value = enabled ? VALUE_TRUE : VALUE_FALSE;
        settingDao.upsert(entity);
    }

    @Override
    public boolean isDarkModeEnabled() {
        SettingEntity entity = settingDao.getByKey(KEY_DARK_MODE_PREFERENCE);
        return entity != null && VALUE_TRUE.equals(entity.value);
    }

    @Override
    public void setDarkModeEnabled(boolean enabled) {
        SettingEntity entity = new SettingEntity();
        entity.key = KEY_DARK_MODE_PREFERENCE;
        entity.value = enabled ? VALUE_TRUE : VALUE_FALSE;
        settingDao.upsert(entity);
    }
}
