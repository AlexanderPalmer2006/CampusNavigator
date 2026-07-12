package za.ac.wits.campusnavigator.domain.model;

import za.ac.wits.campusnavigator.domain.result.Result;

/**
 * One row of the reconciled Favourites list (Story 5.1). Pairs the stable
 * {@code buildingId} (always present) with the {@link Result Result&lt;Building&gt;}
 * resolution the id produced -- {@code Success} when the Building still exists,
 * {@code Error(BUILDING_NO_LONGER_EXISTS)} when it doesn't (ARCHITECTURE-SPINE.md AD-6).
 *
 * <p>This wraps {@code Result<Building>} rather than being replaced by it: {@code
 * Result.Error}'s payload is only an {@code ErrorType}, no id, so a bare {@code
 * List<Result<Building>>} would give the UI no way to know *which* Favourite a stale
 * entry belongs to -- and specifically no way to still offer its unsave action (AC 6:
 * "not a broken row" means the row must remain actionable, not just honestly labeled).
 * {@link #getBuildingId()} is deliberately always available, regardless of
 * {@link #getResolution()}'s outcome.</p>
 */
public final class FavouriteItem {

    private final long buildingId;
    private final Result<Building> resolution;

    public FavouriteItem(long buildingId, Result<Building> resolution) {
        this.buildingId = buildingId;
        this.resolution = resolution;
    }

    public long getBuildingId() {
        return buildingId;
    }

    public Result<Building> getResolution() {
        return resolution;
    }
}
