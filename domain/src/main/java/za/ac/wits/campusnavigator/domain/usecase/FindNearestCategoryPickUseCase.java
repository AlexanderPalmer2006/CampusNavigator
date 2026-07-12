package za.ac.wits.campusnavigator.domain.usecase;

import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.Position;
import za.ac.wits.campusnavigator.domain.model.Route;
import za.ac.wits.campusnavigator.domain.repository.BuildingRepository;
import za.ac.wits.campusnavigator.domain.result.Result;

/**
 * Resolves a Category Pick (e.g. "bathroom") to the single nearest Building carrying that
 * category tag, by real walking distance (FR-8, Story 4.2, ARCHITECTURE-SPINE.md AD-7) --
 * never straight-line proximity. Composes {@link ComputeRouteUseCase} the same way
 * {@code NavigationSession} already does (both are {@code :domain} classes; composing one
 * use case from another is an established pattern, not a new one).
 *
 * <p><b>Deliberate design decision:</b> this use case returns
 * {@link Result.ErrorType#NO_CATEGORY_MATCH} both when zero Buildings carry the requested
 * category tag <i>and</i> when one or more do but none is reachable by any computed route
 * (e.g. a disconnected sub-graph). The epics.md acceptance criterion only literally
 * describes the first case, but the two present identically to the user -- the same
 * honest "none found nearby" failure copy -- so introducing a second {@code ErrorType} to
 * distinguish a cause the UI never actually surfaces differently would violate AD-9's
 * "closed set, extend only for a genuinely new failure mode" discipline.</p>
 */
public final class FindNearestCategoryPickUseCase {

    private final BuildingRepository buildingRepository;
    private final ComputeRouteUseCase computeRouteUseCase;

    public FindNearestCategoryPickUseCase(BuildingRepository buildingRepository, ComputeRouteUseCase computeRouteUseCase) {
        this.buildingRepository = buildingRepository;
        this.computeRouteUseCase = computeRouteUseCase;
    }

    public Result<Building> execute(Position currentPosition, String categoryName, boolean avoidStairs) {
        List<Building> candidates = buildingRepository.getBuildingsByCategory(categoryName);
        if (candidates.isEmpty()) {
            return Result.error(Result.ErrorType.NO_CATEGORY_MATCH);
        }

        Building nearest = null;
        double nearestDistanceMeters = Double.MAX_VALUE;
        for (Building candidate : candidates) {
            Result<Route> routeResult = computeRouteUseCase.execute(currentPosition, candidate, avoidStairs);
            if (routeResult instanceof Result.Success) {
                double distanceMeters = ((Result.Success<Route>) routeResult).getValue().getDistanceMeters();
                if (distanceMeters < nearestDistanceMeters) {
                    nearestDistanceMeters = distanceMeters;
                    nearest = candidate;
                }
            }
        }

        if (nearest == null) {
            // Every candidate exists but none is reachable -- same user-facing outcome as
            // "no candidates at all," see the class Javadoc for why this isn't split into
            // a second ErrorType.
            return Result.error(Result.ErrorType.NO_CATEGORY_MATCH);
        }
        return Result.success(nearest);
    }
}
