package com.watchyojet.simulation;

import java.util.List;

import com.watchyojet.engine.ATCEngine;
import com.watchyojet.manager.AircraftManager;
import com.watchyojet.manager.OpenSkyFetcher;
import com.watchyojet.model.Aircraft;

public class Main {
    public static void main(String[] args) {
        
        AircraftManager manager = new AircraftManager();
        OpenSkyFetcher fetcher = new OpenSkyFetcher();
        ATCEngine engine = new ATCEngine();

        long lastApiFetchTime = 0;
        long API_COOLDOWN_MS = 12000; // 12 second cooldown to prevent OpenSky IP Ban

        System.out.println("Initializing WatchyoJet Autonomous ATC Shadow Mode...");

        while (true) {
            long currentTime = System.currentTimeMillis();

            // Fetch live data every 12 seconds
            if (currentTime - lastApiFetchTime > API_COOLDOWN_MS) {
                System.out.println("\n[SYSTEM] Pinging OpenSky Network for live traffic (PHL Airspace)...");
                
                List<Aircraft> livePlanes = fetcher.fetchLiveTraffic();
                manager.syncWithLiveData(livePlanes);
                
                System.out.println("[SYSTEM] Airspace refreshed. Tracking " + manager.getAircrafts().size() + " live flights.");
                lastApiFetchTime = currentTime;
            }

            // Feed the updated planes into our decision algorithm
            engine.runCycle(manager.getAircrafts());

            // Sleep before next cycle 
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}