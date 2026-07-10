package za.ac.wits.campusnavigator.app;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import za.ac.wits.campusnavigator.domain.location.LocationProvider;
import za.ac.wits.campusnavigator.domain.usecase.GetBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.common.PlaceholderFragment;
import za.ac.wits.campusnavigator.ui.map.HasGetBuildingsUseCase;
import za.ac.wits.campusnavigator.ui.map.HasLocationProvider;
import za.ac.wits.campusnavigator.ui.map.MapFragment;

/**
 * Hosts the 4-tab bottom navigation shell (EXPERIENCE.md Information Architecture, Story
 * 1.1 AC 3). Map has real content; Common Picks/Favourites/Settings show a placeholder
 * until their own epics fill them in.
 */
public final class MainActivity extends AppCompatActivity
        implements HasGetBuildingsUseCase, HasLocationProvider {

    private int selectedNavId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            selectedNavId = R.id.nav_map;
            showFragment(new MapFragment());
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedNavId) {
                return true;
            }
            if (id == R.id.nav_map) {
                selectedNavId = id;
                showFragment(new MapFragment());
                return true;
            } else if (id == R.id.nav_common_picks
                    || id == R.id.nav_favourites
                    || id == R.id.nav_settings) {
                selectedNavId = id;
                showFragment(new PlaceholderFragment());
                return true;
            }
            return false;
        });
    }

    private void showFragment(@NonNull Fragment fragment) {
        if (getSupportFragmentManager().isStateSaved()) {
            // A nav tap raced onSaveInstanceState (e.g. the app is backgrounding) --
            // commit() would throw IllegalStateException here.
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public GetBuildingsUseCase getGetBuildingsUseCase() {
        return ((CampusNavigatorApplication) getApplication()).getGetBuildingsUseCase();
    }

    @Override
    public LocationProvider getLocationProvider() {
        return ((CampusNavigatorApplication) getApplication()).getLocationProvider();
    }
}
