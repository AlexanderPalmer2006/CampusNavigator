package za.ac.wits.campusnavigator.domain.repository;

import java.util.List;

/**
 * Repository seam for saved Buildings (Story 5.1, ARCHITECTURE-SPINE.md AD-1, AD-2, AD-6).
 * Defined here in {@code :domain}; implemented in {@code :data} against the user-data
 * database's {@code FavouriteEntry} table only. Deliberately returns plain {@code long}
 * building ids, never {@code Building} objects -- this repository has no access to
 * {@code BuildingRepository}/{@code CampusDatabase} (the two-database split, AD-6) and
 * must not reach across that boundary. Reconciling an id against a real Building is a
 * use-case concern ({@code GetFavouritesUseCase}), not this repository's job.
 */
public interface FavouritesRepository {

    /** No-op if {@code buildingId} is already favourited -- never a duplicate entry. */
    void saveFavourite(long buildingId);

    /** No-op if {@code buildingId} isn't currently favourited. */
    void removeFavourite(long buildingId);

    boolean isFavourite(long buildingId);

    /** Never null -- an empty list means no Favourites saved (AC 5), not a failure. */
    List<Long> getFavouriteBuildingIds();
}
