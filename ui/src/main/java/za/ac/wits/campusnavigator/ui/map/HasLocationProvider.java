package za.ac.wits.campusnavigator.ui.map;

import za.ac.wits.campusnavigator.domain.location.LocationProvider;

/**
 * Implemented by the hosting Activity (in :app) so MapFragment can obtain the shared
 * LocationProvider without :ui depending on :app or on any DI framework
 * (ARCHITECTURE-SPINE.md AD-10: manual DI via a composition root). Same seam shape as
 * HasGetBuildingsUseCase (Story 1.1).
 */
public interface HasLocationProvider {
    LocationProvider getLocationProvider();
}
