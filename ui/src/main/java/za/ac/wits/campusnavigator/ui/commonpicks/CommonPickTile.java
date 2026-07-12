package za.ac.wits.campusnavigator.ui.commonpicks;

import android.content.Context;
import java.util.Locale;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;
import za.ac.wits.campusnavigator.ui.R;

/**
 * A single Common Picks grid cell's presentation data (Story 4.2) -- a {@code :ui}-only
 * view-state holder, not a {@code :domain} type; purely "how do I render this one tile."
 * Landmark and Category Picks share this one shape (DESIGN.md: "share one tile shape;
 * icon is the only differentiator... never color") -- {@link Kind} plus an icon/label/kind
 * caption is all {@link CommonPicksAdapter} needs to render either, and exactly one of
 * {@link #getBuilding()}/{@link #getCategory()} is non-null depending on {@link #getKind()}.
 */
public final class CommonPickTile {

    public enum Kind { LANDMARK, CATEGORY }

    private final Kind kind;
    private final String icon;
    private final String label;
    private final String kindCaption;
    private final Building building;
    private final CategoryTag category;

    private CommonPickTile(Kind kind, String icon, String label, String kindCaption, Building building, CategoryTag category) {
        this.kind = kind;
        this.icon = icon;
        this.label = label;
        this.kindCaption = kindCaption;
        this.building = building;
        this.category = category;
    }

    public static CommonPickTile forLandmark(Context context, Building building) {
        return new CommonPickTile(Kind.LANDMARK, "📍", building.getName(),
                context.getString(R.string.common_pick_landmark_kind), building, null);
    }

    public static CommonPickTile forCategory(Context context, CategoryTag category) {
        String label = context.getString(R.string.common_pick_category_label, displayNameFor(category.getName()));
        return new CommonPickTile(Kind.CATEGORY, iconFor(category.getName()), label,
                context.getString(R.string.common_pick_category_kind), null, category);
    }

    public Kind getKind() {
        return kind;
    }

    public String getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public String getKindCaption() {
        return kindCaption;
    }

    /** Non-null iff {@link #getKind()} is {@link Kind#LANDMARK}. */
    public Building getBuilding() {
        return building;
    }

    /** Non-null iff {@link #getKind()} is {@link Kind#CATEGORY}. */
    public CategoryTag getCategory() {
        return category;
    }

    /**
     * Fixed glyph lookup by category name, matching the UX mockup
     * ({@code mockups/common-picks.html}) exactly for the categories curated at V1
     * (Story 4.2 Task 1: bathroom/cafeteria/atm). A generic fallback glyph covers any
     * future Category Pick category added to the seed data without a matching glyph
     * here -- adding a new curated category needs a glyph added to this lookup, it is
     * not auto-derived from the category name.
     */
    private static String iconFor(String categoryName) {
        switch (categoryName) {
            case "bathroom":
                return "🚻";
            case "cafeteria":
                return "☕";
            case "atm":
                return "🏧";
            default:
                return "📌"; // generic fallback for a future curated category with no glyph added here yet
        }
    }

    /**
     * Presentation-only display-name lookup local to this one tile label (matches the
     * mockup's "Nearest Bathroom"/"Nearest ATM" styling exactly, including "ATM" as a
     * full-caps acronym rather than plain capitalization -- a generic
     * capitalize-first-letter transform would produce "Atm," wrong for an acronym). Does
     * not change how CategoryTag names render anywhere else; the Building Info Page's own
     * {@code formatCategoryTags} (Story 2.1) still deliberately shows tag names
     * lowercase/as-is. Same "needs an entry added here for a new curated category" caveat
     * as {@link #iconFor(String)} -- the fallback plain-capitalizes for any category name
     * not yet listed here.
     */
    private static String displayNameFor(String categoryName) {
        switch (categoryName) {
            case "atm":
                return "ATM";
            default:
                return capitalize(categoryName);
        }
    }

    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase(Locale.ROOT);
    }
}
