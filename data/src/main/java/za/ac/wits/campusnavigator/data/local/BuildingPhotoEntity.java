package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A Building's photo, if one exists -- one-to-many per Building (ARCHITECTURE-SPINE.md ER
 * diagram). Story 2.1 seeds zero rows for all 5 buildings: no real Wits building photos
 * exist as a data-authoring artifact yet, same class of gap as Story 1.1's missing basemap
 * tiles. This table exists so the schema is ready once real photos are collected.
 */
@Entity(tableName = "BuildingPhoto")
public class BuildingPhotoEntity {

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "building_id")
    public long buildingId;

    @ColumnInfo(name = "photo_reference")
    public String photoReference;
}
