package za.ac.wits.campusnavigator.ui.commonpicks;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;

/**
 * The two raw domain-model lists {@link CommonPicksViewModel} loads together, before
 * {@link CommonPickTile} conversion. Kept as plain domain types here (not
 * {@code CommonPickTile}s) so the ViewModel stays free of any Android {@code Context}
 * dependency -- {@code CommonPickTile.forLandmark}/{@code forCategory} need a
 * {@code Context} to resolve string resources, so that conversion happens in
 * {@code CommonPicksFragment}, which already has one, not here.
 */
public final class CommonPicksData {

    private final List<Building> landmarkPicks;
    private final List<CategoryTag> categoryPicks;

    public CommonPicksData(List<Building> landmarkPicks, List<CategoryTag> categoryPicks) {
        this.landmarkPicks = landmarkPicks;
        this.categoryPicks = categoryPicks;
    }

    public List<Building> getLandmarkPicks() {
        return landmarkPicks;
    }

    public List<CategoryTag> getCategoryPicks() {
        return categoryPicks;
    }
}
