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
import java.util.concurrent.atomic.AtomicInteger;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.search.BuildingSearchResult;
import za.ac.wits.campusnavigator.domain.search.SearchBuildingsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;

/**
 * Calls :domain's GetBuildingsUseCase/SearchBuildingsUseCase only -- never a :data class
 * directly (ARCHITECTURE-SPINE.md AD-1). Both reach Room under the hood, which forbids
 * main-thread queries, so every call is dispatched off-thread here.
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
    private final MutableLiveData<List<BuildingSearchResult>> searchResults =
            new MutableLiveData<>(Collections.emptyList());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final LocationProvider locationProvider;
    private final SearchBuildingsUseCase searchBuildingsUseCase;
    private final AtomicInteger searchRequestSequence = new AtomicInteger();

    public MapViewModel(@NonNull GetBuildingsUseCase getBuildingsUseCase, @NonNull LocationProvider locationProvider,
                         @NonNull SearchBuildingsUseCase searchBuildingsUseCase) {
        this.locationProvider = locationProvider;
        this.searchBuildingsUseCase = searchBuildingsUseCase;
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

    LiveData<List<BuildingSearchResult>> getSearchResults() {
        return searchResults;
    }

    /**
     * Dispatched off-thread (search reads the bundled Room database). Stale results from an
     * earlier, slower query are discarded if a newer search has since been issued -- the
     * sequence number guards against them arriving out of order and overwriting a newer
     * result (unlikely with this dataset's size, but cheap to guard against correctly).
     */
    void search(String query) {
        int requestId = searchRequestSequence.incrementAndGet();
        executorService.execute(() -> {
            List<BuildingSearchResult> results;
            try {
                results = searchBuildingsUseCase.execute(query);
            } catch (RuntimeException e) {
                Log.e(TAG, "Search failed for query=" + query, e);
                results = Collections.emptyList();
            }
            if (requestId == searchRequestSequence.get()) {
                searchResults.postValue(results);
            }
        });
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
