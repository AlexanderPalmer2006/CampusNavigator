-- Story 6.1: correct 3 seed Buildings' lat/lon to their real, OSM-matched
-- footprint centroid (see convert_walkways.py's module docstring for why).
-- FNB Building (id 1) and Robert Sobukwe Block (id 2) are deliberately NOT
-- updated -- no confident real-world OSM match found (see convert_footprints.py).
BEGIN TRANSACTION;
UPDATE Building SET latitude = -26.191919, longitude = 28.030312 WHERE id = 3; -- Great Hall
UPDATE Building SET latitude = -26.19072, longitude = 28.02942 WHERE id = 4; -- Central Library (William Cullen Library)
UPDATE Building SET latitude = -26.189571, longitude = 28.030839 WHERE id = 5; -- Origins Centre
COMMIT;
