package za.ac.wits.campusnavigator.ui.commonpicks;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.usecase.GetLandmarkPicksUseCase;

/**
 * Calls :domain's GetLandmarkPicksUseCase only -- never a :data class directly
 * (ARCHITECTURE-SPINE.md AD-1). Same off-main-thread-load-into-LiveData +
 * try/catch-and-degrade shape as MapViewModel's building list (Story 1.1's code review
 * established this pattern after a corrupted-asset crash risk was found) -- a lookup
 * failure here must not crash the app, it degrades to an empty tab instead.
 */
public final class CommonPicksViewModel extends ViewModel {

    private static final String TAG = "CommonPicksViewModel";

    private final MutableLiveData<List<Building>> landmarkPicks = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CommonPicksViewModel(@NonNull GetLandmarkPicksUseCase getLandmarkPicksUseCase) {
        executorService.execute(() -> {
            try {
                landmarkPicks.postValue(getLandmarkPicksUseCase.execute());
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to load Landmark Picks from the bundled database", e);
                landmarkPicks.postValue(Collections.emptyList());
            }
        });
    }

    public LiveData<List<Building>> getLandmarkPicks() {
        return landmarkPicks;
    }

    @Override
    protected void onCleared() {
        executorService.shutdown();
    }
}
