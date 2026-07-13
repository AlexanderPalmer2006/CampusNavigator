#!/usr/bin/env python3
"""Story 6.1: convert raw/wits_walkways_topo.json into a Node/Edge reseed for campus.db.

Replaces the 5-node hand-authored mesh from Stories 2.2/3.1 with a real campus-scale
walkway graph extracted from OpenStreetMap (see README.md for the source query). This is
a *data* change only -- Node/Edge's schema is unchanged (NodeEntity/EdgeEntity, Story
2.2), so ComputeRouteUseCase/AStarRouter/NavigationSession need zero code changes
(epics.md's own explicit Story 6.1 AC3 framing).

Real OSM node ids (e.g. 7172740703) are remapped to small sequential integers (1..N) --
Room's `Node.id`/`Edge.id` are plain `INTEGER PRIMARY KEY` and every prior story's seed
data used small sequential ids; there's no reason to carry 10-digit OSM ids into the app
schema, and small ids are dramatically more readable in any future `sqlite3` debugging
session.

Also emits building_coords_update.sql: 3 of the 5 existing seed Buildings (Great Hall,
Origins Centre, Central Library) get their `latitude`/`longitude` corrected to match the
real building they're now visually anchored to (see convert_footprints.py's matching
logic -- same 3 buildings, same reasoning). This is *not* enumerated in epics.md's literal
Story 6.1 AC bullet list, but is a real, necessary consequence of the rest of this story:
once Story 6.2 ships a real basemap and Story 6.3 ships a real footprint fill for these
3 buildings, the existing fictional hand-placed pin coordinates (hundreds of metres from
the real footprint -- e.g. the old Great Hall seed coordinate is 247m from the real
Great Hall polygon) would visibly show the building's own label/search-result/marker
sitting on a patch of empty ground away from its own fill and away from the real street
grid. Leaving that stale is a worse outcome than fixing it now, while this story is
already re-grounding the app's geography in real coordinates. The other 2 Buildings (FNB
Building/Accountancy, Robert Sobukwe Block) have no confident real-world match (see
convert_footprints.py) and are deliberately left at their existing fictional coordinates,
unchanged -- not a regression, since that was already true before this story.
"""
import json
import math
import os

RAW_DIR = os.path.join(os.path.dirname(__file__), "raw")
OUT_DIR = os.path.join(os.path.dirname(__file__), "output")


def haversine_m(lat1, lon1, lat2, lon2):
    r = 6371000.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlmb = math.radians(lon2 - lon1)
    a = math.sin(dphi / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dlmb / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


def load_walkways():
    with open(os.path.join(RAW_DIR, "wits_walkways_topo.json")) as f:
        data = json.load(f)
    nodes_by_id = {}
    ways = []
    for el in data["elements"]:
        if el["type"] == "node":
            nodes_by_id[el["id"]] = (el["lat"], el["lon"])
        elif el["type"] == "way":
            ways.append(el)
    return nodes_by_id, ways


def build_graph(nodes_by_id, ways):
    # Only keep OSM nodes actually referenced by a walkway way (all 253 are, but be
    # defensive rather than assume the Overpass recursion returned exactly that set).
    used_osm_ids = sorted({nid for w in ways for nid in w["nodes"] if nid in nodes_by_id})
    osm_id_to_local = {osm_id: i + 1 for i, osm_id in enumerate(used_osm_ids)}

    edges = []
    seen_pairs = set()
    for w in ways:
        is_stairs = 1 if w.get("tags", {}).get("highway") == "steps" else 0
        way_nodes = [n for n in w["nodes"] if n in nodes_by_id]
        for a, b in zip(way_nodes, way_nodes[1:]):
            local_a, local_b = osm_id_to_local[a], osm_id_to_local[b]
            if local_a == local_b:
                continue  # degenerate zero-length segment, skip
            key = (min(local_a, local_b), max(local_a, local_b))
            if key in seen_pairs:
                continue  # two ways sharing the exact same segment -- don't double-insert
            seen_pairs.add(key)
            lat1, lon1 = nodes_by_id[a]
            lat2, lon2 = nodes_by_id[b]
            dist = haversine_m(lat1, lon1, lat2, lon2)
            edges.append((local_a, local_b, dist, is_stairs))

    return osm_id_to_local, edges


def connectivity_report(osm_id_to_local, edges):
    """Union-find over the local node ids; returns (num_components, largest_component_size)."""
    parent = {i: i for i in osm_id_to_local.values()}

    def find(x):
        while parent[x] != x:
            parent[x] = parent[parent[x]]
            x = parent[x]
        return x

    def union(a, b):
        ra, rb = find(a), find(b)
        if ra != rb:
            parent[ra] = rb

    for a, b, _, _ in edges:
        union(a, b)

    from collections import Counter
    sizes = Counter(find(n) for n in parent)
    return len(sizes), max(sizes.values())


# The raw extraction produces 11 disconnected graph components -- a real, common OSM
# data-quality artifact (separately-surveyed path segments that were never snapped
# together in the source data), not a bug in this script. Checked which of the 5 seed
# Buildings' nearest-node snaps fall in which component: {FNB, Robert Sobukwe, Great
# Hall, Central Library} all snap into one 9-node component; Origins Centre snaps into
# the main 184-node component -- a different one. Left as-is, this would be a real
# functional regression from today's fully-connected 5-node mesh: every route to/from
# Origins Centre (a curated Landmark Pick, Story 4.1) would newly return "no route
# available" for every user, in every direction. The other 9 minor disconnected
# fragments (2-16 nodes each) don't contain any Building's nearest node, so they're
# harmless -- not bridged, left as an honest, disclosed OSM-data-quality limitation
# (see Dev Notes), matching this project's "don't over-invest in seed-data realism"
# discipline (correct-course session, 2026-07-11) rather than fabricating a network
# more complete than the real extract actually is.
#
# The one bridge that IS added: a single edge connecting the two components that
# together hold all 5 Buildings' nearest nodes, at the real shortest distance between
# them (22.9m -- computed directly from the two components' own node coordinates, not
# guessed), same class of deliberate, disclosed hand-authored edge Story 2.2 originally
# used for its whole graph and Story 3.1 used for its single is_stairs pick.
BRIDGE_EDGE = (65, 219, 22.90)  # local node ids, Haversine metres


def write_sql(osm_id_to_local, nodes_by_id, edges):
    os.makedirs(OUT_DIR, exist_ok=True)
    path = os.path.join(OUT_DIR, "node_edge_reseed.sql")
    with open(path, "w") as f:
        f.write("-- Story 6.1: reseed Node/Edge with real Wits-campus OSM walkway data.\n")
        f.write("-- Generated by tools/osm/convert_walkways.py -- do not hand-edit, regenerate instead.\n")
        f.write("BEGIN TRANSACTION;\n")
        f.write("DELETE FROM Edge;\n")
        f.write("DELETE FROM Node;\n")
        for osm_id, local_id in sorted(osm_id_to_local.items(), key=lambda kv: kv[1]):
            lat, lon = nodes_by_id[osm_id]
            f.write(
                "INSERT INTO Node (id, latitude, longitude, campus_id, level_id) VALUES "
                f"({local_id}, {lat}, {lon}, 'wits-main', 'OUTDOOR');\n"
            )
        all_edges = list(edges) + [(BRIDGE_EDGE[0], BRIDGE_EDGE[1], BRIDGE_EDGE[2], 0)]
        for i, (a, b, dist, is_stairs) in enumerate(all_edges, start=1):
            comment = "  -- bridge edge, see module docstring" if (a, b, dist) == BRIDGE_EDGE else ""
            f.write(
                "INSERT INTO Edge (id, from_node_id, to_node_id, distance_meters, is_stairs) VALUES "
                f"({i}, {a}, {b}, {dist:.2f}, {is_stairs});{comment}\n"
            )
        f.write("COMMIT;\n")
    return path


# 3 of the 5 existing seed Buildings, matched to a real OSM building (see
# convert_footprints.py's matching logic -- reused here verbatim, not re-derived).
# Coordinates are the matched building's real footprint centroid (planar mean of its
# polygon vertices -- adequate at this ~1km scale, no geodesic centroid needed).
BUILDING_COORD_UPDATES = {
    3: ("Great Hall", -26.191919, 28.030312),          # OSM way 452712704
    4: ("Central Library (William Cullen Library)", -26.190720, 28.029420),  # OSM way 26327942
    5: ("Origins Centre", -26.189571, 28.030839),        # OSM way 739521835
}


def write_building_coord_updates():
    path = os.path.join(OUT_DIR, "building_coords_update.sql")
    with open(path, "w") as f:
        f.write("-- Story 6.1: correct 3 seed Buildings' lat/lon to their real, OSM-matched\n")
        f.write("-- footprint centroid (see convert_walkways.py's module docstring for why).\n")
        f.write("-- FNB Building (id 1) and Robert Sobukwe Block (id 2) are deliberately NOT\n")
        f.write("-- updated -- no confident real-world OSM match found (see convert_footprints.py).\n")
        f.write("BEGIN TRANSACTION;\n")
        for building_id, (label, lat, lon) in BUILDING_COORD_UPDATES.items():
            f.write(
                f"UPDATE Building SET latitude = {lat}, longitude = {lon} "
                f"WHERE id = {building_id}; -- {label}\n"
            )
        f.write("COMMIT;\n")
    return path


SEED_BUILDINGS = {
    1: ("FNB Building", -26.1908, 28.0261),
    2: ("Robert Sobukwe Block", -26.1912, 28.0298),
    3: ("Great Hall", -26.191919, 28.030312),
    4: ("Central Library", -26.190720, 28.029420),
    5: ("Origins Centre", -26.189571, 28.030839),
}


def verify_all_buildings_connected(osm_id_to_local, nodes_by_id, edges):
    """Post-bridge sanity check: every seed Building's nearest node must land in the
    same connected component, or AStarRouter.findRoute will silently return not-found
    for some Building pairs -- exactly the regression this script's BRIDGE_EDGE exists
    to prevent. Fails loudly (non-zero exit) rather than shipping a silent regression.
    """
    parent = {n: n for n in osm_id_to_local.values()}

    def find(x):
        while parent[x] != x:
            parent[x] = parent[parent[x]]
            x = parent[x]
        return x

    def union(a, b):
        ra, rb = find(a), find(b)
        if ra != rb:
            parent[ra] = rb

    for a, b, _, _ in edges:
        union(a, b)

    roots = set()
    for building_id, (name, lat, lon) in SEED_BUILDINGS.items():
        osm_id, (nlat, nlon) = min(
            nodes_by_id.items(), key=lambda kv: haversine_m(lat, lon, kv[1][0], kv[1][1])
        )
        # nodes_by_id is keyed by OSM id -- translate to local id for the union-find lookup.
        local = osm_id_to_local[osm_id]
        root = find(local)
        roots.add(root)
        print(f"  {name}: nearest local node {local} ({haversine_m(lat, lon, nlat, nlon):.1f}m) "
              f"component root {root}")
    if len(roots) != 1:
        raise SystemExit(
            f"FAIL: seed Buildings span {len(roots)} disconnected graph components "
            f"({roots}) -- routing between some Building pairs would silently return "
            "not-found. Add/adjust BRIDGE_EDGE and rerun."
        )
    print("  OK: all 5 seed Buildings' nearest nodes are in the same connected component.")


def main():
    nodes_by_id, ways = load_walkways()
    osm_id_to_local, edges = build_graph(nodes_by_id, ways)
    num_components, largest = connectivity_report(osm_id_to_local, edges)
    print(f"Nodes: {len(osm_id_to_local)}  Edges: {len(edges)} (before bridge edge)")
    print(f"Connected components: {num_components}  Largest component: {largest} nodes "
          f"({100.0 * largest / len(osm_id_to_local):.1f}% of all nodes)")

    # BRIDGE_EDGE's endpoints are hard-coded local ids, derived from *this specific*
    # extract's sorted(used_osm_ids) remapping. If raw/wits_walkways_topo.json is ever
    # regenerated from a fresh Overpass pull, that remapping could shift and silently
    # point the bridge at the wrong (or nonexistent) nodes. Fail loudly and specifically
    # here, before verify_all_buildings_connected()'s broader check would otherwise
    # surface it as a less obvious "5 Buildings span N components" failure.
    local_ids_in_use = set(osm_id_to_local.values())
    if BRIDGE_EDGE[0] not in local_ids_in_use or BRIDGE_EDGE[1] not in local_ids_in_use:
        raise SystemExit(
            f"FAIL: BRIDGE_EDGE references local node id(s) {BRIDGE_EDGE[:2]} that don't "
            f"exist in the current extract's {len(local_ids_in_use)}-node remapping -- "
            "raw/wits_walkways_topo.json was likely regenerated. Recompute the shortest "
            "real bridge between the components holding the 5 seed Buildings' nearest "
            "nodes (see module docstring) and update BRIDGE_EDGE."
        )

    all_edges_for_check = edges + [(BRIDGE_EDGE[0], BRIDGE_EDGE[1], BRIDGE_EDGE[2], 0)]
    print("Post-bridge connectivity check:")
    verify_all_buildings_connected(osm_id_to_local, nodes_by_id, all_edges_for_check)

    sql_path = write_sql(osm_id_to_local, nodes_by_id, edges)
    print(f"Wrote {sql_path}")
    coords_path = write_building_coord_updates()
    print(f"Wrote {coords_path}")


if __name__ == "__main__":
    main()
