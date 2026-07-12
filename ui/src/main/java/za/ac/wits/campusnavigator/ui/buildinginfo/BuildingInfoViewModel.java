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
import za.ac.wits.campusnavigator.domain.usecase.IsFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.RemoveFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SaveFavouriteUseCase;

/**
 * Calls :domain's GetBuildingDetailsUseCase (and, since Story 5.1, the Favourite-state use
 * cases) only -- never a :data class directly (ARCHITECTURE-SPINE.md AD-1). Same
 * off-main-thread + try/catch-and-degrade pattern as MapViewModel's building-list load
 * (Story 1.1's code review added the try/catch after a corrupted-asset crash risk was
 * found) -- a lookup failure must not crash the app.
 *
 * <p>Story 5.1: {@code isFavourite} is loaded in the same background task as {@code
 * details} (one unit of loaded content for this page, same reasoning Story 4.2's
 * CommonPicksViewModel used for its own two-source load) and degrades to {@code false} on
 * failure -- the Save button simply offers to save again rather than crashing or hiding.
 * {@link #toggleFavourite()} needs no rapid-double-tap guard: both underlying writes
 * (insert-with-IGNORE / delete-where) are idempotent, matching this project's established
 * risk tolerance for this class of gesture (Story 4.1's tile tap, Story 2.2's Start
 * Navigation button).</p>
 */
public final class BuildingInfoViewModel extends ViewModel {

    private static final String TAG = "BuildingInfoViewModel";

    private final MutableLiveData<BuildingDetails> details = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFavourite = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final IsFavouriteUseCase isFavouriteUseCase;
    private final SaveFavouriteUseCase saveFavouriteUseCase;
    private final RemoveFavouriteUseCase removeFavouriteUseCase;
    private final long buildingId;

    public BuildingInfoViewModel(@NonNull GetBuildingDetailsUseCase getBuildingDetailsUseCase,
                                  @NonNull IsFavouriteUseCase isFavouriteUseCase,
                                  @NonNull SaveFavouriteUseCase saveFavouriteUseCase,
                                  @NonNull RemoveFavouriteUseCase removeFavouriteUseCase, long buildingId) {
        this.isFavouriteUseCase = isFavouriteUseCase;
        this.saveFavouriteUseCase = saveFavouriteUseCase;
        this.removeFavouriteUseCase = removeFavouriteUseCase;
        this.buildingId = buildingId;
        executorService.execute(() -> {
            try {
                details.postValue(getBuildingDetailsUseCase.execute(buildingId));
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to load Building details for id=" + buildingId, e);
                details.postValue(null);
            }
            try {
                isFavourite.postValue(isFavouriteUseCase.execute(buildingId));
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to load Favourite state for id=" + buildingId, e);
                isFavourite.postValue(false);
            }
        });
    }

    public LiveData<BuildingDetails> getDetails() {
        return details;
    }

    public LiveData<Boolean> getIsFavourite() {
        return isFavourite;
    }

    /** Toggles Favourite state for this Building, persists it, and updates the LiveData. */
    public void toggleFavourite() {
        boolean currentlyFavourite = Boolean.TRUE.equals(isFavourite.getValue());
        boolean nextFavourite = !currentlyFavourite;
        executorService.execute(() -> {
            if (nextFavourite) {
                saveFavouriteUseCase.execute(buildingId);
            } else {
                removeFavouriteUseCase.execute(buildingId);
            }
            isFavourite.postValue(nextFavourite);
        });
    }

    @Override
    protected void onCleared() {
        executorService.shutdown();
    }
}
