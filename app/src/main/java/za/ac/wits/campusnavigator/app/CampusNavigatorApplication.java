package za.ac.wits.campusnavigator.app;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import za.ac.wits.campusnavigator.data.local.CampusDatabase;
import za.ac.wits.campusnavigator.data.local.UserDatabase;
import za.ac.wits.campusnavigator.data.repository.BuildingRepositoryImpl;
import za.ac.wits.campusnavigator.data.repository.FavouritesRepositoryImpl;
import za.ac.wits.campusnavigator.data.repository.RoutingRepositoryImpl;
import za.ac.wits.campusnavigator.data.repository.SettingsRepositoryImpl;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;
import za.ac.wits.campusnavigator.domain.repository.FavouritesRepository;
import za.ac.wits.campusnavigator.domain.repository.RoutingRepository;
import za.ac.wits.campusnavigator.domain.repository.SettingsRepository;
import za.ac.wits.campusnavigator.domain.search.SearchBuildingsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.FindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingFootprintsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetCommonPickCategoriesUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetFavouritesUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetLandmarkPicksUseCase;
import za.ac.wits.campusnavigator.domain.usecase.IsFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.RemoveFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SaveFavouriteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.ui.location.AndroidLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasFindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingFootprintsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetCommonPickCategoriesUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetFavouritesUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetLandmarkPicksUseCase;
import za.ac.wits.campusnavigator.ui.map.HasIsFavouriteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasRemoveFavouriteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSaveFavouriteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSearchBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSetDarkModePreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.MapLibreInitializer;

/**
 * Manual DI composition root (ARCHITECTURE-SPINE.md AD-10) -- no Dagger/Hilt. The full
 * dependency graph is constructed here, by hand, in one file. Later stories add their own
 * wiring to this same class; keep it minimal, only what the current epic's stories need.
 */
public final class CampusNavigatorApplication extends Application
        implements HasGetBuildingsUseCase, HasLocationProvider, HasSearchBuildingsUseCase,
        HasGetBuildingDetailsUseCase, HasComputeRouteUseCase, HasGetAccessibilityPreferenceUseCase,
        HasSetAccessibilityPreferenceUseCase, HasGetLandmarkPicksUseCase, HasGetCommonPickCategoriesUseCase,
        HasFindNearestCategoryPickUseCase, HasGetFavouritesUseCase, HasIsFavouriteUseCase, HasSaveFavouriteUseCase,
        HasRemoveFavouriteUseCase, HasGetDarkModePreferenceUseCase, HasSetDarkModePreferenceUseCase,
        HasGetBuildingFootprintsUseCase {

    private GetBuildingsUseCase getBuildingsUseCase;
    private SearchBuildingsUseCase searchBuildingsUseCase;
    private GetBuildingDetailsUseCase getBuildingDetailsUseCase;
    private GetBuildingFootprintsUseCase getBuildingFootprintsUseCase;
    private ComputeRouteUseCase computeRouteUseCase;
    private GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase;
    private SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase;
    private GetLandmarkPicksUseCase getLandmarkPicksUseCase;
    private GetCommonPickCategoriesUseCase getCommonPickCategoriesUseCase;
    private FindNearestCategoryPickUseCase findNearestCategoryPickUseCase;
    private GetFavouritesUseCase getFavouritesUseCase;
    private IsFavouriteUseCase isFavouriteUseCase;
    private SaveFavouriteUseCase saveFavouriteUseCase;
    private RemoveFavouriteUseCase removeFavouriteUseCase;
    private GetDarkModePreferenceUseCase getDarkModePreferenceUseCase;
    private SetDarkModePreferenceUseCase setDarkModePreferenceUseCase;
    private LocationProvider locationProvider;

    @Override
    public void onCreate() {
        super.onCreate();

        // Required before any MapView is created/inflated -- MapLibre throws
        // MapLibreConfigurationException otherwise.
        MapLibreInitializer.initialize(this);

        CampusDatabase database = CampusDatabase.getInstance(this);
        BuildingRepository buildingRepository =
                new BuildingRepositoryImpl(database.buildingDao(), database.buildingFootprintDao());
        getBuildingsUseCase = new GetBuildingsUseCase(buildingRepository);
        searchBuildingsUseCase = new SearchBuildingsUseCase(buildingRepository);
        getBuildingDetailsUseCase = new GetBuildingDetailsUseCase(buildingRepository);
        getLandmarkPicksUseCase = new GetLandmarkPicksUseCase(buildingRepository);
        getCommonPickCategoriesUseCase = new GetCommonPickCategoriesUseCase(buildingRepository);
        getBuildingFootprintsUseCase = new GetBuildingFootprintsUseCase(buildingRepository);

        RoutingRepository routingRepository = new RoutingRepositoryImpl(database.routingDao());
        computeRouteUseCase = new ComputeRouteUseCase(routingRepository);
        findNearestCategoryPickUseCase = new FindNearestCategoryPickUseCase(buildingRepository, computeRouteUseCase);

        // The first user-data database (AD-6) -- independent from CampusDatabase above,
        // its own migration path, genuinely empty at first launch (no createFromAsset).
        UserDatabase userDatabase = UserDatabase.getInstance(this);
        SettingsRepository settingsRepository = new SettingsRepositoryImpl(userDatabase.settingDao());
        getAccessibilityPreferenceUseCase = new GetAccessibilityPreferenceUseCase(settingsRepository);
        setAccessibilityPreferenceUseCase = new SetAccessibilityPreferenceUseCase(settingsRepository);
        getDarkModePreferenceUseCase = new GetDarkModePreferenceUseCase(settingsRepository);
        setDarkModePreferenceUseCase = new SetDarkModePreferenceUseCase(settingsRepository);
        applyPersistedDarkModePreference();

        FavouritesRepository favouritesRepository = new FavouritesRepositoryImpl(userDatabase.favouriteDao());
        getFavouritesUseCase = new GetFavouritesUseCase(favouritesRepository, buildingRepository);
        isFavouriteUseCase = new IsFavouriteUseCase(favouritesRepository);
        saveFavouriteUseCase = new SaveFavouriteUseCase(favouritesRepository);
        removeFavouriteUseCase = new RemoveFavouriteUseCase(favouritesRepository);

        // Exactly one instance, shared -- never re-instantiated per feature (AD-11).
        locationProvider = new AndroidLocationProvider(this);
    }

    /**
     * Story 5.2: applies the persisted Dark Mode preference as early as possible, before
     * MainActivity's theme resolves, to avoid a wrong-then-recreate flash on cold start.
     * The read is a Room query, so it stays off the main thread (this project's
     * ExecutorService/never-main-thread convention is non-negotiable, no
     * allowMainThreadQueries() shortcut) -- a short-lived, one-shot executor submits the
     * single read, then shuts down immediately (the queued task still runs to completion;
     * this class has no ViewModel#onCleared() equivalent, and the process-lifetime
     * Application singleton doesn't need one). AppCompatDelegate.setDefaultNightMode()
     * itself is applied back on the main thread via Handler -- there is no LiveData here to
     * postValue into, this is a plain Application class, not a ViewModel.
     *
     * <p>Code review fix (2026-07-13): the original implementation only called
     * {@code setDefaultNightMode()} once the async read resolved, leaving
     * {@code AppCompatDelegate} at its own built-in default until then --
     * {@code MODE_NIGHT_FOLLOW_SYSTEM}. That default is <em>not</em> a rare, narrow timing
     * race: it means any user whose device already has system-level Dark Mode on would
     * deterministically see this app's theme resolve dark on every single cold start
     * (following the system), before this method's own async read corrected it back to
     * light and recreated the Activity -- directly contradicting EXPERIENCE.md's "Light
     * Mode is the default surface... not default-on" for that entire user population, not
     * just a rare unlucky one. Fixed by calling {@code setDefaultNightMode(MODE_NIGHT_NO)}
     * synchronously, unconditionally, as the very first statement here -- a static,
     * in-memory call with no I/O, so it carries none of the main-thread-Room-query concern
     * the rest of this method's design deliberately avoids. This guarantees zero exposure
     * to the system's own dark-mode setting leaking into a first-ever (or any) cold start,
     * deterministically, before the async read has even started. The subsequent async read
     * then only ever needs to *upgrade* to {@code MODE_NIGHT_YES} for a user who has
     * actually persisted that preference -- the remaining (much narrower, self-selected)
     * trade-off described below.</p>
     *
     * <p>Accepted trade-off (see the story's own Dev Notes "Resolved Design"): because the
     * upgrade-to-dark read is asynchronous, a returning user who previously enabled Dark
     * Mode can very rarely see one frame of light before that read resolves and
     * {@code AppCompatDelegate} recreates the Activity into dark. In the overwhelmingly
     * common case (a single-row key lookup against an already-open, tiny SQLite file) this
     * resolves in low-single-digit milliseconds, well before MainActivity's own
     * {@code onCreate()}, so no visible flash occurs in practice -- and this narrower
     * trade-off only affects users who have already opted in, not the general population
     * the fix above eliminates exposure for.</p>
     */
    private void applyPersistedDarkModePreference() {
        // Deterministic, synchronous, zero-I/O: see the Javadoc above for why this must
        // run before the async read below, not be folded into it.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ExecutorService darkModeExecutor = Executors.newSingleThreadExecutor();
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        darkModeExecutor.execute(() -> {
            boolean darkModeEnabled = getDarkModePreferenceUseCase.execute();
            if (darkModeEnabled) {
                mainThreadHandler.post(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));
            }
        });
        darkModeExecutor.shutdown();
    }

    @Override
    public GetBuildingsUseCase getGetBuildingsUseCase() {
        return getBuildingsUseCase;
    }

    @Override
    public SearchBuildingsUseCase getSearchBuildingsUseCase() {
        return searchBuildingsUseCase;
    }

    @Override
    public GetBuildingDetailsUseCase getGetBuildingDetailsUseCase() {
        return getBuildingDetailsUseCase;
    }

    @Override
    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    @Override
    public ComputeRouteUseCase getComputeRouteUseCase() {
        return computeRouteUseCase;
    }

    @Override
    public GetAccessibilityPreferenceUseCase getGetAccessibilityPreferenceUseCase() {
        return getAccessibilityPreferenceUseCase;
    }

    @Override
    public SetAccessibilityPreferenceUseCase getSetAccessibilityPreferenceUseCase() {
        return setAccessibilityPreferenceUseCase;
    }

    @Override
    public GetLandmarkPicksUseCase getGetLandmarkPicksUseCase() {
        return getLandmarkPicksUseCase;
    }

    @Override
    public GetCommonPickCategoriesUseCase getGetCommonPickCategoriesUseCase() {
        return getCommonPickCategoriesUseCase;
    }

    @Override
    public FindNearestCategoryPickUseCase getFindNearestCategoryPickUseCase() {
        return findNearestCategoryPickUseCase;
    }

    @Override
    public GetFavouritesUseCase getGetFavouritesUseCase() {
        return getFavouritesUseCase;
    }

    @Override
    public IsFavouriteUseCase getIsFavouriteUseCase() {
        return isFavouriteUseCase;
    }

    @Override
    public SaveFavouriteUseCase getSaveFavouriteUseCase() {
        return saveFavouriteUseCase;
    }

    @Override
    public RemoveFavouriteUseCase getRemoveFavouriteUseCase() {
        return removeFavouriteUseCase;
    }

    @Override
    public GetDarkModePreferenceUseCase getGetDarkModePreferenceUseCase() {
        return getDarkModePreferenceUseCase;
    }

    @Override
    public SetDarkModePreferenceUseCase getSetDarkModePreferenceUseCase() {
        return setDarkModePreferenceUseCase;
    }

    @Override
    public GetBuildingFootprintsUseCase getGetBuildingFootprintsUseCase() {
        return getBuildingFootprintsUseCase;
    }
}
