#!/usr/bin/env python3
"""Story 6.1: convert raw/wits_terrain.json into real Mapbox Vector Tile (MVT) tiles.

Output: ../../ui/src/main/assets/tiles/{z}/{x}/{y}.pbf -- the "bundled offline
vector-tile source, committed as a build-time asset" epics.md's Story 6.1 AC1 asks for.
Story 6.2 wires this into style.json as a *declared* source (the low-risk, standard
MapLibre pipeline per the rendering-risk decision in epic-6-scoping-2026-07-12.md); this
script only produces the tile files themselves, no app/style.json change.

## Why hand-rolled, not `tippecanoe`

`tippecanoe` (the standard tool for this) and `gdal`/`ogr2ogr` are both unavailable in
this sandboxed environment (checked: not on PATH, not installable via `apt` without a
package cache/sudo path that exists here). `pip install mapbox-vector-tile mercantile
shapely` succeeded and provides the same MVT protobuf encoding tippecanoe itself
produces, just without tippecanoe's tile-generation orchestration -- reimplemented here
directly since the tile pyramid is tiny (max 157 tiles, zoom 14-18, over a ~1.4km bbox,
not planet-scale).

## Two layers, matching Story 6.2 AC1's "streets and terrain"

- `roads`: every `highway=*` way as a LineString, tagged with its highway value as
  `class` (so Story 6.2's style.json can style `secondary`/`footway`/`service`/etc.
  differently, same way any real vector basemap does).
- `landcover`: closed-ring `landuse=*`/`natural=*`/`leisure=*` ways as Polygons (open
  rings -- e.g. a `natural=tree_row` line -- are skipped for this layer; they're not
  fill-able area data). Provides the "terrain" half of AC1 (grass, water, sports pitches,
  etc.) as color-differentiable ground cover, distinct from Story 6.3's later
  Building-scoped footprint fills.

Zoom range 14-18 (bracketing `MapFragment.DEFAULT_ZOOM = 16`). MapLibre's standard
vector-tile pipeline over/under-zooms the nearest available tile outside this range, so
panning/zooming past it degrades resolution rather than breaking (standard MVT client
behavior, not something this script needs to handle).
"""
import json
import os

import mercantile
from shapely.geometry import LineString, Polygon, box, mapping
from shapely.ops import transform as shapely_transform
import mapbox_vector_tile

RAW_DIR = os.path.join(os.path.dirname(__file__), "raw")
TILES_DIR = os.path.join(os.path.dirname(__file__), "..", "..", "ui", "src", "main", "assets", "tiles")

MIN_ZOOM = 14
MAX_ZOOM = 18


def load_features():
    with open(os.path.join(RAW_DIR, "wits_terrain.json")) as f:
        data = json.load(f)

    roads = []
    landcover = []
    for el in data["elements"]:
        if el["type"] != "way" or not el.get("geometry"):
            continue
        tags = el.get("tags", {})
        coords = [(pt["lon"], pt["lat"]) for pt in el["geometry"] if pt]
        if len(coords) < 2:
            continue

        if "highway" in tags:
            try:
                geom = LineString(coords)
            except Exception:
                continue
            if geom.is_valid and not geom.is_empty:
                roads.append((geom, {"class": tags["highway"]}))

        landcover_kind = tags.get("landuse") or tags.get("natural") or tags.get("leisure")
        if landcover_kind and coords[0] == coords[-1] and len(coords) >= 4:
            try:
                geom = Polygon(coords)
            except Exception:
                continue
            if geom.is_valid and not geom.is_empty:
                landcover.append((geom, {"kind": landcover_kind}))

    return roads, landcover


def clip_to_tile(geoms, tile_box):
    """Intersect every (geometry, props) pair with tile_box; drop empty/non-intersecting
    results. Returns a list of (clipped_geometry, props)."""
    out = []
    for geom, props in geoms:
        if not geom.intersects(tile_box):
            continue
        clipped = geom.intersection(tile_box)
        if clipped.is_empty:
            continue
        out.append((clipped, props))
    return out


def to_mvt_features(clipped):
    features = []
    for geom, props in clipped:
        features.append({"geometry": mapping(geom), "properties": props})
    return features


def main():
    roads, landcover = load_features()
    print(f"Loaded {len(roads)} road ways, {len(landcover)} landcover polygons")

    # Defensive: computed across whichever layer(s) actually have data. A future re-run
    # against a different bbox/extract could plausibly have zero landcover polygons (or,
    # less likely, zero roads) -- unconditionally concatenating both would then raise
    # ValueError: min() arg is an empty sequence. Not reachable with today's extract (68
    # landcover polygons present), but this script's whole purpose (per README.md) is to
    # be reproducible against a *future* extract, not just today's.
    road_lons = [c[0] for geom, _ in roads for c in geom.coords]
    road_lats = [c[1] for geom, _ in roads for c in geom.coords]
    landcover_lons = [c[0] for geom, _ in landcover for c in geom.exterior.coords]
    landcover_lats = [c[1] for geom, _ in landcover for c in geom.exterior.coords]
    all_lons = road_lons + landcover_lons
    all_lats = road_lats + landcover_lats
    if not all_lons or not all_lats:
        raise SystemExit("No road or landcover geometry loaded -- check raw/wits_terrain.json.")
    west, south, east, north = min(all_lons), min(all_lats), max(all_lons), max(all_lats)

    os.makedirs(TILES_DIR, exist_ok=True)
    total_written = 0
    total_skipped_empty = 0

    for zoom in range(MIN_ZOOM, MAX_ZOOM + 1):
        tiles = list(mercantile.tiles(west, south, east, north, [zoom]))
        for t in tiles:
            b = mercantile.bounds(t)  # LngLatBbox(west, south, east, north)
            tile_box = box(b.west, b.south, b.east, b.north)

            clipped_roads = clip_to_tile(roads, tile_box)
            clipped_landcover = clip_to_tile(landcover, tile_box)
            if not clipped_roads and not clipped_landcover:
                total_skipped_empty += 1
                continue

            layers = []
            if clipped_roads:
                layers.append({"name": "roads", "features": to_mvt_features(clipped_roads)})
            if clipped_landcover:
                layers.append({"name": "landcover", "features": to_mvt_features(clipped_landcover)})

            pbf = mapbox_vector_tile.encode(
                layers,
                default_options={
                    "quantize_bounds": (b.west, b.south, b.east, b.north),
                    "on_invalid_geometry": mapbox_vector_tile.encoder.on_invalid_geometry_make_valid,
                },
            )

            out_dir = os.path.join(TILES_DIR, str(t.z), str(t.x))
            os.makedirs(out_dir, exist_ok=True)
            out_path = os.path.join(out_dir, f"{t.y}.pbf")
            with open(out_path, "wb") as f:
                f.write(pbf)
            total_written += 1

    print(f"Wrote {total_written} tile files to {os.path.abspath(TILES_DIR)}")
    print(f"Skipped {total_skipped_empty} empty tiles (no road/landcover data in that tile)")


if __name__ == "__main__":
    main()
