package za.ac.wits.campusnavigator.ui.favourites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Collections;
import java.util.List;
import za.ac.wits.campusnavigator.domain.model.Building;
import za.ac.wits.campusnavigator.domain.model.FavouriteItem;
import za.ac.wits.campusnavigator.domain.result.Result;
import za.ac.wits.campusnavigator.ui.R;

/**
 * Renders the Favourites list (Story 5.1) -- a plain {@link BaseAdapter}/{@code ListView}
 * row per {@code item_favourite_row.xml} (DESIGN.md: hairline-divided list row, not the
 * card style Building search results/Common Pick tiles use). Same "tiny dataset, no
 * RecyclerView dependency" reasoning as every other adapter in this app.
 *
 * <p>Two states per row, driven by {@link FavouriteItem#getResolution()}: a resolved
 * {@link Result.Success} renders the real Building name/faculty and its main body is
 * tappable to navigate there; a {@link Result.Error} (AC 6, always
 * {@code BUILDING_NO_LONGER_EXISTS} here) renders the honest "This place is no longer
 * available" copy instead, with its main body tap a no-op (nothing to navigate to) -- but
 * the trailing unsave icon stays active either way, since {@link FavouriteItem#getBuildingId()}
 * is always available regardless of resolution outcome (AC 6: "not a broken row" means the
 * row must remain actionable, not just honestly labeled).</p>
 */
public final class FavouritesAdapter extends BaseAdapter {

    /** Fragment/host calls back into this when a row or its unsave icon is tapped. */
    public interface OnFavouriteInteractionListener {
        void onFavouriteRowTapped(Building building);

        void onUnsaveTapped(long buildingId);
    }

    private final Context context;
    private final OnFavouriteInteractionListener listener;
    private List<FavouriteItem> items = Collections.emptyList();

    public FavouritesAdapter(Context context, OnFavouriteInteractionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitItems(List<FavouriteItem> newItems) {
        items = newItems == null ? Collections.emptyList() : newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public FavouriteItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getBuildingId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_favourite_row, parent, false);
        }
        FavouriteItem item = items.get(position);

        View rowContent = view.findViewById(R.id.favouriteRowContent);
        TextView titleView = view.findViewById(R.id.favouriteRowTitle);
        TextView metaView = view.findViewById(R.id.favouriteRowMeta);
        TextView unsaveIcon = view.findViewById(R.id.favouriteUnsaveIcon);

        Result<Building> resolution = item.getResolution();
        if (resolution instanceof Result.Success) {
            Building building = ((Result.Success<Building>) resolution).getValue();
            String name = building.getName();
            titleView.setText(name);
            titleView.setTextColor(context.getResources().getColor(R.color.ink_primary, context.getTheme()));

            String faculty = building.getFacultyDepartment();
            if (faculty != null && !faculty.trim().isEmpty()) {
                metaView.setText(faculty);
                metaView.setVisibility(View.VISIBLE);
            } else {
                metaView.setVisibility(View.GONE);
            }

            rowContent.setContentDescription(context.getString(R.string.favourite_row_description, name));
            rowContent.setOnClickListener(v -> listener.onFavouriteRowTapped(building));
        } else {
            // AC 6: honest "no longer available" state -- distinct ink_secondary styling
            // (not ink_primary, DESIGN.md's ink-hierarchy convention) so a stale row reads
            // visually different from a real entry, not just via its text.
            titleView.setText(R.string.favourite_stale_building);
            titleView.setTextColor(context.getResources().getColor(R.color.ink_secondary, context.getTheme()));
            metaView.setVisibility(View.GONE);

            rowContent.setContentDescription(context.getString(R.string.favourite_stale_building));
            // Nothing to navigate to -- a no-op tap, not a crash on a null Building.
            rowContent.setOnClickListener(v -> { });
        }

        long buildingId = item.getBuildingId();
        unsaveIcon.setContentDescription(context.getString(R.string.favourite_unsave_description,
                resolution instanceof Result.Success
                        ? ((Result.Success<Building>) resolution).getValue().getName()
                        : context.getString(R.string.favourite_stale_building)));
        unsaveIcon.setOnClickListener(v -> listener.onUnsaveTapped(buildingId));

        return view;
    }
}
