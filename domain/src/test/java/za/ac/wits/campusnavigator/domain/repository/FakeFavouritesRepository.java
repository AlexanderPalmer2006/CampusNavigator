package za.ac.wits.campusnavigator.domain.repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Plain in-memory fake, shared across :domain tests -- mirrors FakeBuildingRepository. */
public final class FakeFavouritesRepository implements FavouritesRepository {

    // LinkedHashSet: insertion order, deterministic test assertions -- same reasoning
    // Story 4.2's ORDER BY fix gave the real Room queries.
    private final Set<Long> favouriteBuildingIds = new LinkedHashSet<>();

    public FakeFavouritesRepository() {
    }

    public FakeFavouritesRepository(List<Long> initialFavouriteBuildingIds) {
        favouriteBuildingIds.addAll(initialFavouriteBuildingIds);
    }

    @Override
    public void saveFavourite(long buildingId) {
        favouriteBuildingIds.add(buildingId);
    }

    @Override
    public void removeFavourite(long buildingId) {
        favouriteBuildingIds.remove(buildingId);
    }

    @Override
    public boolean isFavourite(long buildingId) {
        return favouriteBuildingIds.contains(buildingId);
    }

    @Override
    public List<Long> getFavouriteBuildingIds() {
        return new ArrayList<>(favouriteBuildingIds);
    }
}
