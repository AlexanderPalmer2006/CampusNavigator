package za.ac.wits.campusnavigator.app;

import android.app.Application;
import za.ac.wits.campusnavigator.data.local.CampusDatabase;
import za.ac.wits.campusnavigator.data.local.UserDatabase;
import za.ac.wits.campusnavigator.data.repository.BuildingRepositoryImpl;
import za.ac.wits.campusnavigator.data.repository.RoutingRepositoryImpl;
import za.ac.wits.campusnavigator.data.repository.SettingsRepositoryImpl;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;
import za.ac.wits.campusnavigator.domain.repository.RoutingRepository;
import za.ac.wits.campusnavigator.domain.repository.SettingsRepository;
import za.ac.wits.campusnavigator.domain.search.SearchBuildingsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.FindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetCommonPickCategoriesUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetLandmarkPicksUseCase;
import za.ac.wits.campusnavigator.domain.usecase.SetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.location.AndroidLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasFindNearestCategoryPickUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetCommonPickCategoriesUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetLandmarkPicksUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasSearchBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasSetAccessibilityPreferenceUseCase;
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
        HasFindNearestCategoryPickUseCase {

    private GetBuildingsUseCase getBuildingsUseCase;
    private SearchBuildingsUseCase searchBuildingsUseCase;
    private GetBuildingDetailsUseCase getBuildingDetailsUseCase;
    private ComputeRouteUseCase computeRouteUseCase;
    private GetAccessibilityPreferenceUseCase getAccessibilityPreferenceUseCase;
    private SetAccessibilityPreferenceUseCase setAccessibilityPreferenceUseCase;
    private GetLandmarkPicksUseCase getLandmarkPicksUseCase;
    private GetCommonPickCategoriesUseCase getCommonPickCategoriesUseCase;
    private FindNearestCategoryPickUseCase findNearestCategoryPickUseCase;
    private LocationProvider locationProvider;

    @Override
    public void onCreate() {
        super.onCreate();

        // Required before any MapView is created/inflated -- MapLibre throws
        // MapLibreConfigurationException otherwise.
        MapLibreInitializer.initialize(this);

        CampusDatabase database = CampusDatabase.getInstance(this);
        BuildingRepository buildingRepository = new BuildingRepositoryImpl(database.buildingDao());
        getBuildingsUseCase = new GetBuildingsUseCase(buildingRepository);
        searchBuildingsUseCase = new SearchBuildingsUseCase(buildingRepository);
        getBuildingDetailsUseCase = new GetBuildingDetailsUseCase(buildingRepository);
        getLandmarkPicksUseCase = new GetLandmarkPicksUseCase(buildingRepository);
        getCommonPickCategoriesUseCase = new GetCommonPickCategoriesUseCase(buildingRepository);

        RoutingRepository routingRepository = new RoutingRepositoryImpl(database.routingDao());
        computeRouteUseCase = new ComputeRouteUseCase(routingRepository);
        findNearestCategoryPickUseCase = new FindNearestCategoryPickUseCase(buildingRepository, computeRouteUseCase);

        // The first user-data database (AD-6) -- independent from CampusDatabase above,
        // its own migration path, genuinely empty at first launch (no createFromAsset).
        UserDatabase userDatabase = UserDatabase.getInstance(this);
        SettingsRepository settingsRepository = new SettingsRepositoryImpl(userDatabase.settingDao());
        getAccessibilityPreferenceUseCase = new GetAccessibilityPreferenceUseCase(settingsRepository);
        setAccessibilityPreferenceUseCase = new SetAccessibilityPreferenceUseCase(settingsRepository);

        // Exactly one instance, shared -- never re-instantiated per feature (AD-11).
        locationProvider = new AndroidLocationProvider(this);
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
}
