package za.ac.wits.campusnavigator.app;

import android.app.Application;
import za.ac.wits.campusnavigator.data.local.CampusDatabase;
import za.ac.wits.campusnavigator.data.repository.BuildingRepositoryImpl;
import za.ac.wits.campusnavigator.data.repository.RoutingRepositoryImpl;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;
import za.ac.wits.campusnavigator.domain.repository.RoutingRepository;
import za.ac.wits.campusnavigator.domain.search.SearchBuildingsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.ComputeRouteUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.location.AndroidLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.map.HasSearchBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.MapLibreInitializer;

/**
 * Manual DI composition root (ARCHITECTURE-SPINE.md AD-10) -- no Dagger/Hilt. The full
 * dependency graph is constructed here, by hand, in one file. Later stories add their own
 * wiring to this same class; keep it minimal, only what the current epic's stories need.
 */
public final class CampusNavigatorApplication extends Application
        implements HasGetBuildingsUseCase, HasLocationProvider, HasSearchBuildingsUseCase,
        HasGetBuildingDetailsUseCase, HasComputeRouteUseCase {

    private GetBuildingsUseCase getBuildingsUseCase;
    private SearchBuildingsUseCase searchBuildingsUseCase;
    private GetBuildingDetailsUseCase getBuildingDetailsUseCase;
    private ComputeRouteUseCase computeRouteUseCase;
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

        RoutingRepository routingRepository = new RoutingRepositoryImpl(database.routingDao());
        computeRouteUseCase = new ComputeRouteUseCase(routingRepository);

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
}
