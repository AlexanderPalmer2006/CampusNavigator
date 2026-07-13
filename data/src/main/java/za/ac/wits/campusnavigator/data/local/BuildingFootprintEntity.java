package za.ac.wits.campusnavigator.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One closed polygon ring of a Building's real-world footprint (Story 6.3,
 * ARCHITECTURE-SPINE.md / epic-6-scoping-2026-07-12.md §1). One row per ring, not one row
 * per Building -- a footprint is a repeating/multi-polygon shape, the same category
 * distinction that already put {@link CategoryTagEntity}/{@link BuildingPhotoEntity} in
 * their own tables rather than a scalar {@code BUILDING} column. Multiple rows sharing the
 * same {@code buildingId} represent a multi-polygon building (e.g. an L-shaped block); a
 * hole (a ring nested inside another) is out of scope for V1 -- none of the seeded real
 * footprints have one.
 *
 * <p>{@code ringGeoJson} is a flat JSON array of {@code [lon, lat]} pairs for one closed
 * ring, e.g. {@code "[[28.03,-26.19],[28.031,-26.19],...]"} -- parsed by hand in
 * {@code BuildingRepositoryImpl} rather than pulling in a JSON library dependency this
 * project doesn't otherwise have, for a payload this small (5-22 vertices per ring, 3 rings
 * seeded today).</p>
 */
@Entity(tableName = "BuildingFootprint")
public class BuildingFootprintEntity {

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "building_id")
    public long buildingId;

    @ColumnInfo(name = "ring_geojson")
    public String ringGeoJson;
}
