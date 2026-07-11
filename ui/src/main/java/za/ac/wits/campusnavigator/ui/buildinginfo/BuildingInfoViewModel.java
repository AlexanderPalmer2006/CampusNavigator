package za.ac.wits.campusnavigator.ui.buildinginfo;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.model.BuildingDetails;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingDetailsUseCase;

/**
 * Calls :domain's GetBuildingDetailsUseCase only -- never a :data class directly
 * (ARCHITECTURE-SPINE.md AD-1). Same off-main-thread + try/catch-and-degrade pattern as
 * MapViewModel's building-list load (Story 1.1's code review added the try/catch after a
 * corrupted-asset crash risk was found) -- a lookup failure must not crash the app.
 */
public final class BuildingInfoViewModel extends ViewModel {

    private static final String TAG = "BuildingInfoViewModel";

    private final MutableLiveData<BuildingDetails> details = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BuildingInfoViewModel(@NonNull GetBuildingDetailsUseCase getBuildingDetailsUseCase, long buildingId) {
        executorService.execute(() -> {
            try {
                details.postValue(getBuildingDetailsUseCase.execute(buildingId));
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to load Building details for id=" + buildingId, e);
                details.postValue(null);
            }
        });
    }

    public LiveData<BuildingDetails> getDetails() {
        return details;
    }

    @Override
    protected void onCleared() {
        executorService.shutdown();
    }
}
