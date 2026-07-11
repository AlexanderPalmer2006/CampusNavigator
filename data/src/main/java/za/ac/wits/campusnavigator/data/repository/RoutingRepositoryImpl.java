package za.ac.wits.campusnavigator.data.repository;

import java.util.ArrayList;
import java.util.List;
import za.ac.wits.campusnavigator.data.local.EdgeEntity;
import za.ac.wits.campusnavigator.data.local.NodeEntity;
import za.ac.wits.campusnavigator.data.local.RoutingDao;
import za.ac.wits.campusnavigator.domain.model.Edge;
import za.ac.wits.campusnavigator.domain.model.Node;
import za.ac.wits.campusnavigator.domain.repository.RoutingRepository;

/**
 * Implements the :domain-defined RoutingRepository against Room. Maps every Room entity
 * to its plain domain counterpart -- :domain never sees a Room type. Story 2.2.
 */
public final class RoutingRepositoryImpl implements RoutingRepository {

    private final RoutingDao routingDao;

    public RoutingRepositoryImpl(RoutingDao routingDao) {
        this.routingDao = routingDao;
    }

    @Override
    public List<Node> getAllNodes() {
        List<NodeEntity> entities = routingDao.getAllNodes();
        List<Node> nodes = new ArrayList<>(entities.size());
        for (NodeEntity entity : entities) {
            nodes.add(new Node(entity.id, entity.latitude, entity.longitude, entity.campusId, entity.levelId));
        }
        return nodes;
    }

    @Override
    public List<Edge> getAllEdges() {
        List<EdgeEntity> entities = routingDao.getAllEdges();
        List<Edge> edges = new ArrayList<>(entities.size());
        for (EdgeEntity entity : entities) {
            edges.add(new Edge(entity.id, entity.fromNodeId, entity.toNodeId, entity.distanceMeters, entity.isStairs));
        }
        return edges;
    }
}
