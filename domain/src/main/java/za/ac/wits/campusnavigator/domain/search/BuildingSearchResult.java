package za.ac.wits.campusnavigator.domain.search;

import za.ac.wits.campusnavigator.domain.model.Building;

/**
 * One ranked search result: a Building plus an optional inferred Room Token/floor. No
 * Android dependency, per ARCHITECTURE-SPINE.md AD-5 -- nullability is documented, not
 * annotated. Both fields are only ever set together, by an exact Code+Room-Token
 * resolution -- fuzzy matches never carry either (Story 2.1 Dev Notes: Resolved Search
 * Algorithm). {@code roomToken} is kept alongside the derived {@code floorHint} so the UI
 * can render the exact microcopy ("Room 101 — estimated Floor 1") without re-parsing.
 */
public final class BuildingSearchResult {

    private final Building building;
    private final String roomToken;
    private final Integer floorHint;

    /** {@code roomToken}/{@code floorHint} are null together -- no Room Token resolved. */
    public BuildingSearchResult(Building building, String roomToken, Integer floorHint) {
        this.building = building;
        this.roomToken = roomToken;
        this.floorHint = floorHint;
    }

    public Building getBuilding() {
        return building;
    }

    /** Null unless this result came from an exact Code+Room-Token resolution. */
    public String getRoomToken() {
        return roomToken;
    }

    /** Null unless this result came from an exact Code+Room-Token resolution. */
    public Integer getFloorHint() {
        return floorHint;
    }
}
