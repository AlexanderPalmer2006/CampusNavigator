package za.ac.wits.campusnavigator.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;
import za.ac.wits.campusnavigator.domain.repository.FakeBuildingRepository;

/**
 * Mirrors {@link GetLandmarkPicksUseCaseTest}'s shape (Review Findings, 2026-07-12) -- this
 * use case's own Javadoc claims "same reasoning and shape as GetLandmarkPicksUseCase," which
 * should include matching test coverage, not just a matching implementation.
 */
public class GetCommonPickCategoriesUseCaseTest {

    @Test
    public void execute_returnsCuratedCategoryPickTags() {
        List<CategoryTag> curated = new ArrayList<>();
        curated.add(new CategoryTag(5L, "bathroom", true));
        curated.add(new CategoryTag(6L, "cafeteria", true));
        FakeBuildingRepository repository =
                new FakeBuildingRepository(Collections.emptyList(), Collections.emptyMap(), curated);
        GetCommonPickCategoriesUseCase useCase = new GetCommonPickCategoriesUseCase(repository);

        List<CategoryTag> result = useCase.execute();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(CategoryTag::isCommonPickCategory));
        assertTrue(result.stream().anyMatch(tag -> tag.getName().equals("bathroom")));
        assertTrue(result.stream().anyMatch(tag -> tag.getName().equals("cafeteria")));
    }

    @Test
    public void execute_returnsEmptyListNotNull_whenNoneCurated() {
        FakeBuildingRepository repository =
                new FakeBuildingRepository(Collections.emptyList(), Collections.emptyMap(), Collections.emptyList());
        GetCommonPickCategoriesUseCase useCase = new GetCommonPickCategoriesUseCase(repository);

        List<CategoryTag> result = useCase.execute();

        assertTrue(result.isEmpty());
    }
}
