package za.ac.wits.campusnavigator.domain.model;

import java.util.List;

/**
 * Aggregate for the Building Info Page (FR-5, Story 2.1): a Building plus its category
 * tags and whether it has at least one photo. {@code hasPhoto} rather than a photo list --
 * the Info Page only needs to know whether to render the photo section at all, per AC 4's
 * "omit entirely, no placeholder" requirement, not manage a gallery.
 */
public final class BuildingDetails {

    private final Building building;
    private final List<CategoryTag> categoryTags;
    private final boolean hasPhoto;

    public BuildingDetails(Building building, List<CategoryTag> categoryTags, boolean hasPhoto) {
        this.building = building;
        this.categoryTags = categoryTags;
        this.hasPhoto = hasPhoto;
    }

    public Building getBuilding() {
        return building;
    }

    public List<CategoryTag> getCategoryTags() {
        return categoryTags;
    }

    public boolean hasPhoto() {
        return hasPhoto;
    }
}
