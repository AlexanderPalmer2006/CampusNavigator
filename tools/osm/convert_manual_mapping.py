#!/usr/bin/env python3
"""Converts a hand-drawn GeoJSON file (e.g. exported from geojson.io) into an *additive*
Node/Edge/Building/BuildingFootprint reseed for campus.db.

Unlike convert_walkways.py (Story 6.1, a full DELETE+INSERT reseed from a single OSM
extract), this script is designed to be run repeatedly, each time against a fresh
hand-drawn GeoJSON file covering whatever paths/buildings were mapped in that session --
it only INSERTs new rows on top of whatever's already in campus.db, auto-allocating ids
starting just above the current max so it never collides with existing OSM-derived or
prior manually-mapped data.

## What to draw (see README.md's "Manual mapping" section for the full walkthrough)

In geojson.io (https://geojson.io, satellite basemap toggle bottom-left) or uMap:

- **A real walkway that's missing from the current graph**: draw a *LineString*, tracing
  the path in satellite imagery. Optionally add a boolean property `is_stairs: true` on
  any segment that's actually stairs (so accessible routing correctly treats it as
  impassable, per AD-8). Draw the line so its endpoints land ON or very near an existing
  path/node if you want it to connect into the routable network -- this script snaps any
  vertex within SNAP_DISTANCE_METERS of an existing Node (or another new vertex in the
  same file) onto that node rather than creating a duplicate, so tracing over an existing
  intersection connects automatically.
- **An unlabeled building**: draw a *Polygon* tracing its real outline, with a property
  `name: "Whatever It's Called"`. This creates both a new `Building` row (searchable,
  routable, a real destination -- same mechanism every existing Building already uses)
  and a `BuildingFootprint` row (a permanent outline fill on the map, same mechanism
  Story 6.3 already renders for Great Hall/Central Library/Origins Centre).
- **An unlabeled building you don't want to trace the outline of**: draw a *Point*
  instead, with a `name` property -- creates a `Building` row only, no footprint fill
  (same as FNB Building/Robert Sobukwe Block today).
- **A mislabeled or wrongly-positioned EXISTING building** (e.g. FNB Building/Robert
  Sobukwe Block, which never got a real OSM match in Story 6.1 and still sit at their
  original placeholder coordinates with no outline): draw a Polygon or Point the same
  way, but add a `correct_building_id` property set to that Building's existing numeric
  id (run `sqlite3 campus.db "SELECT id, name FROM Building"` to look them up). This
  UPDATEs the existing Building row's name/position in place (never creates a duplicate)
  and, for a Polygon, replaces any existing footprint for that building with the newly
  traced one -- correcting AC 3's "no footprint" gap, not adding a second building on
  top of the first.

Optional properties on a Polygon or Point: `code`, `faculty_department`.

## Usage

    python3 convert_manual_mapping.py path/to/your-export.geojson

Writes output/manual_mapping_reseed.sql. Review it, then apply the same way every prior
story's seed data was applied:

    sqlite3 ../../data/src/main/assets/database/campus.db < output/manual_mapping_reseed.sql
"""
import json
import math
import os
import sys

OUT_DIR = os.path.join(os.path.dirname(__file__), "output")
DB_PATH = os.path.join(os.path.dirname(__file__), "..", "..",
                        "data", "src", "main", "assets", "database", "campus.db")

# Vertices within this distance of an existing Node (or another new vertex already
# placed in this run) are snapped onto it rather than creating a duplicate -- real-world
# GPS/satellite-tracing precision is rarely better than a couple of metres, and without
# snapping, two hand-traced lines meant to meet at the same real intersection would
# instead create two disconnected nodes a few centimetres apart, silently failing to
# connect (the exact class of problem this whole script exists to fix).
SNAP_DISTANCE_METERS = 5.0


def haversine_m(lat1, lon1, lat2, lon2):
    r = 6371000.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlmb = math.radians(lon2 - lon1)
    a = math.sin(dphi / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dlmb / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


def current_max_ids():
    """Reads the real current max id for each table directly from campus.db, via the
    sqlite3 CLI (no Python sqlite3 dependency assumption beyond the stdlib module, which
    is used here since it's a read-only query and simpler than shelling out)."""
    import sqlite3
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    ids = {}
    for table in ("Node", "Edge", "Building", "BuildingFootprint"):
        cur.execute(f"SELECT COALESCE(MAX(id), 0) FROM {table}")
        ids[table] = cur.fetchone()[0]
    conn.close()
    return ids


def existing_nodes():
    """(node_id, lat, lon) for every Node already in campus.db -- new line vertices snap
    onto these if close enough, so a hand-traced path that crosses an existing
    intersection actually connects to the routable network instead of floating nearby."""
    import sqlite3
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute("SELECT id, latitude, longitude FROM Node")
    rows = cur.fetchall()
    conn.close()
    return rows


def existing_edge_pairs():
    """(min_id, max_id) for every Edge already in campus.db -- undirected, same convention
    AStarRouter/convert_walkways.py already use. Prevents re-running this script against a
    GeoJSON export that re-traces (in full or in part) an already-applied mapping session
    from inserting duplicate edges between the same two nodes -- a real, observed case:
    exporting again mid-session from geojson.io naturally re-includes everything drawn so
    far, not just what's new since the last export."""
    import sqlite3
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute("SELECT from_node_id, to_node_id FROM Edge")
    pairs = {(min(a, b), max(a, b)) for a, b in cur.fetchall()}
    conn.close()
    return pairs


class NodeAllocator:
    """Assigns local ids to new line vertices, snapping to existing or already-allocated
    nodes within SNAP_DISTANCE_METERS rather than always creating a new one."""

    def __init__(self, next_id, existing):
        self.next_id = next_id
        self.points = [(nid, lat, lon) for nid, lat, lon in existing]  # (id, lat, lon)
        self.new_node_sql = []

    def get_or_create(self, lat, lon):
        for nid, elat, elon in self.points:
            if haversine_m(lat, lon, elat, elon) <= SNAP_DISTANCE_METERS:
                return nid
        nid = self.next_id
        self.next_id += 1
        self.points.append((nid, lat, lon))
        self.new_node_sql.append(
            f"INSERT INTO Node (id, latitude, longitude, campus_id, level_id) VALUES "
            f"({nid}, {lat}, {lon}, 'wits-main', 'OUTDOOR');"
        )
        return nid


def process_linestring(feature, node_alloc, edge_id_start, seen_pairs):
    coords = feature["geometry"]["coordinates"]  # [[lon, lat], ...]
    is_stairs = 1 if feature.get("properties", {}).get("is_stairs") else 0
    edge_sql = []
    edge_id = edge_id_start
    prev_node_id = None
    skipped_duplicates = 0
    for lon, lat in coords:
        node_id = node_alloc.get_or_create(lat, lon)
        if prev_node_id is not None and prev_node_id != node_id:
            pair = (min(prev_node_id, node_id), max(prev_node_id, node_id))
            if pair in seen_pairs:
                skipped_duplicates += 1
            else:
                seen_pairs.add(pair)
                dist = haversine_m(*_latlon(node_alloc, prev_node_id), *_latlon(node_alloc, node_id))
                edge_sql.append(
                    "INSERT INTO Edge (id, from_node_id, to_node_id, distance_meters, is_stairs) "
                    f"VALUES ({edge_id}, {prev_node_id}, {node_id}, {dist:.2f}, {is_stairs});"
                )
                edge_id += 1
        prev_node_id = node_id
    return edge_sql, edge_id, skipped_duplicates


def _latlon(node_alloc, node_id):
    for nid, lat, lon in node_alloc.points:
        if nid == node_id:
            return lat, lon
    raise KeyError(node_id)


def polygon_centroid(rings):
    # Planar mean of the outer ring's vertices -- same approximation
    # convert_walkways.py's own BUILDING_COORD_UPDATES already uses at this ~1km scale.
    outer = rings[0]
    lons = [c[0] for c in outer]
    lats = [c[1] for c in outer]
    return sum(lats) / len(lats), sum(lons) / len(lons)


def process_polygon(feature, building_id, footprint_id):
    props = feature.get("properties", {})
    name = props.get("name")
    if not name:
        return None, "Polygon feature has no 'name' property -- skipped (every new " \
                      "Building needs a name to be searchable/identifiable)."
    coords = feature["geometry"]["coordinates"]  # [[[lon,lat],...]] (outer ring [0])
    lat, lon = polygon_centroid(coords)
    code = props.get("code")
    faculty = props.get("faculty_department")
    code_sql = f"'{code}'" if code else "NULL"
    faculty_sql = f"'{faculty}'" if faculty else "NULL"
    name_escaped = name.replace("'", "''")
    ring_geojson = json.dumps(coords[0])  # exact [[lon,lat],...] shape parseRing() expects
    ring_geojson_escaped = ring_geojson.replace("'", "''")

    correct_id = props.get("correct_building_id")
    if correct_id is not None:
        building_sql = (
            f"UPDATE Building SET name = '{name_escaped}', latitude = {lat}, "
            f"longitude = {lon}, code = {code_sql}, faculty_department = {faculty_sql} "
            f"WHERE id = {correct_id};"
        )
        # Replace, not add to, any existing footprint for this Building -- a correction
        # means the old outline (or lack of one) is wrong, not that this is a second ring
        # of a genuinely multi-polygon building.
        delete_sql = f"DELETE FROM BuildingFootprint WHERE building_id = {correct_id};"
        footprint_sql = (
            "INSERT INTO BuildingFootprint (id, building_id, ring_geojson) VALUES "
            f"({footprint_id}, {correct_id}, '{ring_geojson_escaped}');"
        )
        return (building_sql, delete_sql, footprint_sql, f"{name} (corrected)"), None

    building_sql = (
        "INSERT INTO Building (id, name, latitude, longitude, campus_id, code, "
        "faculty_department, is_landmark_pick) VALUES "
        f"({building_id}, '{name_escaped}', {lat}, {lon}, 'wits-main', {code_sql}, {faculty_sql}, 0);"
    )
    footprint_sql = (
        "INSERT INTO BuildingFootprint (id, building_id, ring_geojson) VALUES "
        f"({footprint_id}, {building_id}, '{ring_geojson_escaped}');"
    )
    return (building_sql, None, footprint_sql, name), None


def process_point(feature, building_id):
    props = feature.get("properties", {})
    name = props.get("name")
    if not name:
        return None, "Point feature has no 'name' property -- skipped."
    lon, lat = feature["geometry"]["coordinates"]
    code = props.get("code")
    faculty = props.get("faculty_department")
    code_sql = f"'{code}'" if code else "NULL"
    faculty_sql = f"'{faculty}'" if faculty else "NULL"
    name_escaped = name.replace("'", "''")

    correct_id = props.get("correct_building_id")
    if correct_id is not None:
        building_sql = (
            f"UPDATE Building SET name = '{name_escaped}', latitude = {lat}, "
            f"longitude = {lon}, code = {code_sql}, faculty_department = {faculty_sql} "
            f"WHERE id = {correct_id};"
        )
        return (building_sql, f"{name} (corrected)"), None

    building_sql = (
        "INSERT INTO Building (id, name, latitude, longitude, campus_id, code, "
        "faculty_department, is_landmark_pick) VALUES "
        f"({building_id}, '{name_escaped}', {lat}, {lon}, 'wits-main', {code_sql}, {faculty_sql}, 0);"
    )
    return (building_sql, name), None


def main():
    if len(sys.argv) != 2:
        raise SystemExit(f"Usage: python3 {sys.argv[0]} path/to/exported.geojson")
    geojson_path = sys.argv[1]

    with open(geojson_path) as f:
        data = json.load(f)
    features = data["features"]

    max_ids = current_max_ids()
    node_alloc = NodeAllocator(max_ids["Node"] + 1, existing_nodes())
    edge_id = max_ids["Edge"] + 1
    building_id = max_ids["Building"] + 1
    footprint_id = max_ids["BuildingFootprint"] + 1
    seen_pairs = existing_edge_pairs()

    all_edge_sql = []
    all_building_sql = []
    all_delete_sql = []
    all_footprint_sql = []
    summary_paths = 0
    summary_buildings = []
    summary_corrections = []
    warnings = []
    total_skipped_duplicates = 0

    for feature in features:
        geom_type = feature.get("geometry", {}).get("type")
        if geom_type == "LineString":
            edge_sql, edge_id, skipped = process_linestring(feature, node_alloc, edge_id, seen_pairs)
            all_edge_sql.extend(edge_sql)
            total_skipped_duplicates += skipped
            summary_paths += 1
        elif geom_type == "Polygon":
            result, warning = process_polygon(feature, building_id, footprint_id)
            if warning:
                warnings.append(warning)
                continue
            building_sql, delete_sql, footprint_sql, name = result
            all_building_sql.append(building_sql)
            if delete_sql:
                all_delete_sql.append(delete_sql)
                summary_corrections.append(name)
            else:
                summary_buildings.append(f"{name} (outlined)")
                building_id += 1
            all_footprint_sql.append(footprint_sql)
            footprint_id += 1
        elif geom_type == "Point":
            result, warning = process_point(feature, building_id)
            if warning:
                warnings.append(warning)
                continue
            building_sql, name = result
            all_building_sql.append(building_sql)
            if "(corrected)" in name:
                summary_corrections.append(name)
            else:
                summary_buildings.append(f"{name} (point only)")
                building_id += 1
        else:
            warnings.append(f"Unsupported geometry type '{geom_type}' -- skipped.")

    os.makedirs(OUT_DIR, exist_ok=True)
    out_path = os.path.join(OUT_DIR, "manual_mapping_reseed.sql")
    with open(out_path, "w") as f:
        f.write(f"-- Generated by tools/osm/convert_manual_mapping.py from {os.path.basename(geojson_path)}\n")
        f.write("-- Node/Edge/new-Building rows are additive only. Any 'correct_building_id'\n")
        f.write("-- feature UPDATEs that existing Building row and replaces its footprint.\n")
        f.write("BEGIN TRANSACTION;\n")
        for line in node_alloc.new_node_sql:
            f.write(line + "\n")
        for line in all_edge_sql:
            f.write(line + "\n")
        for line in all_building_sql:
            f.write(line + "\n")
        for line in all_delete_sql:
            f.write(line + "\n")
        for line in all_footprint_sql:
            f.write(line + "\n")
        f.write("COMMIT;\n")

    print(f"New Nodes: {len(node_alloc.new_node_sql)}")
    print(f"New Edges: {len(all_edge_sql)}  (from {summary_paths} drawn path(s))")
    if total_skipped_duplicates:
        print(f"Skipped {total_skipped_duplicates} edge(s) already present in campus.db "
              f"(same node pair, likely re-exported from an earlier mapping session)")
    print(f"New Buildings: {len(summary_buildings)}")
    for b in summary_buildings:
        print(f"  - {b}")
    if summary_corrections:
        print(f"Corrected Buildings: {len(summary_corrections)}")
        for c in summary_corrections:
            print(f"  - {c}")
    if warnings:
        print("Warnings:")
        for w in warnings:
            print(f"  ! {w}")
    print(f"Wrote {out_path}")
    print("Review it, then apply with:")
    print(f"  sqlite3 {os.path.relpath(DB_PATH, os.path.dirname(__file__))} < output/manual_mapping_reseed.sql")


if __name__ == "__main__":
    main()
