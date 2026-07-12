package za.ac.wits.campusnavigator.ui.favourites;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.domain.model.FavouriteItem;
import za.ac.wits.campusnavigator.domain.usecase.GetFavouritesUseCase;
import za.ac.wits.campusnavigator.domain.usecase.RemoveFavouriteUseCase;

/**
 * Calls :domain's GetFavouritesUseCase/RemoveFavouriteUseCase only -- never a :data class
 * directly (ARCHITECTURE-SPINE.md AD-1). Same off-main-thread-load-into-LiveData +
 * try/catch-and-degrade shape every prior list-loading ViewModel in this app uses (Story
 * 1.1's corrupted-asset-read precedent) -- a lookup failure degrades to an empty list, not
 * a crash.
 *
 * <p>Story 5.1: {@link #unsaveFavourite(long)} performs the Room delete <em>and</em> the
 * subsequent reload on this same single-thread {@code ExecutorService}, back to back --
 * both the write and the read must stay off the main thread (the same main-thread-Room-
 * query crash class Story 1.1 originally established), and running them on the same
 * executor guarantees the reload's read can never race ahead of the delete's write. Unlike
 * a Room {@code Flow}/reactive query, {@code GetFavouritesUseCase} is a one-shot read --
 * unsaving a row from this same list has no other way to disappear from the displayed list
 * than an explicit reload; {@code CommonPicksViewModel} (Story 4.1/4.2) never needed this
 * because nothing on that tab mutates its own data source.</p>
 */
public final class FavouritesViewModel extends ViewModel {

    private static final String TAG = "FavouritesViewModel";

    private final MutableLiveData<List<FavouriteItem>> favourites = new MutableLiveData<>();
    private final GetFavouritesUseCase getFavouritesUseCase;
    private final RemoveFavouriteUseCase removeFavouriteUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public FavouritesViewModel(@NonNull GetFavouritesUseCase getFavouritesUseCase,
                                @NonNull RemoveFavouriteUseCase removeFavouriteUseCase) {
        this.getFavouritesUseCase = getFavouritesUseCase;
        this.removeFavouriteUseCase = removeFavouriteUseCase;
        loadFavourites();
    }

    public LiveData<List<FavouriteItem>> getFavourites() {
        return favourites;
    }

    /** AC 4: removes a Favourite from this list's own unsave icon, then reloads. */
    public void unsaveFavourite(long buildingId) {
        executorService.execute(() -> {
            removeFavouriteUseCase.execute(buildingId);
            postFavourites();
        });
    }

    private void loadFavourites() {
        executorService.execute(this::postFavourites);
    }

    private void postFavourites() {
        try {
            favourites.postValue(getFavouritesUseCase.execute());
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to load Favourites", e);
            favourites.postValue(Collections.emptyList());
        }
    }

    @Override
    protected void onCleared() {
        executorService.shutdown();
    }
}
