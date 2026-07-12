package za.ac.wits.campusnavigator.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.annotation.Nullable;

@Dao
public interface SettingDao {

    @Query("SELECT * FROM Setting WHERE `key` = :key")
    @Nullable
    SettingEntity getByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SettingEntity entity);
}
