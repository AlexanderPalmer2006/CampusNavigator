package za.ac.wits.campusnavigator.ui.map;

/**
 * Implemented by the hosting Activity so any Fragment can switch the bottom-nav selection
 * back to the Map tab without :ui depending on :app or doing its own FragmentTransaction
 * (ARCHITECTURE-SPINE.md AD-10). Story 4.1: tapping a Common Pick tile starts navigation and
 * must land the user on the Map, where the route actually renders -- reuses MainActivity's
 * existing bottom-nav selection logic (including its selectedNavId bookkeeping and its
 * already-selected no-op guard) rather than a Fragment performing its own transaction.
 */
public interface HasBottomNavigation {
    void selectMapTab();
}
