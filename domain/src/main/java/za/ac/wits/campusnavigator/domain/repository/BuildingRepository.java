package za.ac.wits.campusnavigator.domain.repository;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.BuildingDetails;
import za.ac.wits.campusnavigator.domain.model.BuildingFootprint;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;

/**
 * Repository seam between ViewModels/use cases and the data layer (ARCHITECTURE-SPINE.md
 * AD-1, AD-2). Defined here in :domain; implemented in :data against Room. ViewModels must
 * never bypass this to query Room directly.
 */
public interface BuildingRepository {

    /**
     * Returns every Building known for the current campus. Never null — an empty list
     * signals "no buildings available" rather than a failure; a real I/O failure surfaces
     * as an unchecked exception (unexpected/programmer-error condition, per AD-9), not a
     * Result type, since there is no meaningful "expected failure" case for this read.
     */
    List<Building> getAllBuildings();

    /**
     * Returns every Building curated as a "Landmark Pick" (FR-8, Story 4.1) -- never null,
     * an empty list means none are curated. Same "empty list is a valid state, real I/O
     * failure throws" convention as {@link #getAllBuildings()}.
     */
    List<Building> getLandmarkPicks();

    /**
     * Returns the Building Info Page aggregate for one Building (FR-5, Story 2.1). A real
     * I/O failure surfaces as an unchecked exception, same convention as
     * {@link #getAllBuildings()}.
     */
    BuildingDetails getBuildingDetails(long buildingId);

    /**
     * Returns every Building carrying the given category tag name (e.g. "bathroom") --
     * never null, an empty list means no Building carries that tag (Story 4.2, AD-7). Same
     * "empty list is a valid state, real I/O failure throws" convention as
     * {@link #getAllBuildings()}; the meaningful "no match" *expected-failure* outcome is
     * derived one layer up, in {@code FindNearestCategoryPickUseCase}.
     */
    List<Building> getBuildingsByCategory(String categoryName);

    /**
     * Returns every {@link CategoryTag} curated as a "Category Pick" tile on the Common
     * Picks tab (Story 4.2) -- never null, an empty list means none are curated. Same
     * convention as {@link #getLandmarkPicks()}, applied to CategoryTag instead of Building.
     */
    List<CategoryTag> getCommonPickCategories();

    /**
     * Returns just the {@link Building} for one id, {@code null} if unknown/stale -- the
     * lighter-weight counterpart to {@link #getBuildingDetails(long)} for callers that only
     * need the Building itself, not its category tags/photo (Story 5.1's Favourites
     * reconciliation, {@code GetFavouritesUseCase}, doesn't need either). Same
     * null-on-missing convention as {@link #getBuildingDetails(long)}.
     */
    Building getBuildingById(long buildingId);

    /**
     * Returns every {@link BuildingFootprint} with seeded polygon data (Story 6.3) -- never
     * null, an empty list means no Building has a seeded footprint yet. A Building with no
     * footprint simply has no corresponding entry here (same "omit entirely, no
     * placeholder" precedent as {@code BuildingPhoto}'s absence, AC 3). Same "empty list is
     * a valid state, real I/O failure throws" convention as {@link #getAllBuildings()}.
     */
    List<BuildingFootprint> getAllBuildingFootprints();
}
