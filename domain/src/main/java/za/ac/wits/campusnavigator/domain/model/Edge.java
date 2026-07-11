package za.ac.wits.campusnavigator.domain.model;

/**
 * An undirected walkable connection between two {@link Node}s. Pure domain model -- no
 * Android or Room dependency, per ARCHITECTURE-SPINE.md AD-5. {@code stairs} is reserved
 * for Epic 3's AD-8 (accessible routing) -- read here, not yet acted on by this story's
 * routing logic. Story 2.2.
 */
public final class Edge {

    private final long id;
    private final long fromNodeId;
    private final long toNodeId;
    private final double distanceMeters;
    private final boolean stairs;

    public Edge(long id, long fromNodeId, long toNodeId, double distanceMeters, boolean stairs) {
        this.id = id;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.distanceMeters = distanceMeters;
        this.stairs = stairs;
    }

    public long getId() {
        return id;
    }

    public long getFromNodeId() {
        return fromNodeId;
    }

    public long getToNodeId() {
        return toNodeId;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public boolean isStairs() {
        return stairs;
    }
}
