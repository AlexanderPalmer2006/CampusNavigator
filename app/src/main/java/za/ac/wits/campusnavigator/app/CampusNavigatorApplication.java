package za.ac.wits.campusnavigator.app;

import android.app.Application;
import za.ac.wits.campusnavigator.data.local.CampusDatabase;
import za.ac.wits.campusnavigator.data.repository.BuildingRepositoryImpl;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.MapLibreInitializer;

/**
 * Manual DI composition root (ARCHITECTURE-SPINE.md AD-10) -- no Dagger/Hilt. The full
 * dependency graph is constructed here, by hand, in one file. Later stories add their own
 * wiring to this same class; keep it minimal, only what the current epic's stories need.
 */
public final class CampusNavigatorApplication extends Application implements HasGetBuildingsUseCase {

    private GetBuildingsUseCase getBuildingsUseCase;

    @Override
    public void onCreate() {
        super.onCreate();

        // Required before any MapView is created/inflated -- MapLibre throws
        // MapLibreConfigurationException otherwise.
        MapLibreInitializer.initialize(this);

        CampusDatabase database = CampusDatabase.getInstance(this);
        BuildingRepository buildingRepository = new BuildingRepositoryImpl(database.buildingDao());
        getBuildingsUseCase = new GetBuildingsUseCase(buildingRepository);
    }

    @Override
    public GetBuildingsUseCase getGetBuildingsUseCase() {
        return getBuildingsUseCase;
    }
}
