package za.ac.wits.campusnavigator.ui.map;

import android.content.Context;
import org.maplibre.android.MapLibre;
import org.maplibre.android.WellKnownTileServer;

/**
 * Encapsulates MapLibre's required one-time initialization so :app doesn't need to know
 * about MapLibre types directly -- Map Rendering is :ui's responsibility
 * (ARCHITECTURE-SPINE.md Design Paradigm), even though it's folded into :ui rather than a
 * separate module. Must be called (once, from Application.onCreate) before any MapView is
 * created/inflated, or MapView throws MapLibreConfigurationException.
 */
public final class MapLibreInitializer {

    private MapLibreInitializer() {
    }

    public static void initialize(Context context) {
        // No API key needed: the app never talks to a hosted MapLibre tile service, only
        // bundled local assets (ARCHITECTURE-SPINE.md AD-3, offline-first).
        MapLibre.getInstance(context, null, WellKnownTileServer.MapLibre);
    }
}
