package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.FavouriteItem;
import za.ac.wits.campusnavigator.domain.repository.FakeBuildingRepository;
import za.ac.wits.campusnavigator.domain.repository.FakeFavouritesRepository;
import za.ac.wits.campusnavigator.domain.result.Result;

public class GetFavouritesUseCaseTest {

    @Test
    public void execute_resolvesEveryFavouritedBuildingThatStillExists() {
        Building fnb = new Building(1L, "FNB Building", -26.1908, 28.0261, "wits-main", "FNB", "Accountancy", false);
        Building library = new Building(4L, "Central Library", -26.1922, 28.0302, "wits-main", "LIB", null, true);
        FakeBuildingRepository buildingRepository = new FakeBuildingRepository(Arrays.asList(fnb, library));
        FakeFavouritesRepository favouritesRepository = new FakeFavouritesRepository(Arrays.asList(1L, 4L));
        GetFavouritesUseCase useCase = new GetFavouritesUseCase(favouritesRepository, buildingRepository);

        List<FavouriteItem> items = useCase.execute();

        assertEquals(2, items.size());
        for (FavouriteItem item : items) {
            assertTrue(item.getResolution() instanceof Result.Success);
        }
        assertEquals(1L, items.get(0).getBuildingId());
        assertEquals(4L, items.get(1).getBuildingId());
    }

    @Test
    public void execute_surfacesStaleFavourite_asBuildingNoLongerExists_withoutHidingOthers() {
        Building library = new Building(4L, "Central Library", -26.1922, 28.0302, "wits-main", "LIB", null, true);
        // Building id 99 was favourited once but no longer exists in the bundled data.
        FakeBuildingRepository buildingRepository = new FakeBuildingRepository(Arrays.asList(library));
        FakeFavouritesRepository favouritesRepository = new FakeFavouritesRepository(Arrays.asList(99L, 4L));
        GetFavouritesUseCase useCase = new GetFavouritesUseCase(favouritesRepository, buildingRepository);

        List<FavouriteItem> items = useCase.execute();

        assertEquals(2, items.size());

        FavouriteItem staleItem = items.get(0);
        assertEquals(99L, staleItem.getBuildingId());
        assertTrue(staleItem.getResolution() instanceof Result.Error);
        assertEquals(Result.ErrorType.BUILDING_NO_LONGER_EXISTS,
                ((Result.Error<Building>) staleItem.getResolution()).getErrorType());

        FavouriteItem resolvedItem = items.get(1);
        assertEquals(4L, resolvedItem.getBuildingId());
        assertTrue(resolvedItem.getResolution() instanceof Result.Success);
    }

    @Test
    public void execute_returnsEmptyListNotNull_whenNoFavouritesSaved() {
        GetFavouritesUseCase useCase =
                new GetFavouritesUseCase(new FakeFavouritesRepository(), new FakeBuildingRepository(new ArrayList<>()));

        List<FavouriteItem> items = useCase.execute();

        assertTrue(items.isEmpty());
    }
}
