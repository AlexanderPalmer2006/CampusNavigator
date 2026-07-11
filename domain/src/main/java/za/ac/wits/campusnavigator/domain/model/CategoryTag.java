package za.ac.wits.campusnavigator.domain.model;

/**
 * A campus-wide category a Building can be tagged with (e.g. "library"). Pure domain
 * model — no Android or Room dependency, per ARCHITECTURE-SPINE.md AD-5.
 */
public final class CategoryTag {

    private final long id;
    private final String name;

    public CategoryTag(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
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
