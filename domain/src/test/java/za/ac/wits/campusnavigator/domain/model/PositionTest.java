package za.ac.wits.campusnavigator.domain.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PositionTest {

    @Test
    public void constructor_storesAllFieldsExactly() {
        Position position = new Position(-26.1908, 28.0261, 12.5f);

        assertEquals(-26.1908, position.getLatitude(), 0.0);
        assertEquals(28.0261, position.getLongitude(), 0.0);
        assertEquals(12.5f, position.getAccuracyMeters(), 0.0f);
    }
}
