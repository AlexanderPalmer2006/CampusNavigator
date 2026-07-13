package za.ac.wits.campusnavigator.domain.model;

import java.util.List;

/**
 * A Building's real-world footprint: one or more closed polygon rings (Story 6.3,
 * epic-6-scoping-2026-07-12.md §1). A footprint is repeating/multi-polygon, not scalar --
 * most Buildings have exactly one ring, but a multi-block Building could have several. Each
 * ring reuses {@link Position} as its vertex carrier type, the same way {@link Route}'s own
 * Javadoc already documents doing for waypoints ("accuracyMeters isn't meaningful... and is
 * always 0 here, not a GPS reading") -- same precedent, not a new pattern.
 *
 * <p>A hole (a ring nested inside another) is out of scope for V1 -- every ring here is a
 * simple, independent closed polygon.</p>
 */
public final class BuildingFootprint {

    private final long buildingId;
    private final List<List<Position>> rings;

    public BuildingFootprint(long buildingId, List<List<Position>> rings) {
        this.buildingId = buildingId;
        this.rings = rings;
    }

    public long getBuildingId() {
        return buildingId;
    }

    public List<List<Position>> getRings() {
        return rings;
    }
}
