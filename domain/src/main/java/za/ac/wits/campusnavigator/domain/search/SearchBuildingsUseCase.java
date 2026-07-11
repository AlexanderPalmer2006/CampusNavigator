package za.ac.wits.campusnavigator.domain.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;

/**
 * Resolves a raw search query to ranked Buildings (FR-3, FR-4). Implements the resolved
 * algorithm from Story 2.1 Dev Notes exactly -- do not reorder or reinvent:
 *
 * <ol>
 *   <li>Exact case-insensitive name match.</li>
 *   <li>Exact Code (+ Room-Token) match -- Building Codes are unique and non-prefixing
 *       (a seed-data invariant, not enforced here), so at most one Building can match.
 *       A purely-numeric remainder after the code yields a Floor Hint (first digit); a
 *       non-numeric remainder (PRD's unresolved non-numeric-Room-Token case) falls
 *       through to fuzzy matching rather than guessing a wrong floor.</li>
 *   <li>Fuzzy fallback: Levenshtein distance against every Building's name and code,
 *       best (minimum) distance per Building, top 5 results. Fuzzy results never carry
 *       a Floor Hint.</li>
 * </ol>
 */
public final class SearchBuildingsUseCase {

    private static final int FUZZY_RESULT_LIMIT = 5;

    private final BuildingRepository buildingRepository;

    public SearchBuildingsUseCase(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<BuildingSearchResult> execute(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        if (query.isEmpty()) {
            return Collections.emptyList();
        }
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        List<Building> buildings = buildingRepository.getAllBuildings();

        List<BuildingSearchResult> exactNameMatch = findExactNameMatch(buildings, normalizedQuery);
        if (exactNameMatch != null) {
            return exactNameMatch;
        }

        List<BuildingSearchResult> codeMatch = findCodeMatch(buildings, normalizedQuery);
        if (codeMatch != null) {
            return codeMatch;
        }

        return fuzzyMatch(buildings, normalizedQuery);
    }

    private static List<BuildingSearchResult> findExactNameMatch(List<Building> buildings, String normalizedQuery) {
        for (Building building : buildings) {
            if (building.getName() != null && building.getName().toLowerCase(Locale.ROOT).equals(normalizedQuery)) {
                return Collections.singletonList(new BuildingSearchResult(building, null, null));
            }
        }
        return null;
    }

    private static List<BuildingSearchResult> findCodeMatch(List<Building> buildings, String normalizedQuery) {
        for (Building building : buildings) {
            String code = building.getCode();
            if (code == null || code.isEmpty()) {
                continue;
            }
            String normalizedCode = code.toLowerCase(Locale.ROOT);
            if (!normalizedQuery.startsWith(normalizedCode)) {
                continue;
            }
            String remainder = normalizedQuery.substring(normalizedCode.length());
            if (remainder.isEmpty()) {
                return Collections.singletonList(new BuildingSearchResult(building, null, null));
            }
            if (isAllDigits(remainder)) {
                int floorHint = Character.getNumericValue(remainder.charAt(0));
                return Collections.singletonList(new BuildingSearchResult(building, remainder, floorHint));
            }
            // Non-numeric Room Token suffix (e.g. a letter) -- PRD's flagged, unresolved
            // case (Sec 9 Q6). Don't guess; fall through to fuzzy matching below.
            return null;
        }
        return null;
    }

    private static List<BuildingSearchResult> fuzzyMatch(List<Building> buildings, String normalizedQuery) {
        List<Building> ranked = new ArrayList<>(buildings);
        Collections.sort(ranked, Comparator.comparingInt(building -> bestDistance(building, normalizedQuery)));

        List<BuildingSearchResult> results = new ArrayList<>();
        for (int i = 0; i < ranked.size() && i < FUZZY_RESULT_LIMIT; i++) {
            results.add(new BuildingSearchResult(ranked.get(i), null, null));
        }
        return results;
    }

    private static int bestDistance(Building building, String normalizedQuery) {
        int nameDistance = building.getName() == null
                ? Integer.MAX_VALUE
                : LevenshteinDistance.compute(normalizedQuery, building.getName().toLowerCase(Locale.ROOT));
        int codeDistance = building.getCode() == null
                ? Integer.MAX_VALUE
                : LevenshteinDistance.compute(normalizedQuery, building.getCode().toLowerCase(Locale.ROOT));
        return Math.min(nameDistance, codeDistance);
    }

    private static boolean isAllDigits(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
