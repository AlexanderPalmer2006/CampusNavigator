package za.ac.wits.campusnavigator.domain.result;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ResultTest {

    @Test
    public void success_carriesItsValue() {
        Result<String> result = Result.success("route");

        assertTrue(result instanceof Result.Success);
        assertEquals("route", ((Result.Success<String>) result).getValue());
    }

    @Test
    public void error_carriesItsErrorType() {
        Result<String> result = Result.error(Result.ErrorType.NO_ROUTE_AVAILABLE);

        assertTrue(result instanceof Result.Error);
        assertEquals(Result.ErrorType.NO_ROUTE_AVAILABLE, ((Result.Error<String>) result).getErrorType());
    }

    @Test
    public void notFound_isItsOwnDistinctVariant() {
        Result<String> result = Result.notFound();

        assertTrue(result instanceof Result.NotFound);
    }
}
