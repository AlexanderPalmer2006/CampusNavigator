package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import za.ac.wits.campusnavigator.domain.repository.FakeFavouritesRepository;

/**
 * Code review fix (2026-07-13): {@code SaveFavouriteUseCase}/{@code RemoveFavouriteUseCase}/
 * {@code IsFavouriteUseCase} shipped with zero unit tests in Story 5.1 -- a dismissal citing
 * {@code GetAccessibilityPreferenceUseCase}/{@code SetAccessibilityPreferenceUseCase} as an
 * "also untested for the identical reason" precedent turned out to be factually wrong on
 * closer inspection: {@code AccessibilityPreferenceUseCaseTest} (Story 3.1) has covered
 * exactly this shape since it was introduced. This mirrors that test class's exact
 * structure for the Favourite-state trio instead of leaving the gap in place.
 */
public class FavouriteStateUseCaseTest {

    @Test
    public void isFavourite_defaultsToFalse_whenNeverSaved() {
        IsFavouriteUseCase useCase = new IsFavouriteUseCase(new FakeFavouritesRepository());

        assertFalse(useCase.execute(1L));
    }

    @Test
    public void save_thenIsFavourite_reflectsThePersistedValue() {
        FakeFavouritesRepository repository = new FakeFavouritesRepository();
        SaveFavouriteUseCase saveUseCase = new SaveFavouriteUseCase(repository);
        IsFavouriteUseCase isFavouriteUseCase = new IsFavouriteUseCase(repository);

        saveUseCase.execute(1L);

        assertTrue(isFavouriteUseCase.execute(1L));
    }

    @Test
    public void save_calledTwice_remainsFavourited() {
        FakeFavouritesRepository repository = new FakeFavouritesRepository();
        SaveFavouriteUseCase saveUseCase = new SaveFavouriteUseCase(repository);
        IsFavouriteUseCase isFavouriteUseCase = new IsFavouriteUseCase(repository);

        saveUseCase.execute(1L);
        saveUseCase.execute(1L);

        assertTrue(isFavouriteUseCase.execute(1L));
    }

    @Test
    public void remove_thenIsFavourite_reflectsTheRemoval() {
        FakeFavouritesRepository repository = new FakeFavouritesRepository(java.util.Collections.singletonList(1L));
        RemoveFavouriteUseCase removeUseCase = new RemoveFavouriteUseCase(repository);
        IsFavouriteUseCase isFavouriteUseCase = new IsFavouriteUseCase(repository);

        removeUseCase.execute(1L);

        assertFalse(isFavouriteUseCase.execute(1L));
    }

    @Test
    public void remove_whenNeverSaved_isANoOp() {
        FakeFavouritesRepository repository = new FakeFavouritesRepository();
        RemoveFavouriteUseCase removeUseCase = new RemoveFavouriteUseCase(repository);
        IsFavouriteUseCase isFavouriteUseCase = new IsFavouriteUseCase(repository);

        removeUseCase.execute(1L);

        assertFalse(isFavouriteUseCase.execute(1L));
    }
}
