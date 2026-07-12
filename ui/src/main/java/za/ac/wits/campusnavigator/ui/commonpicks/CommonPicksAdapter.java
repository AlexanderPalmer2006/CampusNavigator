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
import za.ac.wits.campusnavigator.domain.model.CategoryTag;
import za.ac.wits.campusnavigator.ui.R;

/**
 * Renders both Landmark and Category Pick tiles in the Common Picks tab's 2-column grid
 * (DESIGN.md Common Pick tile, mockups/common-picks.html) -- both kinds share the exact
 * same {@code item_common_pick_tile.xml} layout, unmodified, distinguished only by the
 * icon glyph/label/kind-caption text {@link CommonPickTile} resolves (DESIGN.md: "icon is
 * the only differentiator... never color"). Plain {@link BaseAdapter}/{@code GridView}
 * rather than RecyclerView -- same reasoning as Story 2.1's {@code BuildingSearchAdapter}:
 * a tiny dataset, no new Gradle dependency needed.
 *
 * <p>Story 4.1 built this for Landmark Picks only; Story 4.2 extends it for Category
 * Picks by generalizing {@code List<Building>} to {@code List<CommonPickTile>}, per that
 * story's own Dev Notes anticipating exactly this extension.</p>
 */
public final class CommonPicksAdapter extends BaseAdapter {

    /** Fragment/host calls back into this when a tile is tapped. */
    public interface OnPickTappedListener {
        void onLandmarkPickTapped(Building building);

        void onCategoryPickTapped(CategoryTag category);
    }

    private final Context context;
    private final OnPickTappedListener listener;
    private List<CommonPickTile> picks = Collections.emptyList();

    public CommonPicksAdapter(Context context, OnPickTappedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitPicks(List<CommonPickTile> newPicks) {
        picks = newPicks == null ? Collections.emptyList() : newPicks;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return picks.size();
    }

    @Override
    public CommonPickTile getItem(int position) {
        return picks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_common_pick_tile, parent, false);
        }
        CommonPickTile tile = picks.get(position);

        TextView iconView = view.findViewById(R.id.pickIcon);
        TextView labelView = view.findViewById(R.id.pickLabel);
        TextView kindView = view.findViewById(R.id.pickKind);

        iconView.setText(tile.getIcon());
        labelView.setText(tile.getLabel());
        kindView.setText(tile.getKindCaption());

        view.setContentDescription(
                context.getString(R.string.common_pick_tile_description, tile.getLabel(), tile.getKindCaption()));

        if (tile.getKind() == CommonPickTile.Kind.LANDMARK) {
            view.setOnClickListener(v -> listener.onLandmarkPickTapped(tile.getBuilding()));
        } else {
            view.setOnClickListener(v -> listener.onCategoryPickTapped(tile.getCategory()));
        }

        return view;
    }
}
