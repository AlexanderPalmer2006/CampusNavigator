package za.ac.wits.campusnavigator.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Collections;
import java.util.List;
import za.ac.wits.campusnavigator.domain.search.BuildingSearchResult;
import za.ac.wits.campusnavigator.ui.R;

/**
 * Renders search suggestions as Building cards (DESIGN.md) in the Map surface's suggestions
 * {@code ListView} (Story 2.1 Task 4). A plain {@link BaseAdapter}/{@code ListView} rather
 * than RecyclerView -- the dataset is tiny (top 5 fuzzy results max) and this avoids adding
 * a new Gradle dependency, consistent with this project's established minimal-dependency
 * posture (no Play Services, no GraphHopper, hand-rolled Levenshtein).
 */
public final class BuildingSearchAdapter extends BaseAdapter {

    /** Fragment/host calls back into this when a suggestion row is tapped. */
    public interface OnResultTappedListener {
        void onResultTapped(BuildingSearchResult result);
    }

    private final Context context;
    private final OnResultTappedListener listener;
    private List<BuildingSearchResult> results = Collections.emptyList();

    public BuildingSearchAdapter(Context context, OnResultTappedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitResults(List<BuildingSearchResult> newResults) {
        results = newResults == null ? Collections.emptyList() : newResults;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public BuildingSearchResult getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return results.get(position).getBuilding().getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_building_search_result, parent, false);
        }
        BuildingSearchResult result = results.get(position);

        String name = result.getBuilding().getName();
        TextView nameView = view.findViewById(R.id.resultName);
        nameView.setText(name);

        TextView metaView = view.findViewById(R.id.resultMeta);
        String metaText = buildMetaText(result);
        if (metaText != null) {
            metaView.setText(metaText);
            metaView.setVisibility(View.VISIBLE);
        } else {
            metaView.setVisibility(View.GONE);
        }

        view.setContentDescription(metaText == null ? name : name + ", " + metaText);
        view.setOnClickListener(v -> listener.onResultTapped(result));

        return view;
    }

    private String buildMetaText(BuildingSearchResult result) {
        // AC 3: Floor Hint takes priority over the ordinary faculty/code meta line when
        // this result came from an exact Code+Room-Token resolution.
        if (result.getFloorHint() != null && result.getRoomToken() != null) {
            return context.getString(R.string.search_result_floor_hint, result.getRoomToken(), result.getFloorHint());
        }
        String faculty = result.getBuilding().getFacultyDepartment();
        if (faculty != null && !faculty.isEmpty()) {
            return faculty;
        }
        String code = result.getBuilding().getCode();
        if (code != null && !code.isEmpty()) {
            return code;
        }
        return null;
    }
}
