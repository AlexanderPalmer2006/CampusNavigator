package za.ac.wits.campusnavigator.data.local;

import androidx.room.Dao;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RoutingDao {

    @Query("SELECT * FROM Node")
    List<NodeEntity> getAllNodes();

    @Query("SELECT * FROM Edge")
    List<EdgeEntity> getAllEdges();
}
