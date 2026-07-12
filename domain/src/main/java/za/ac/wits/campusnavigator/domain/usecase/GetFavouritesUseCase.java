package za.ac.wits.campusnavigator.domain.usecase;

import java.util.ArrayList;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.FavouriteItem;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;
import za.ac.wits.campusnavigator.domain.repository.FavouritesRepository;
import za.ac.wits.campusnavigator.domain.result.Result;

/**
 * Loads the Favourites list, reconciling each saved building id against the bundled
 * campus data (FR-9, Story 5.1, ARCHITECTURE-SPINE.md AD-6) -- "a lazy, on-read check...
 * performed when the Favourites list is loaded, not as a background job at every launch."
 * There is no other trigger for this reconciliation anywhere in the app, deliberately.
 *
 * <p>Composes {@link FavouritesRepository} (the user-data database) with
 * {@link BuildingRepository} (the bundled campus database) -- the two-database split
 * (AD-6) means neither repository may depend on the other directly; this cross-database
 * join is a use-case concern, the same "compose two repositories inside a use case"
 * pattern {@code FindNearestCategoryPickUseCase} already established (Story 4.2).</p>
 */
public final class GetFavouritesUseCase {

    private final FavouritesRepository favouritesRepository;
    private final BuildingRepository buildingRepository;

    public GetFavouritesUseCase(FavouritesRepository favouritesRepository, BuildingRepository buildingRepository) {
        this.favouritesRepository = favouritesRepository;
        this.buildingRepository = buildingRepository;
    }

    public List<FavouriteItem> execute() {
        List<Long> buildingIds = favouritesRepository.getFavouriteBuildingIds();
        List<FavouriteItem> items = new ArrayList<>(buildingIds.size());
        for (long buildingId : buildingIds) {
            Building building = buildingRepository.getBuildingById(buildingId);
            Result<Building> resolution = building == null
                    ? Result.error(Result.ErrorType.BUILDING_NO_LONGER_EXISTS)
                    : Result.success(building);
            items.add(new FavouriteItem(buildingId, resolution));
        }
        return items;
    }
}
