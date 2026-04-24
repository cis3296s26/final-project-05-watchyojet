package com.watchyojet.simulation;

import java.util.Arrays;
import java.util.List;

import com.watchyojet.model.Aircraft;
import com.watchyojet.model.AircraftType;

public class DemoScenario {

    // Pair A — head-on convergence, altDiff 500 ft → CRITICAL → altitude resolution
    // Pair B — crossing paths, altDiff 500 ft → HIGH → altitude resolution
    // Pair C — altDiff exactly 1499 ft → SAFE_VERTICAL_SEPARATION gate bails → heading resolution
    // BKGD    — realistic background traffic, no conflicts

    public static List<Aircraft> build() {
        return Arrays.asList(
            new Aircraft("DEMO01", 40.00, -75.30, 22000, 300, 180, AircraftType.B737),
            new Aircraft("DEMO02", 39.80, -75.32, 21500, 300,   2, AircraftType.B737),

            new Aircraft("DEMO03", 40.05, -75.65, 28000, 320,  90, AircraftType.A320),
            new Aircraft("DEMO04", 40.05, -75.30, 27500, 320, 270, AircraftType.A320),

            new Aircraft("DEMO05", 39.58, -75.55, 12000, 280,  90, AircraftType.B737),
            new Aircraft("DEMO06", 39.50, -75.15, 13499, 280, 270, AircraftType.B737),

            new Aircraft("BKGD01", 40.50, -74.90, 35000, 260, 220, AircraftType.A350),
            new Aircraft("BKGD02", 39.20, -76.10, 32000, 280,  50, AircraftType.A320),
            new Aircraft("BKGD03", 40.40, -75.50,  7500, 200, 150, AircraftType.GENERIC),
            new Aircraft("BKGD04", 39.20, -74.80,  6000, 190, 330, AircraftType.GENERIC)
        );
    }
}
