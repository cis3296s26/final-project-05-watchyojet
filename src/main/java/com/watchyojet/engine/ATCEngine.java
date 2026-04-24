package com.watchyojet.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.watchyojet.WYJAppController;
import com.watchyojet.model.Aircraft;
import com.watchyojet.model.Conflict;
import com.watchyojet.model.Resolution;
import com.watchyojet.simulation.MovementEngine;

public class ATCEngine {

    private final ConflictDetector  detector;
    private final ResolutionEngine  resolver;
    private final MovementEngine    movement;

    // cooldown prevents re-resolving the same aircraft every cycle
    private final Map<String, Long> resolutionCooldown = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 15_000;

    public ATCEngine() {
        this.detector = new ConflictDetector();
        this.resolver = new ResolutionEngine();
        this.movement = new MovementEngine();
    }

    public void runCycle(List<Aircraft> aircrafts) {

        movement.updatePositions(aircrafts);

        List<Conflict> conflicts = detector.detectConflicts(aircrafts);

        if (conflicts.isEmpty()) {
            updateMap(aircrafts);
            return;
        }

        // global sort: CRITICAL first, then earliest tCPA
        conflicts.sort(Comparator.comparing(Conflict::getSeverity)
                                 .thenComparingDouble(Conflict::getTCPA));

        long now = System.currentTimeMillis();
        resolutionCooldown.entrySet().removeIf(e -> now - e.getValue() > COOLDOWN_MS * 2);

        // aircraft on cooldown or already resolved this cycle may not be moved
        Set<String> locked = new HashSet<>();
        for (Map.Entry<String, Long> e : resolutionCooldown.entrySet()) {
            if (e.getValue() + COOLDOWN_MS > now) locked.add(e.getKey());
        }

        // committed[callsign] = {alt, hdg, spd} with NaN = unchanged
        Map<String, double[]> committed = new HashMap<>();

        List<Resolution>  allResolutions = new ArrayList<>();
        List<String[]>    resolvedPairs  = new ArrayList<>();
        List<String[]>    unresolvedPairs = new ArrayList<>();

        // process each cluster holistically
        for (List<Conflict> cluster : detectClusters(conflicts)) {

            List<Resolution> clusterResolutions =
                resolver.resolveCluster(cluster, aircrafts, committed, locked);

            for (Resolution r : clusterResolutions) {
                String cs = r.getAircraft().getCallsign();
                locked.add(cs);
                resolutionCooldown.put(cs, now);
                allResolutions.add(r);

                // find both callsigns for the first conflict involving this aircraft
                String cs1 = cs, cs2 = cs;
                for (Conflict c : cluster) {
                    if (c.getA1().getCallsign().equals(cs) || c.getA2().getCallsign().equals(cs)) {
                        cs1 = c.getA1().getCallsign();
                        cs2 = c.getA2().getCallsign();
                        break;
                    }
                }

                String tag;
                if (r.isSpeedResolution()) {
                    tag = "SPD:" + (int) r.getNewSpeed();
                } else if (r.isHeadingResolution()) {
                    tag = "HDG:" + (int) r.getNewHeading();
                } else {
                    tag = String.valueOf((int) r.getNewAltitude());
                }
                resolvedPairs.add(new String[]{cs1, cs2, cs, tag});
            }

            // any conflict in the cluster with both aircraft still locked = unresolved
            for (Conflict c : cluster) {
                boolean a1resolved = allResolutions.stream()
                    .anyMatch(r -> r.getAircraft().getCallsign().equals(c.getA1().getCallsign()));
                boolean a2resolved = allResolutions.stream()
                    .anyMatch(r -> r.getAircraft().getCallsign().equals(c.getA2().getCallsign()));
                if (!a1resolved && !a2resolved) {
                    unresolvedPairs.add(new String[]{c.getA1().getCallsign(), c.getA2().getCallsign()});
                }
            }
        }

        // apply resolutions
        for (Resolution r : allResolutions) {
            Aircraft ac = r.getAircraft();
            if (r.isSpeedResolution()) {
                ac.setSpeed(r.getNewSpeed());
                System.out.println("[RESOLVED-SPD] " + ac.getCallsign()
                        + " → " + (int) r.getNewSpeed() + " kt");
            } else if (r.isHeadingResolution()) {
                ac.setHeading(r.getNewHeading());
                System.out.println("[RESOLVED-HDG] " + ac.getCallsign()
                        + " → heading " + (int) r.getNewHeading() + "°");
            } else {
                ac.setAltitude(r.getNewAltitude());
                System.out.println("[RESOLVED] " + ac.getCallsign()
                        + " → " + (int) r.getNewAltitude() + " ft");
            }
        }

        if (!unresolvedPairs.isEmpty()) {
            System.out.println("[UNRESOLVED] " + unresolvedPairs.size()
                    + " conflict(s) could not be resolved this cycle");
        }

        // post-resolution sanity check
        if (!allResolutions.isEmpty()) {
            List<Conflict> postCheck = detector.detectConflicts(aircrafts);
            long newlyCreated = postCheck.stream()
                .filter(pc -> allResolutions.stream().anyMatch(res ->
                    res.getAircraft().getCallsign().equals(pc.getA1().getCallsign()) ||
                    res.getAircraft().getCallsign().equals(pc.getA2().getCallsign())))
                .count();
            if (newlyCreated > 0)
                System.out.println("[WARNING] " + newlyCreated
                        + " new conflict(s) introduced by resolution — will handle next cycle");
        }

        notifyConflicts(conflicts, resolvedPairs);
        updateMap(aircrafts);
    }

    // ── BFS cluster detection ─────────────────────────────────────────────────

    private List<List<Conflict>> detectClusters(List<Conflict> conflicts) {
        // build adjacency: callsign → set of conflicts involving that aircraft
        Map<String, Set<Conflict>> byCallsign = new HashMap<>();
        for (Conflict c : conflicts) {
            byCallsign.computeIfAbsent(c.getA1().getCallsign(), k -> new HashSet<>()).add(c);
            byCallsign.computeIfAbsent(c.getA2().getCallsign(), k -> new HashSet<>()).add(c);
        }

        Set<Conflict>       visited  = new HashSet<>();
        List<List<Conflict>> clusters = new ArrayList<>();

        for (Conflict seed : conflicts) {
            if (visited.contains(seed)) continue;
            List<Conflict>  cluster = new ArrayList<>();
            Deque<Conflict> queue   = new ArrayDeque<>();
            queue.add(seed);
            visited.add(seed);
            while (!queue.isEmpty()) {
                Conflict curr = queue.poll();
                cluster.add(curr);
                for (String cs : new String[]{curr.getA1().getCallsign(), curr.getA2().getCallsign()}) {
                    for (Conflict neighbor : byCallsign.getOrDefault(cs, Collections.emptySet())) {
                        if (visited.add(neighbor)) queue.add(neighbor);
                    }
                }
            }
            clusters.add(cluster);
        }
        return clusters;
    }

    // ── UI bridge ─────────────────────────────────────────────────────────────

    private void notifyConflicts(List<Conflict> conflicts, List<String[]> resolvedPairs) {
        WYJAppController ctrl = WYJAppController.getInstance();
        if (ctrl == null) return;
        List<String[]> allPairs = new ArrayList<>();
        for (Conflict c : conflicts)
            allPairs.add(new String[]{c.getA1().getCallsign(), c.getA2().getCallsign()});
        ctrl.batchNotify(allPairs, resolvedPairs);
    }

    private void updateMap(List<Aircraft> aircrafts) {
        WYJAppController ctrl = WYJAppController.getInstance();
        if (ctrl == null) return;
        ctrl.updateAllAircraft(aircrafts);
    }
}
