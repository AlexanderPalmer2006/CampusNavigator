package za.ac.wits.campusnavigator.ui.buildinginfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.BuildingDetails;
import za.ac.wits.campusnavigator.domain.model.CategoryTag;
import za.ac.wits.campusnavigator.ui.R;
import za.ac.wits.campusnavigator.ui.map.HasComputeRouteUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetAccessibilityPreferenceUseCase;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingDetailsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModel;
import za.ac.wits.campusnavigator.ui.navigation.NavigationViewModelFactory;

/**
 * The Building Info Page (FR-5): name, faculty/department, category tags, and the photo
 * section only when one actually exists (AC 4 -- never a broken/placeholder image).
 * Reached contextually from search results or a map-pin tap, never a nav destination
 * itself (EXPERIENCE.md IA) -- see MainActivity.showBuildingInfo's back-stack handling.
 */
public final class BuildingInfoFragment extends Fragment {

    private static final String ARG_BUILDING_ID = "buildingId";

    @Nullable
    private BuildingDetails currentDetails;

    public static BuildingInfoFragment newInstance(long buildingId) {
        BuildingInfoFragment fragment = new BuildingInfoFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_BUILDING_ID, buildingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_building_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long buildingId = requireArguments().getLong(ARG_BUILDING_ID);
        HasGetBuildingDetailsUseCase provider = (HasGetBuildingDetailsUseCase) requireActivity();
        BuildingInfoViewModelFactory factory =
                new BuildingInfoViewModelFactory(provider.getGetBuildingDetailsUseCase(), buildingId);
        BuildingInfoViewModel viewModel = new ViewModelProvider(this, factory).get(BuildingInfoViewModel.class);

        HasComputeRouteUseCase computeRouteProvider = (HasComputeRouteUseCase) requireActivity();
        HasLocationProvider locationProviderHost = (HasLocationProvider) requireActivity();
        HasGetAccessibilityPreferenceUseCase accessibilityPreferenceProvider =
                (HasGetAccessibilityPreferenceUseCase) requireActivity();
        NavigationViewModelFactory navigationFactory = new NavigationViewModelFactory(
                computeRouteProvider.getComputeRouteUseCase(), locationProviderHost.getLocationProvider(),
                accessibilityPreferenceProvider.getGetAccessibilityPreferenceUseCase());
        // Activity-scoped: the same instance BuildingInfoFragment and (after popping back)
        // MapFragment both reach through requireActivity() -- a route started here must
        // survive the pop-back-stack to the Map that renders it (Story 2.2 Dev Notes:
        // "Resolved Design: NavigationViewModel (Activity-scoped) shape").
        NavigationViewModel navigationViewModel =
                new ViewModelProvider(requireActivity(), navigationFactory).get(NavigationViewModel.class);

        TextView nameView = view.findViewById(R.id.buildingName);
        TextView facultyView = view.findViewById(R.id.buildingFaculty);
        TextView categoryTagsView = view.findViewById(R.id.buildingCategoryTags);
        View photoSection = view.findViewById(R.id.photoSection);
        MaterialButton startNavigationButton = view.findViewById(R.id.startNavigationButton);

        startNavigationButton.setOnClickListener(v -> {
            if (currentDetails == null) {
                // Details haven't loaded yet (or failed to) -- nothing to navigate to.
                return;
            }
            boolean started = navigationViewModel.startNavigation(currentDetails.getBuilding());
            if (started) {
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                Snackbar.make(view, R.string.navigation_no_position_available, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getDetails().observe(getViewLifecycleOwner(), details -> {
            currentDetails = details;
            if (details == null) {
                // Lookup failed (or Building unexpectedly missing) -- degrade gracefully,
                // no crash (Story 1.1/1.2's established try/catch-and-degrade pattern).
                nameView.setText(R.string.building_info_load_failed);
                facultyView.setVisibility(View.GONE);
                categoryTagsView.setVisibility(View.GONE);
                photoSection.setVisibility(View.GONE);
                startNavigationButton.setVisibility(View.GONE);
                return;
            }
            startNavigationButton.setVisibility(View.VISIBLE);

            String name = details.getBuilding().getName();
            nameView.setText(name);
            nameView.setContentDescription(name);

            String faculty = details.getBuilding().getFacultyDepartment();
            if (faculty != null && !faculty.trim().isEmpty()) {
                facultyView.setText(faculty);
                facultyView.setVisibility(View.VISIBLE);
            } else {
                // Omitted entirely, not shown as an empty row (Task 5) -- a whitespace-only
                // value is treated the same as null/empty, not a blank visible row.
                facultyView.setVisibility(View.GONE);
            }

            categoryTagsView.setText(formatCategoryTags(details.getCategoryTags()));

            // AC 4: photo section omitted entirely (GONE, not an empty/broken image) when
            // no photo exists -- true for all 5 seed buildings right now (Task 1).
            photoSection.setVisibility(details.hasPhoto() ? View.VISIBLE : View.GONE);
        });
    }

    private String formatCategoryTags(List<CategoryTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return getString(R.string.building_info_no_category_tags);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(tags.get(i).getName());
        }
        return builder.toString();
    }
}
