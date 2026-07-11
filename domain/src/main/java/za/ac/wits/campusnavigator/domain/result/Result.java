package za.ac.wits.campusnavigator.domain.result;

/**
 * Shared Result-style type for :domain use cases (ARCHITECTURE-SPINE.md AD-9). Every
 * :domain use case with an expected-failure case returns this -- one shared shape, not a
 * differently-shaped Result/Error type reinvented per use case. Null and thrown
 * exceptions are reserved for unexpected/programmer-error conditions only, never for an
 * expected failure like "no route exists."
 *
 * <p>First used by Story 2.2's {@code ComputeRouteUseCase}. {@link ErrorType} is a fixed,
 * closed set -- a new failure mode extends it (e.g. Epic 3's accessible-routing failure),
 * it never spawns a parallel Result/Error shape.</p>
 */
public abstract class Result<T> {

    private Result() {
    }

    public static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    public static <T> Result<T> error(ErrorType errorType) {
        return new Error<>(errorType);
    }

    public static <T> Result<T> notFound() {
        return new NotFound<>();
    }

    public static final class Success<T> extends Result<T> {
        private final T value;

        private Success(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }

    public static final class Error<T> extends Result<T> {
        private final ErrorType errorType;

        private Error(ErrorType errorType) {
            this.errorType = errorType;
        }

        public ErrorType getErrorType() {
            return errorType;
        }
    }

    public static final class NotFound<T> extends Result<T> {
        private NotFound() {
        }
    }

    /** Fixed, closed set -- extend, never replace or duplicate. */
    public enum ErrorType {
        NO_ROUTE_AVAILABLE
    }
}
