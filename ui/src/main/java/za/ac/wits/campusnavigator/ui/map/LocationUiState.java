package za.ac.wits.campusnavigator.ui.map;

import androidx.annotation.Nullable;
import za.ac.wits.campusnavigator.domain.model.Position;

/**
 * :ui-local presentation state for the "you are here" marker (Story 1.2). Wraps :domain's
 * Position with UI-only concerns (accuracy-degraded, permission-denied) -- kept out of
 * :domain since it's presentation state, not a domain concept (Story 1.2 Dev Notes).
 */
final class LocationUiState {

    @Nullable
    private final Position position;
    private final boolean accuracyDegraded;
    private final boolean permissionDenied;

    private LocationUiState(@Nullable Position position, boolean accuracyDegraded, boolean permissionDenied) {
        this.position = position;
        this.accuracyDegraded = accuracyDegraded;
        this.permissionDenied = permissionDenied;
    }

    static LocationUiState initial() {
        return new LocationUiState(null, false, false);
    }

    static LocationUiState withPosition(LocationUiState current, Position position) {
        return new LocationUiState(position, current.accuracyDegraded, false);
    }

    static LocationUiState withAccuracyDegraded(LocationUiState current, boolean degraded) {
        return new LocationUiState(current.position, degraded, current.permissionDenied);
    }

    static LocationUiState permissionDenied() {
        return new LocationUiState(null, false, true);
    }

    @Nullable
    Position getPosition() {
        return position;
    }

    boolean isAccuracyDegraded() {
        return accuracyDegraded;
    }

    boolean isPermissionDenied() {
        return permissionDenied;
    }
}
