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

    /** Title-case tile label (matches the mockup's "Nearest Bathroom"/"Nearest ATM"). */
    private static String displayNameFor(String categoryName) {
        if (categoryName == null) {
            // Defensive only -- CategoryTag.name has no live production path that can be
            // null (every seeded row has a real name), but a Java String switch throws NPE
            // on a null selector, so a malformed/future row must degrade gracefully rather
            // than crash the whole Common Picks tab load (Review Findings, 2026-07-12).
            return "";
        }
        switch (categoryName) {
            case "atm":
                return "ATM";
            default:
                return capitalize(categoryName);
        }
    }

    /**
     * Public so {@code CommonPicksFragment}'s "No {category} found nearby" honest-failure
     * Snackbar (AC 2) can format the category name for that sentence-case message context
     * (Review Findings, 2026-07-12) -- deliberately *not* {@link #displayNameFor}, which is
     * title-case for a tile label ("Nearest Bathroom") and would wrongly read "No Bathroom
     * found nearby" against EXPERIENCE.md UJ-3's literal lowercase-mid-sentence copy ("No
     * bathroom found nearby"). Passes the raw (already-lowercase) category name through
     * unchanged for ordinary words, but still uppercases "ATM" -- an acronym stays
     * uppercase in sentence case too, unlike an ordinary common noun; without this
     * exception the message would read the ungrammatical "No atm found nearby" while the
     * tile that triggered it reads "Nearest ATM."
     */
    public static String sentenceCaseNameFor(String categoryName) {
        if (categoryName == null) {
            return "";
        }
        return "atm".equals(categoryName) ? "ATM" : categoryName;
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
        if (categoryName == null) {
            // Same defensive-null reasoning as displayNameFor -- a Java String switch
            // throws NPE on a null selector (Review Findings, 2026-07-12).
            return "📌";
        }
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

    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase(Locale.ROOT);
    }
}
