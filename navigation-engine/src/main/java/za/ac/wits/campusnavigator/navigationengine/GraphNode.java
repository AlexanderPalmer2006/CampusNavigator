package za.ac.wits.campusnavigator.navigationengine;

/**
 * A routing-graph vertex. Deliberately distinct from :data's Room entities and
 * :domain's domain models -- a small, self-contained algorithm-library vocabulary that
 * :domain maps into (Story 2.2 Task 1).
 */
public final class GraphNode {

    public final long id;
    public final double latitude;
    public final double longitude;

    public GraphNode(long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
