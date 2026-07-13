#!/usr/bin/env python3
"""Story 6.1: convert raw/wits_buildings.json into a per-Building footprint GeoJSON set.

Output: output/building_footprints.geojson -- ready for Story 6.3 to seed into its new
BUILDING_FOOTPRINT entity (which does not exist yet; this story does not touch it or
campus.db's schema). Not an app-bundled asset itself -- a dev-time authoring artifact,
same role as node_edge_reseed.sql.

## Matching seed Buildings to real OSM buildings

The 5 existing seed Buildings (campus.db, Story 1.1-4.2) use plausible-sounding real Wits
landmark names but fictional, hand-placed coordinates (confirmed: the old "Great Hall"
seed coordinate is 247m from the real OSM-mapped Great Hall building; nearest-OSM-building
matching by raw coordinate proximity would have matched several seed Buildings to the
WRONG real building, e.g. the old "Great Hall" seed coordinate's nearest real OSM building
is actually "Yale-Columbia Southern Observatory", 11m away -- a real building, just not
Great Hall). Proximity-only matching was tried first and rejected for exactly this reason
(see Dev Notes in the story file for the full before/after table).

Matching is instead done by name, cross-checked against real-world knowledge of the
actual Wits campus, not by coordinate proximity:

- **Great Hall** -> OSM way 452712704, name "Great Hall" -- exact name match.
- **Origins Centre** -> OSM way 739521835, name "Origins Centre" -- exact name match.
- **Central Library** -> OSM way 26327942, name "William Cullen library" -- not a string
  match, a real-world-knowledge match: William Cullen Library is Wits' actual historic
  main/central library (on Central Block, East Campus) -- this is the real building the
  seed data's "Central Library" was always describing, just under its full proper name.
- **FNB Building (Accountancy)** -> no confident match. No OSM building tagged with any
  name containing "FNB", "Accountancy", "Commerce", or similar in this extract.
- **Robert Sobukwe Block** -> no confident match. No OSM building tagged with any name
  containing "Sobukwe" in this extract.

3 of 5 seed Buildings get real footprint polygons. The other 2 get none -- and that's a
correctly-handled case, not a gap: Story 6.3 AC3 explicitly requires "a Building with no
footprint data renders with no fill and no crash," matching the existing BuildingPhoto
"omit entirely, no placeholder" precedent this project already established. Inventing a
plausible-looking polygon for a building that doesn't provably exist in OSM under either
name would be a worse outcome than an honest, disclosed gap -- fabricated geodata a
future story might build on top of without knowing it was never real.
"""
import json
import os

RAW_DIR = os.path.join(os.path.dirname(__file__), "raw")
OUT_DIR = os.path.join(os.path.dirname(__file__), "output")

# building_id (campus.db) -> (OSM way id, OSM name) -- see module docstring for the
# matching reasoning behind each of these 3, and why the other 2 seed Buildings (id 1
# FNB Building, id 2 Robert Sobukwe Block) are deliberately absent from this map.
MATCHES = {
    3: (452712704, "Great Hall"),
    4: (26327942, "William Cullen library"),
    5: (739521835, "Origins Centre"),
}


def main():
    with open(os.path.join(RAW_DIR, "wits_buildings.json")) as f:
        data = json.load(f)

    ways_by_id = {el["id"]: el for el in data["elements"] if el["type"] == "way"}

    features = []
    for building_id, (osm_way_id, osm_name) in MATCHES.items():
        way = ways_by_id.get(osm_way_id)
        if way is None:
            raise SystemExit(f"OSM way {osm_way_id} ({osm_name}) not found in raw extract -- "
                              "re-run the Overpass query, the extract may be stale.")
        geom = way["geometry"]
        # OSM ways forming a closed polygon repeat their first node as the last -- GeoJSON
        # requires exactly this (a closed LinearRing), so no adjustment needed as long as
        # the way is genuinely closed. Verify rather than assume.
        if geom[0] != geom[-1]:
            raise SystemExit(f"OSM way {osm_way_id} ({osm_name}) is not a closed ring "
                              f"({geom[0]} != {geom[-1]}) -- can't represent as a Polygon.")
        ring = [[pt["lon"], pt["lat"]] for pt in geom]
        features.append({
            "type": "Feature",
            "properties": {
                "building_id": building_id,
                "osm_way_id": osm_way_id,
                "osm_name": osm_name,
            },
            "geometry": {
                "type": "Polygon",
                "coordinates": [ring],
            },
        })

    fc = {"type": "FeatureCollection", "features": features}
    os.makedirs(OUT_DIR, exist_ok=True)
    out_path = os.path.join(OUT_DIR, "building_footprints.geojson")
    with open(out_path, "w") as f:
        json.dump(fc, f, indent=2)
    print(f"Wrote {out_path}: {len(features)} Building footprints "
          f"({len(MATCHES)} of 5 seed Buildings matched)")


if __name__ == "__main__":
    main()
