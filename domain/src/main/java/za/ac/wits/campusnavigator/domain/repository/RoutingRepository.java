package za.ac.wits.campusnavigator.domain.repository;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Edge;
import za.ac.wits.campusnavigator.domain.model.Node;

/**
 * Repository seam for the walkway routing graph (ARCHITECTURE-SPINE.md AD-1, AD-2).
 * Defined here in :domain; implemented in :data against Room. Story 2.2.
 */
public interface RoutingRepository {

    /** Never null -- an empty list signals "no graph data," a real I/O failure throws. */
    List<Node> getAllNodes();

    /** Never null -- an empty list signals "no graph data," a real I/O failure throws. */
    List<Edge> getAllEdges();
}
