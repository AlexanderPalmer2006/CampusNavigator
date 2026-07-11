package za.ac.wits.campusnavigator.domain.repository;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Edge;
import za.ac.wits.campusnavigator.domain.model.Node;

/** Plain in-memory fake, shared across :domain tests -- mirrors FakeBuildingRepository. */
public final class FakeRoutingRepository implements RoutingRepository {

    private final List<Node> nodes;
    private final List<Edge> edges;

    public FakeRoutingRepository(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    @Override
    public List<Node> getAllNodes() {
        return nodes;
    }

    @Override
    public List<Edge> getAllEdges() {
        return edges;
    }
}
