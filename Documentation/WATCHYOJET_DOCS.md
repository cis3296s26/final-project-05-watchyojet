# WatchyoJet — System Documentation

## What is it

WatchyoJet is an autonomous Air Traffic Control (ATC) decision system that runs in shadow mode over the Philadelphia TRACON (Terminal Radar Approach Control) airspace. It pulls live flight data from the OpenSky Network, simulates aircraft movement, detects potential conflicts, and automatically issues altitude-based separation instructions — all in real time, with no human input required.

"Shadow mode" means the system makes decisions and logs them, but doesn't actually send commands to real aircraft. It's a simulation layer that runs on top of real-world data.

---

## What it can do

- Pull live aircraft positions from the OpenSky Network API every 12 seconds, covering the PHL TRACON region (~60 NM radius around Philadelphia International)
- Track 100–250 live flights simultaneously
- Simulate aircraft movement between API refreshes (2-second cycles using heading + speed)
- Detect conflicts using CPA (Closest Point of Approach) math — catches aircraft on converging courses before they actually get close
- Classify conflicts by severity: CRITICAL, HIGH, or MEDIUM
- Resolve conflicts by issuing altitude changes, respecting:
  - Aircraft type and maximum altitude ceiling
  - Flight phase (takeoff/landing, final approach, climb/descent, cruise)
  - Altitude band separation (IFR above FL180 stays above FL180, VFR below stays below)
  - Priority rules (emergency aircraft never get moved; military aircraft get right of way)
  - A 15-second cooldown per aircraft (prevents oscillating re-assignments)
  - Pending assignments in the same cycle (prevents two aircraft being sent to the same altitude)
- Display everything on a live map with aircraft markers, conflict highlights, and a resolution history log
- Log ATC events to an in-UI event feed

---

## How each part works

### Live data ingestion — `OpenSkyFetcher`

Sends an HTTP GET to the OpenSky REST API with a bounding box around PHL. Parses the response (a JSON array of state vectors) and converts each entry into an `Aircraft` object. Skips any entry with null callsign, position, speed, or heading. Uses barometric altitude (field 13) when available, falls back to geometric altitude (field 7). Converts everything to imperial units (feet, knots).

Because the OpenSky API doesn't return aircraft type, `inferType()` makes a guess based on speed and altitude: anything fast and high is mapped to A320, mid-range to B737, slow and low to C172.

### Aircraft state management — `AircraftManager`

Maintains a `ConcurrentHashMap<callsign, Aircraft>` that persists across API refreshes. On each refresh, positions, heading, and speed are updated from live data. Altitude is deliberately NOT overwritten — if the ATC engine already issued a resolution that changed an aircraft's altitude, we keep that value. Overwriting with raw transponder data would undo the instruction.

New aircraft entering the airspace for the first time get all fields (including altitude) from live data.

### Movement simulation — `MovementEngine`

Runs every 2 seconds. Moves each aircraft forward based on its current speed and heading using equirectangular projection:

```
distanceNm = speed * (2 / 3600)
dLat = distanceNm * cos(heading) / 60
dLon = distanceNm * sin(heading) / (60 * cos(lat))
```

This keeps positions consistent between API refreshes so the ATC engine always operates on up-to-date locations.

### Conflict detection — `ConflictDetector`

Checks every pair of aircraft. For each pair:

1. Computes tCPA (time to Closest Point of Approach) using relative velocity vectors
2. If tCPA is within the 0–120 second lookahead window, computes dCPA (distance at that moment) using `TrajectoryPredictor`
3. Flags a conflict if dCPA < 3.0 NM and altitude difference < 1000 ft

Severity classification:
- CRITICAL: dCPA < 1.0 NM and altitude diff < 500 ft
- HIGH: dCPA < 3.0 NM
- MEDIUM: everything else that still triggers the thresholds

### Trajectory prediction — `TrajectoryPredictor`

Static helper. Given an aircraft and a time in seconds, returns the predicted `[lat, lon]` using the same equirectangular math as `MovementEngine`. Used by `ConflictDetector` to compute where two aircraft will be at the moment of closest approach.

### Conflict resolution — `ResolutionEngine`

Handles one aircraft at a time. Selects which aircraft to move based on priority (emergency > military > commercial > general aviation — lower priority gets moved). Then searches for a free altitude:

- Builds a search array: `[+1000, -1000, +2000, -2000, ..., +15000, -15000]` ft offsets from current altitude
- Bounds the search by flight phase:
  - TAKEOFF/LANDING: ±500–1000 ft
  - FINAL APPROACH: ±2000 ft
  - CLIMB/DESCENT: +5000 / -3000 ft
  - CRUISE: full range up to aircraft ceiling
- Applies band lock: aircraft above FL180 can't drop below it; aircraft below FL180 can't climb above it
- For each candidate altitude, `isAltitudeFree()` checks all nearby aircraft (within 8 NM) — both their current altitude and any pending assignments from the same cycle
- Returns the closest free altitude, or null if none found

### ATC engine loop — `ATCEngine`

Called every 2 seconds. Steps:

1. Move all aircraft forward one cycle
2. Detect conflicts
3. Sort conflicts by severity (CRITICAL first)
4. For each conflict, attempt resolution:
   - Check cooldown (15s since last resolution) and per-cycle deduplication
   - If primary aircraft blocked, try the other one
   - Track assigned altitudes in `pendingAltitudes` so the same slot isn't given to two aircraft
5. Apply all resolutions (set new altitude on aircraft object)
6. Post-check: re-run detection and log if any new conflicts were introduced by the resolutions
7. Notify the UI

### UI bridge — `WYJAppController`

JavaFX controller that owns the `WebEngine` (the browser inside the app). All communication to the HTML/JS layer goes through `webEngine.executeScript()` on the JavaFX application thread (`Platform.runLater`).

Key calls:
- `batchUpdateAircraft(arr)` — sends all aircraft positions as a JSON array to JS every cycle
- `markConflict(cs1, cs2)` — highlights a conflict pair on the map
- `markResolved(cs1, cs2, movedCs, newAlt)` — adds an entry to the resolution history log
- `logATCEvent(msg)` — appends a line to the event feed

### Frontend — `map.html`

A single HTML file with embedded CSS and JavaScript. Uses Leaflet.js for the map. No framework, no build step.

Aircraft are rendered as rotated SVG plane icons on the map. Color indicates status: normal, conflict (red), or recently resolved (green). Clicking a marker opens a popup with callsign, altitude, speed, and heading.

The resolution history panel shows the last 10 resolutions. Each entry includes the timestamp, the conflicting pair, which aircraft was moved, and the new altitude. Entries expire after 30 seconds and the age label updates every 2 seconds.

---

## Architecture overview

```
OpenSky API
    │
    ▼
OpenSkyFetcher (HTTP every 12s)
    │
    ▼
AircraftManager (stateful, persists altitude across refreshes)
    │
    ▼
ATCEngine (every 2s)
    ├── MovementEngine      → updates lat/lon
    ├── ConflictDetector    → finds pairs violating separation
    ├── ResolutionEngine    → picks new altitude for one aircraft
    └── WYJAppController    → bridge to JavaFX WebView
                                │
                                ▼
                            map.html (Leaflet map + live panels)
```

The main thread (`Main.java`) runs the 2-second loop. It's spawned from a JavaFX background task (`spawnMainThread`) so it doesn't block the UI thread. The UI only updates via `Platform.runLater()`.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI framework | JavaFX 21 |
| Map rendering | Leaflet.js (loaded in WebView) |
| HTTP client | Java built-in `java.net.http.HttpClient` |
| JSON parsing | Jackson (`ObjectMapper`) |
| Build tool | Maven |
| Live data | OpenSky Network REST API |
| Frontend | Plain HTML/CSS/JS (no framework) |

---

## Problems I ran into

### 1. Resolution was undone on every API refresh

The first implementation just replaced the full aircraft state from the API response every 12 seconds. So the ATC engine would issue an altitude change, it would apply, and then 12 seconds later the raw transponder altitude would overwrite it. The resolution was effectively undone before it had any effect.

Fix: `AircraftManager.syncWithLiveData()` now only updates position, heading, and speed. Altitude is skipped for aircraft that already exist in the manager's state.

### 2. All cruise-phase conflicts were unresolved (CRUISE floor bug)

The resolution engine uses phase-based altitude bounds. For CRUISE aircraft, the floor was mistakenly set to `HIGH_ALT_FLOOR` (18,000 ft) by default. For aircraft cruising between 10,000 and 18,000 ft, this made `searchFloor > searchCeiling`, giving the resolver an empty search range. Every conflict involving a cruise aircraft in that band returned null — which in a busy airspace meant 20+ unresolved conflicts per cycle.

Fix: CRUISE `searchFloor` defaults to `MIN_ALTITUDE` (3,000 ft). The band lock then raises it to 18,000 only for aircraft that are already above FL180.

### 3. Dense airspace blocked all candidate altitudes

With `PROXIMITY_NM = 15`, the `isAltitudeFree()` check was scanning a 15 NM radius around each aircraft before assigning an altitude. In the PHL TRACON with ~200 aircraft, that radius typically captured 15–20 other aircraft, each blocking a 1,000 ft slot. Combined with the phase-bounded search range, there were no free slots left. Unresolved counts stayed at 9–16 per cycle even after the CRUISE fix.

Fix: Reduced `PROXIMITY_NM` to 8 NM, matching a more realistic separation check range. This reduced the number of blocked slots to 3–6 per aircraft and made resolutions possible again in dense regions.

### 4. Cooldown too long, persistent conflicts couldn't be re-addressed

The cross-cycle cooldown was initially 45 seconds to prevent oscillation (aircraft A resolves, gets moved, comes back into conflict, resolves again, repeat). But with a 2-second cycle, 45 seconds meant ~22 cycles where a persistently conflicting aircraft couldn't be touched. For aircraft on genuine converging courses, tCPA was dropping toward zero while the cooldown blocked intervention.

Fix: Reduced cooldown to 15 seconds (~7 cycles). Long enough to confirm a resolution worked, short enough to re-engage if the aircraft is still converging.

### 5. JavaFX WebView JS bridge timing

The WebView loads the HTML file asynchronously. Early versions called `webEngine.executeScript()` before the page had finished loading, which silently failed with no error. Aircraft updates were going nowhere and the map stayed empty.

Fix: Added a load state listener in `WYJApp.java`. The main ATC loop (`Main.main()`) only starts after `Worker.State.SUCCEEDED` fires, ensuring the JS functions exist before any calls go through.

### 6. Conflict sort order was wrong

`Conflict.Severity` was defined as `CRITICAL, HIGH, MEDIUM`. Sorting with `Comparator.comparing(Conflict::getSeverity)` uses enum ordinal, so CRITICAL (ordinal 0) sorts before HIGH (ordinal 1) and MEDIUM (ordinal 2) — which is what we want. But the original code didn't sort at all, so a batch of conflicts was processed in whatever order `detectConflicts()` returned them (pair iteration order). MEDIUM conflicts were sometimes resolved before CRITICAL ones.

Fix: Added `conflicts.sort(Comparator.comparing(Conflict::getSeverity))` before the resolution loop.

### 7. Two aircraft assigned the same altitude in one cycle

Without coordination between resolutions in the same cycle, two different conflict pairs could each have their aircraft sent to the same altitude — creating a new conflict in the process of resolving the original ones. The post-resolution safety check was logging these cascades every cycle.

Fix: `pendingAltitudes` map tracks altitudes committed earlier in the same cycle. `isAltitudeFree()` checks this map before returning a candidate. Assignments don't collide within a single cycle.
