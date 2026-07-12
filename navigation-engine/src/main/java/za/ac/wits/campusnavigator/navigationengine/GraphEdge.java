package za.ac.wits.campusnavigator.navigationengine;

/**
 * A walkable connection between two {@link GraphNode}s. Undirected -- one edge object
 * represents a walkable connection both ways, the router doesn't distinguish direction
 * (Story 2.2 Task 1). {@code isStairs} (Story 3.1, AD-8) marks a stair segment -- when
 * accessible routing is requested, edges with this flag are excluded from the search
 * space entirely (impassable), not down-weighted.
 */
public final class GraphEdge {

    public final long fromNodeId;
    public final long toNodeId;
    public final double distanceMeters;
    public final boolean isStairs;

    public GraphEdge(long fromNodeId, long toNodeId, double distanceMeters, boolean isStairs) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.distanceMeters = distanceMeters;
        this.isStairs = isStairs;
    }
}
