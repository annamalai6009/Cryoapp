package com.cryo.export.service;
import com.cryo.export.dto.FreezerConfigDto;
import com.cryo.export.dto.FreezerReadingExportDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class FreezerDataService {

    private static final Logger log = LoggerFactory.getLogger(FreezerDataService.class);

    private final WebClient freezerServiceClient;
    private final ObjectMapper objectMapper;

    public FreezerDataService(
            @Value("${freezer.service.url:http://freezer-service}") String freezerServiceUrl,
            ObjectMapper objectMapper) {

        this.freezerServiceClient = WebClient.builder()
                .baseUrl(freezerServiceUrl)
                .build();
        this.objectMapper = objectMapper;
    }
    // ✅ NEW METHOD: Fetch Configuration
    public FreezerConfigDto getFreezerConfig(String freezerId, String authHeader) {
        try {
            String jsonResponse = freezerServiceClient.get()
                    .uri("/freezers/{freezerId}/config", freezerId)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(jsonResponse);
            // Assuming standard ApiResponse structure: { success: true, data: { ... } }
            if (root.has("data")) {
                return objectMapper.treeToValue(root.get("data"), FreezerConfigDto.class);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch config for freezer: {}", freezerId, e);
            return null; // Handle null gracefully in caller
        }
    }



    public List<FreezerReadingExportDto> getFreezerReadings(String freezerId,
                                                            LocalDateTime from,
                                                            LocalDateTime to,
                                                            String authorizationHeader) {

        // 1) Call freezer-service and get RAW JSON STRING
        String jsonResponse = freezerServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/freezers/{freezerId}/chart")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .build(freezerId))
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //log.info("RAW JSON from freezer-service /chart: {}", jsonResponse);
// ✅ REPLACE WITH THIS (Log only size/status):
        if (jsonResponse == null || jsonResponse.isBlank()) {
            log.warn("Received empty response from freezer-service for ID: {}", freezerId);
            return List.of();
        }

        // Log basic info without the full data payload
        log.info("Received data from freezer-service for ID: {}. Payload size: {} bytes",
                freezerId, jsonResponse.length());

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            List<FreezerReadingExportDto> result = new ArrayList<>();

            // Case 1: root is an ARRAY:  [ {...}, {...} ]
            if (root.isArray()) {
                for (JsonNode node : root) {
                    FreezerReadingExportDto dto =
                            objectMapper.treeToValue(node, FreezerReadingExportDto.class);
                    result.add(dto);
                }
                log.info("Parsed {} readings (root array)", result.size());
                return result;
            }

            // Case 2: root is OBJECT with some ARRAY field: { "points": [..], "currentStatus": {...} }
            if (root.isObject()) {
                JsonNode arrayNode = null;

                // Try common field names first
                if (root.has("points")) {
                    arrayNode = root.get("points");
                } else if (root.has("data")) {
                    arrayNode = root.get("data");
                } else if (root.has("chartData")) {
                    arrayNode = root.get("chartData");
                } else {
                    // Fallback: pick the first array field we find
                    Iterator<String> fieldNames = root.fieldNames();
                    while (fieldNames.hasNext()) {
                        String fieldName = fieldNames.next();
                        JsonNode candidate = root.get(fieldName);
                        if (candidate.isArray()) {
                            arrayNode = candidate;
                            break;
                        }
                    }
                }

                if (arrayNode != null && arrayNode.isArray()) {
                    for (JsonNode node : arrayNode) {
                        FreezerReadingExportDto dto =
                                objectMapper.treeToValue(node, FreezerReadingExportDto.class);
                        result.add(dto);
                    }
                    log.info("Parsed {} readings (array field in object)", result.size());
                    return result;
                } else {
                    log.warn("No array field found in /chart JSON – cannot extract readings.");
                }
            }

            // If we reach here, we couldn't parse properly
            log.warn("Unexpected JSON format from /chart: {}", jsonResponse);
            return List.of();

        } catch (Exception e) {
            log.error("Failed to parse /chart JSON from freezer-service", e);
            return List.of();
        }
    }
}
