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
    // We set a bounding box around Philadelphia International Airport (PHL)
    // PHL TRACON area: ~60 NM radius around Philadelphia International (39.87N, 75.24W)
    private static final String OPENSKY_URL = "https://opensky-network.org/api/states/all?lamin=38.8&lomin=-76.5&lamax=40.9&lomax=-74.0";
    
    private final HttpClient client;
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

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("API Rate Limited or Error. Status code: " + response.statusCode());
                return liveAircraft; // Return empty list, dont crash
            }

            JsonNode rootNode = mapper.readTree(response.body());
            JsonNode statesNode = rootNode.get("states");

            if (statesNode != null && statesNode.isArray()) {
                for (JsonNode state : statesNode) {
                    // Skip data missing crucial positioning stats to avoid null errors
                    if (state.size() < 11 ||
                            state.get(1).isNull() ||
                            state.get(5).isNull() ||
                            state.get(6).isNull() ||
                            state.get(7).isNull() ||
                            state.get(9).isNull() ||
                            state.get(10).isNull()) 
                            {
                             continue;
                          }

                    String callsign = state.get(1).asText().trim();
                    if (callsign.isEmpty()) callsign = state.get(0).asText(); // fallback to ICAO 24-bit address

                    double lon = state.get(5).asDouble();
                    double lat = state.get(6).asDouble();
                    
                    // Convert OpenSky meters to feet
                    double altitudeFeet = state.get(7).asDouble() * 3.28084;
                    // Convert OpenSky m/s to Knots (Nautical mi/hr)
                    double speedKnots = state.get(9).asDouble() * 1.94384;
                    double heading = state.get(10).asDouble();

                    // Defaulting to generic for now, we can write logic later to guess the plane type based on speed/altitude
                    AircraftType type = AircraftType.GENERIC;

                    Aircraft aircraft = new Aircraft(callsign, lat, lon, altitudeFeet, speedKnots, heading, type);
                    liveAircraft.add(aircraft);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch OpenSky data: " + e.getMessage());
        }
        return liveAircraft;
    }
}