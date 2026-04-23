package com.watchyojet.manager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchyojet.model.Aircraft;
import com.watchyojet.model.AircraftType;

public class OpenSkyFetcher {

    // PHL TRACON area: ~60 NM radius around Philadelphia International (39.87N, 75.24W)
    private static final String OPENSKY_URL =
        "https://opensky-network.org/api/states/all?lamin=38.8&lomin=-76.5&lamax=40.9&lomax=-74.0";

    private final HttpClient   client;
    private final ObjectMapper mapper;

    public OpenSkyFetcher() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    public List<Aircraft> fetchLiveTraffic() {
        List<Aircraft> liveAircraft = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENSKY_URL))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("[FETCHER] API rate limited or error. Status: "
                        + response.statusCode());
                return liveAircraft;
            }

            JsonNode rootNode  = mapper.readTree(response.body());
            JsonNode statesNode = rootNode.get("states");

            if (statesNode == null || !statesNode.isArray()) return liveAircraft;

            for (JsonNode state : statesNode) {

                // OpenSky state vector has 17 fields (indices 0–16).
                if (state.size() < 11) continue;

                // Fields required for safe operation — skip if any are null.
                if (state.get(1).isNull()  ||   // callsign
                    state.get(5).isNull()  ||   // longitude
                    state.get(6).isNull()  ||   // latitude
                    state.get(9).isNull()  ||   // velocity (m/s)
                    state.get(10).isNull())      // true_track (heading)
                    continue;

                // Altitude: prefer baro_altitude (field 13) — this is what ATC uses.
                // Fall back to geo_altitude (field 7) only if baro is missing.
                double altMeters;
                if (state.size() > 13 && !state.get(13).isNull()) {
                    altMeters = state.get(13).asDouble();
                } else if (!state.get(7).isNull()) {
                    altMeters = state.get(7).asDouble();
                } else {
                    continue; // no usable altitude
                }

                String callsign = state.get(1).asText().trim();
                if (callsign.isEmpty()) callsign = state.get(0).asText(); // ICAO hex fallback

                double lon          = state.get(5).asDouble();
                double lat          = state.get(6).asDouble();
                double altitudeFeet = altMeters * 3.28084;
                double speedKnots   = state.get(9).asDouble() * 1.94384;
                double heading      = state.get(10).asDouble();

                AircraftType type = inferType(speedKnots, altitudeFeet);

                liveAircraft.add(new Aircraft(callsign, lat, lon,
                        altitudeFeet, speedKnots, heading, type));
            }

        } catch (Exception e) {
            System.out.println("[FETCHER] Failed to fetch OpenSky data: " + e.getMessage());
        }
        return liveAircraft;
    }

    // Infer aircraft type from observable performance to enable priority rules.
    private AircraftType inferType(double speedKnots, double altFeet) {
        if (speedKnots > 400 || altFeet > 25000) return AircraftType.A320;
        if (speedKnots > 200)                    return AircraftType.B737;
        return AircraftType.C172;
    }
}
