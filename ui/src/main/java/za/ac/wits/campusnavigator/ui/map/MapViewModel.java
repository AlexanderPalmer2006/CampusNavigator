package za.ac.wits.campusnavigator.ui.map;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;

/**
 * Calls :domain's GetBuildingsUseCase only -- never a :data class directly
 * (ARCHITECTURE-SPINE.md AD-1). GetBuildingsUseCase reaches Room under the hood, which
 * forbids main-thread queries, so the call is dispatched off-thread here.
 *
 * <p>Implements {@link LocationProvider.Listener} directly (rather than a private inner
 * class) so MapFragment can also call {@link #onPermissionDenied()} directly in the
 * permission-request-denied path, where the shared LocationProvider itself never reports
 * denial because {@code start()} is never called on that path (Story 1.2 Dev Notes).</p>
 */
public final class MapViewModel extends ViewModel implements LocationProvider.Listener {

    private static final String TAG = "MapViewModel";

    private final MutableLiveData<List<Building>> buildings = new MutableLiveData<>();
    private final MutableLiveData<LocationUiState> locationState = new MutableLiveData<>(LocationUiState.initial());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final LocationProvider locationProvider;

    public MapViewModel(@NonNull GetBuildingsUseCase getBuildingsUseCase, @NonNull LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
        locationProvider.addListener(this);

        executorService.execute(() -> {
            try {
                buildings.postValue(getBuildingsUseCase.execute());
            } catch (RuntimeException e) {
                // A corrupted bundled asset must not crash the app -- degrade to an
                // empty map (no labels) rather than take down the whole process.
                Log.e(TAG, "Failed to load buildings from the bundled database", e);
                buildings.postValue(Collections.emptyList());
            }
        });
    }

    public LiveData<List<Building>> getBuildings() {
        return buildings;
    }

    LiveData<LocationUiState> getLocationState() {
        return locationState;
    }

    @Override
    public void onPositionUpdate(Position position) {
        locationState.setValue(LocationUiState.withPosition(locationState.getValue(), position));
    }

    @Override
    public void onAccuracyChanged(boolean degraded) {
        locationState.setValue(LocationUiState.withAccuracyDegraded(locationState.getValue(), degraded));
    }

    @Override
    public void onPermissionDenied() {
        locationState.setValue(LocationUiState.permissionDenied());
    }

    @Override
    protected void onCleared() {
        locationProvider.removeListener(this);
        executorService.shutdown();
    }
}
