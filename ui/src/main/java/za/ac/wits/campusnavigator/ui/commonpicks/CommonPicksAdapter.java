package za.ac.wits.campusnavigator.ui.commonpicks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Collections;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.ui.R;

/**
 * Renders Landmark Pick tiles in the Common Picks tab's 2-column grid (DESIGN.md Common
 * Pick tile, mockups/common-picks.html). Plain {@link BaseAdapter}/{@code GridView} rather
 * than RecyclerView -- same reasoning as Story 2.1's {@code BuildingSearchAdapter}: a tiny
 * dataset (today's seed data has 3 Landmark Picks), no new Gradle dependency needed.
 *
 * <p>Story 4.1 scope only: every tile here is a Landmark Pick (a {@link Building}). Story
 * 4.2 will extend this adapter for real when Category Picks exist -- deliberately not
 * built as a speculative polymorphic "pick item" abstraction now (mirrors NavigationSession's
 * own Story 2.2-then-3.1 precedent of extending a working single-purpose class rather than
 * pre-stubbing for a not-yet-built second case).</p>
 */
public final class CommonPicksAdapter extends BaseAdapter {

    /** Fragment/host calls back into this when a tile is tapped. */
    public interface OnPickTappedListener {
        void onLandmarkPickTapped(Building building);
    }

    private final Context context;
    private final OnPickTappedListener listener;
    private List<Building> landmarkPicks = Collections.emptyList();

    public CommonPicksAdapter(Context context, OnPickTappedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitLandmarkPicks(List<Building> newLandmarkPicks) {
        landmarkPicks = newLandmarkPicks == null ? Collections.emptyList() : newLandmarkPicks;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return landmarkPicks.size();
    }

    @Override
    public Building getItem(int position) {
        return landmarkPicks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return landmarkPicks.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_common_pick_tile, parent, false);
        }
        Building building = landmarkPicks.get(position);

        TextView iconView = view.findViewById(R.id.pickIcon);
        TextView labelView = view.findViewById(R.id.pickLabel);
        TextView kindView = view.findViewById(R.id.pickKind);

        // DESIGN.md: "Distinguish Landmark vs. Category Picks by icon only -- never color."
        // Every tile is a Landmark Pick this story (Story 4.2 adds the category glyphs).
        iconView.setText("📍");
        labelView.setText(building.getName());
        String kind = context.getString(R.string.common_pick_landmark_kind);
        kindView.setText(kind);

        view.setContentDescription(
                context.getString(R.string.common_pick_tile_description, building.getName(), kind));
        view.setOnClickListener(v -> listener.onLandmarkPickTapped(building));

        return view;
    }
}
