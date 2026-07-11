package za.ac.wits.campusnavigator.domain.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LevenshteinDistanceTest {

    @Test
    public void identicalStrings_haveZeroDistance() {
        assertEquals(0, LevenshteinDistance.compute("fnb", "fnb"));
    }

    @Test
    public void singleSubstitution_hasDistanceOne() {
        assertEquals(1, LevenshteinDistance.compute("fnb", "fnc"));
    }

    @Test
    public void singleInsertion_hasDistanceOne() {
        assertEquals(1, LevenshteinDistance.compute("fnb", "fnbs"));
    }

    @Test
    public void emptyAgainstNonEmpty_equalsLengthOfNonEmpty() {
        assertEquals(5, LevenshteinDistance.compute("", "great"));
    }

    @Test
    public void knownClassicExample_kittenToSitting() {
        assertEquals(3, LevenshteinDistance.compute("kitten", "sitting"));
    }
}
