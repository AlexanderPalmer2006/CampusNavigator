package za.ac.wits.campusnavigator.navigationengine;

/**
 * A walkable connection between two {@link GraphNode}s. Undirected -- one edge object
 * represents a walkable connection both ways, the router doesn't distinguish direction
 * (Story 2.2 Task 1).
 */
public final class GraphEdge {

    public final long fromNodeId;
    public final long toNodeId;
    public final double distanceMeters;

    public GraphEdge(long fromNodeId, long toNodeId, double distanceMeters) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.distanceMeters = distanceMeters;
    }
}
