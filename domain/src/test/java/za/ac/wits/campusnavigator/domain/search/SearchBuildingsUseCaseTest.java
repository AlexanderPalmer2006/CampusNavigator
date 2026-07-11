package za.ac.wits.campusnavigator.domain.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.repository.FakeBuildingRepository;

/**
 * Exercises the resolved search algorithm (Story 2.1 Dev Notes) against the same 5
 * buildings actually seeded in campus.db, so these tests double as a sanity check on the
 * real seed data's search behavior, not just abstract algorithm correctness.
 */
public class SearchBuildingsUseCaseTest {

    private static SearchBuildingsUseCase newUseCase() {
        List<Building> seed = new ArrayList<>();
        seed.add(new Building(1L, "FNB Building (Accountancy)", -26.1908, 28.0261, "wits-main", "FNB", "Accountancy"));
        seed.add(new Building(2L, "Robert Sobukwe Block", -26.1912, 28.0298, "wits-main", "RSB", null));
        seed.add(new Building(3L, "Great Hall", -26.1904, 28.0285, "wits-main", "GH", null));
        seed.add(new Building(4L, "Central Library", -26.1922, 28.0302, "wits-main", "LIB", null));
        seed.add(new Building(5L, "Origins Centre", -26.1896, 28.0309, "wits-main", "ORIG", null));
        return new SearchBuildingsUseCase(new FakeBuildingRepository(seed));
    }

    @Test
    public void exactNameMatch_returnsSingleResultNoFloorHint() {
        List<BuildingSearchResult> results = newUseCase().execute("Great Hall");

        assertEquals(1, results.size());
        assertEquals("Great Hall", results.get(0).getBuilding().getName());
        assertNull(results.get(0).getFloorHint());
    }

    @Test
    public void exactCodeMatch_isCaseInsensitive_returnsSingleResultNoFloorHint() {
        List<BuildingSearchResult> results = newUseCase().execute("fnb");

        assertEquals(1, results.size());
        assertEquals("FNB Building (Accountancy)", results.get(0).getBuilding().getName());
        assertNull(results.get(0).getFloorHint());
    }

    @Test
    public void exactCodePlusRoomToken_resolvesFloorHintFromFirstDigit() {
        List<BuildingSearchResult> results = newUseCase().execute("FNB101");

        assertEquals(1, results.size());
        assertEquals("FNB Building (Accountancy)", results.get(0).getBuilding().getName());
        assertEquals("101", results.get(0).getRoomToken());
        assertEquals(Integer.valueOf(1), results.get(0).getFloorHint());
    }

    @Test
    public void exactCodePlusRoomToken_differentBuildingAndFloor() {
        List<BuildingSearchResult> results = newUseCase().execute("LIB205");

        assertEquals(1, results.size());
        assertEquals("Central Library", results.get(0).getBuilding().getName());
        assertEquals("205", results.get(0).getRoomToken());
        assertEquals(Integer.valueOf(2), results.get(0).getFloorHint());
    }

    @Test
    public void nonNumericRoomTokenSuffix_fallsThroughToFuzzyMatch_noFloorHint() {
        List<BuildingSearchResult> results = newUseCase().execute("FNBG12");

        assertEquals(5, results.size());
        for (BuildingSearchResult result : results) {
            assertNull("Fuzzy results must never carry a Floor Hint", result.getFloorHint());
            assertNull("Fuzzy results must never carry a Room Token", result.getRoomToken());
        }
    }

    @Test
    public void typoQuery_fuzzyMatchRanksClosestNameFirst() {
        List<BuildingSearchResult> results = newUseCase().execute("Grat Hall");

        assertTrue(results.size() > 0);
        assertEquals("Great Hall", results.get(0).getBuilding().getName());
        assertNull(results.get(0).getFloorHint());
    }

    @Test
    public void noMatchAtAll_neverReturnsBareEmptyResult_returnsFuzzySuggestions() {
        List<BuildingSearchResult> results = newUseCase().execute("xyzzy");

        assertEquals("Must always surface fuzzy suggestions, never a bare no-results state", 5, results.size());
    }

    @Test
    public void blankQuery_returnsEmptyList() {
        List<BuildingSearchResult> results = newUseCase().execute("   ");

        assertTrue(results.isEmpty());
    }

    @Test
    public void nullQuery_returnsEmptyList() {
        List<BuildingSearchResult> results = newUseCase().execute(null);

        assertTrue(results.isEmpty());
    }
}
