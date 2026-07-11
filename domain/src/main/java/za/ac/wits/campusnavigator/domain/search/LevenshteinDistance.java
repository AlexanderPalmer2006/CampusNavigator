package za.ac.wits.campusnavigator.domain.search;

/**
 * Plain edit-distance calculation for fuzzy Building-search fallback (Story 2.1). No
 * external fuzzy-matching library -- consistent with this project's established
 * minimal-dependency posture (no Play Services, no GraphHopper).
 */
final class LevenshteinDistance {

    private LevenshteinDistance() {
    }

    /** Case-sensitive by design -- callers normalize (lowercase) both inputs first. */
    static int compute(String a, String b) {
        int[][] distances = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            distances[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            distances[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int substitutionCost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                distances[i][j] = Math.min(
                        Math.min(distances[i - 1][j] + 1, distances[i][j - 1] + 1),
                        distances[i - 1][j - 1] + substitutionCost);
            }
        }
        return distances[a.length()][b.length()];
    }
}
