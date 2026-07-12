package za.ac.wits.campusnavigator.data.repository;

import java.util.List;
import za.ac.wits.campusnavigator.data.local.FavouriteDao;
import za.ac.wits.campusnavigator.data.local.FavouriteEntity;
import za.ac.wits.campusnavigator.domain.repository.FavouritesRepository;

/**
 * Implements the :domain-defined FavouritesRepository against Room's user-data database.
 * Same thin-wrapper shape as SettingsRepositoryImpl. Story 5.1.
 */
public final class FavouritesRepositoryImpl implements FavouritesRepository {

    private final FavouriteDao favouriteDao;

    public FavouritesRepositoryImpl(FavouriteDao favouriteDao) {
        this.favouriteDao = favouriteDao;
    }

    @Override
    public void saveFavourite(long buildingId) {
        FavouriteEntity entity = new FavouriteEntity();
        entity.buildingId = buildingId;
        favouriteDao.insert(entity);
    }

    @Override
    public void removeFavourite(long buildingId) {
        favouriteDao.delete(buildingId);
    }

    @Override
    public boolean isFavourite(long buildingId) {
        return favouriteDao.exists(buildingId);
    }

    @Override
    public List<Long> getFavouriteBuildingIds() {
        return favouriteDao.getAllBuildingIds();
    }
}
