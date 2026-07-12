package za.ac.wits.campusnavigator.domain.model;

/**
 * A campus-wide category a Building can be tagged with (e.g. "library"). Pure domain
 * model — no Android or Room dependency, per ARCHITECTURE-SPINE.md AD-5.
 */
public final class CategoryTag {

    private final long id;
    private final String name;
    private final boolean isCommonPickCategory;

    public CategoryTag(long id, String name, boolean isCommonPickCategory) {
        this.id = id;
        this.name = name;
        this.isCommonPickCategory = isCommonPickCategory;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Curated flag (Story 4.2) marking this tag as a "Category Pick" tile on the Common
     * Picks tab -- mirrors {@code Building.isLandmarkPick}'s (Story 4.1) exact same
     * curation-flag pattern, applied one level over. Independent of whether this tag is
     * also shown descriptively on a Building Info Page (Story 2.1) -- a tag can be
     * Category-Pick-curated, descriptive, both, or neither.
     */
    public boolean isCommonPickCategory() {
        return isCommonPickCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryTag)) return false;
        CategoryTag other = (CategoryTag) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
