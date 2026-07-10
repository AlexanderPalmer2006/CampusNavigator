package za.ac.wits.campusnavigator.ui.map;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;

/**
 * Calls :domain's GetBuildingsUseCase only -- never a :data class directly
 * (ARCHITECTURE-SPINE.md AD-1). GetBuildingsUseCase reaches Room under the hood, which
 * forbids main-thread queries, so the call is dispatched off-thread here.
 */
public final class MapViewModel extends ViewModel {

    private final MutableLiveData<List<Building>> buildings = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MapViewModel(@NonNull GetBuildingsUseCase getBuildingsUseCase) {
        executorService.execute(() -> buildings.postValue(getBuildingsUseCase.execute()));
    }

    public LiveData<List<Building>> getBuildings() {
        return buildings;
    }

    @Override
    protected void onCleared() {
        executorService.shutdown();
    }
}
