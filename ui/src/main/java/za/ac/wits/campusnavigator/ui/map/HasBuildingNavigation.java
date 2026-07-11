package za.ac.wits.campusnavigator.ui.map;

/**
 * Implemented by the hosting Activity so any Fragment can navigate to the Building Info
 * Page without :ui depending on :app (ARCHITECTURE-SPINE.md AD-10). Story 2.1 Task 6.
 * Unlike bottom-nav tab switches, this destination is added to the back stack -- it's a
 * contextual, tap-through destination (EXPERIENCE.md IA), not a nav tab.
 */
public interface HasBuildingNavigation {
    void showBuildingInfo(long buildingId);
}
