package za.ac.wits.campusnavigator.ui.commonpicks;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.usecase.GetCommonPickCategoriesUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetLandmarkPicksUseCase;

/**
 * Calls :domain's GetLandmarkPicksUseCase and GetCommonPickCategoriesUseCase only --
 * never a :data class directly (ARCHITECTURE-SPINE.md AD-1). Same off-main-thread-load-
 * into-LiveData + try/catch-and-degrade shape as MapViewModel's building list (Story
 * 1.1's code review established this pattern after a corrupted-asset crash risk was
 * found) -- a lookup failure here must not crash the app, it degrades to an empty tab.
 *
 * <p>Story 4.2: both reads happen in the same background task and are posted together as
 * one {@link CommonPicksData} -- a failure in *either* read degrades the *whole* tab to
 * empty rather than showing a partially-loaded grid, treating "the tab's content" as one
 * unit, consistent with every other screen in this app never showing a partial-failure
 * UI. {@link CommonPickTile} conversion (which needs a {@code Context} for string
 * resources) deliberately happens in {@code CommonPicksFragment}, not here -- this
 * ViewModel stays free of any Android {@code Context} dependency, same as every other
 * ViewModel in this app.</p>
 */
public final class CommonPicksViewModel extends ViewModel {

    private static final String TAG = "CommonPicksViewModel";

    private final MutableLiveData<CommonPicksData> commonPicksData = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CommonPicksViewModel(@NonNull GetLandmarkPicksUseCase getLandmarkPicksUseCase,
                                 @NonNull GetCommonPickCategoriesUseCase getCommonPickCategoriesUseCase) {
        executorService.execute(() -> {
            try {
                commonPicksData.postValue(new CommonPicksData(
                        getLandmarkPicksUseCase.execute(), getCommonPickCategoriesUseCase.execute()));
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to load Common Picks from the bundled database", e);
                commonPicksData.postValue(new CommonPicksData(Collections.emptyList(), Collections.emptyList()));
            }
        });
    }

    public LiveData<CommonPicksData> getCommonPicksData() {
        return commonPicksData;
    }

    @Override
    protected void onCleared() {
        executorService.shutdown();
    }
}
