package za.ac.wits.campusnavigator.domain.repository;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;

/**
 * Repository seam between ViewModels/use cases and the data layer (ARCHITECTURE-SPINE.md
 * AD-1, AD-2). Defined here in :domain; implemented in :data against Room. ViewModels must
 * never bypass this to query Room directly.
 */
public interface BuildingRepository {

    /**
     * Returns every Building known for the current campus. Never null — an empty list
     * signals "no buildings available" rather than a failure; a real I/O failure surfaces
     * as an unchecked exception (unexpected/programmer-error condition, per AD-9), not a
     * Result type, since there is no meaningful "expected failure" case for this read.
     */
    List<Building> getAllBuildings();
}
