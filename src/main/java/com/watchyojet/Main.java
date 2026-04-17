package com.watchyojet;

import java.util.List;

import com.watchyojet.engine.ATCEngine;
import com.watchyojet.manager.AircraftManager;
import com.watchyojet.manager.OpenSkyFetcher;
import com.watchyojet.model.Aircraft;

public class Main {

    private static long lastApiFetchTime = 0;
    private static final long API_COOLDOWN_MS = 12000;

    public static void main(String[] args) {

        AircraftManager manager = new AircraftManager();
        OpenSkyFetcher fetcher = new OpenSkyFetcher();
        ATCEngine engine = new ATCEngine();

        System.out.println("Initializing WatchyoJet Autonomous ATC Shadow Mode...");

        while (true) {
            long currentTime = System.currentTimeMillis();

            // Fetch LIVE aircraft from OpenSky
            if (currentTime - lastApiFetchTime > API_COOLDOWN_MS) {

                System.out.println("\n[SYSTEM] Fetching LIVE traffic (PHL Airspace)...");

                List<Aircraft> livePlanes = fetcher.fetchLiveTraffic();

                manager.syncWithLiveData(livePlanes);

                System.out.println("[SYSTEM] Airspace refreshed. Tracking "
                        + manager.getAircrafts().size() + " live flights.");

                lastApiFetchTime = currentTime;
            }

            engine.runCycle(manager.getAircrafts());

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}