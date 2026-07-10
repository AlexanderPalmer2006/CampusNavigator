package za.ac.wits.campusnavigator.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import za.ac.wits.campusnavigator.ui.R;

/**
 * Stub content for Common Picks, Favourites, and Settings tabs (Story 1.1, AC 3) --
 * their real content is filled in by Epics 4, 5, and 3 respectively.
 */
public final class PlaceholderFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }
}
