package com.watchyojet;

import java.util.List;

import com.watchyojet.engine.ATCEngine;
import com.watchyojet.manager.AircraftManager;
import com.watchyojet.manager.OpenSkyFetcher;
import com.watchyojet.model.Aircraft;
import com.watchyojet.simulation.DemoScenario;

public class Main {

    // flip to false to switch back to live OpenSky data
    public static final boolean DEMO_MODE = false;

    private static final long API_COOLDOWN_MS   = 12_000;
    private static final long DEMO_RESET_MS     = 60_000; // reset scenario every 60 s

    private static long lastApiFetchTime  = -API_COOLDOWN_MS;
    private static long lastDemoResetTime = Long.MIN_VALUE;

    public static void main(String[] args) {

        AircraftManager manager = new AircraftManager();
        OpenSkyFetcher  fetcher = new OpenSkyFetcher();
        ATCEngine       engine  = new ATCEngine();

        System.out.println("Initializing WatchyoJet Autonomous ATC Shadow Mode...");
        if (DEMO_MODE) System.out.println("[DEMO] Running controlled scenario — 10 aircraft, 3 conflict pairs");

        while (true) {
            long now = System.currentTimeMillis();

            if (DEMO_MODE) {
                if (now - lastDemoResetTime > DEMO_RESET_MS) {
                    manager.reset(DemoScenario.build());
                    System.out.println("\n[DEMO] Scenario reset. Tracking "
                            + manager.getAircrafts().size() + " aircraft.");
                    lastDemoResetTime = now;
                }
            } else {
                if (now - lastApiFetchTime > API_COOLDOWN_MS) {
                    System.out.println("\n[SYSTEM] Fetching LIVE traffic (PHL Airspace)...");
                    List<Aircraft> live = fetcher.fetchLiveTraffic();
                    manager.syncWithLiveData(live);
                    System.out.println("[SYSTEM] Airspace refreshed. Tracking "
                            + manager.getAircrafts().size() + " live flights.");
                    lastApiFetchTime = now;
                }
            }

            engine.runCycle(manager.getAircrafts());

            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
}