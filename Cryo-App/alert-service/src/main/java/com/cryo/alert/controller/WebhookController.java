package com.cryo.alert.controller;
import com.cryo.alert.dto.FreezerStatusResponse;
import com.cryo.alert.repository.FreezerAlertStateRepository;
import com.cryo.alert.service.WhatsappSmsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Value("${whatsapp.verify.token:cryo_secret_token}")
    private String verifyToken;
    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;
    @Value("${whatsapp.access.token}")
    private String accessToken;
    @Value("${freezer.service.url:http://localhost:8082}")
    private String freezerServiceUrl;
    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Autowired
    private FreezerAlertStateRepository stateRepository;
    @Autowired
    private WhatsappSmsService whatsappSmsService;
    @Autowired
    private ObjectMapper mapper;

    private final RestTemplate restTemplate;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public WebhookController(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // 1. Verify Webhook
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) return ResponseEntity.ok(challenge);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // 2. Receive Updates
    @PostMapping
    public ResponseEntity<Void> receiveWhatsappUpdate(@RequestBody String payload) {
        try {
            JsonNode root = mapper.readTree(payload);
            if (root.has("entry") && root.get("entry").isArray()) {
                JsonNode value = root.get("entry").get(0).get("changes").get(0).get("value");
                if (value.has("messages")) {
                    JsonNode message = value.get("messages").get(0);
                    String mobile = message.get("from").asText();

                    // ✅ UPDATED: Handle "Hello" AND Search Text
                    if (message.has("text")) {
                        String text = message.get("text").get("body").asText().trim();
                        if (text.equalsIgnoreCase("Hello") || text.equalsIgnoreCase("Hi")) {
                            handleHello(mobile);
                        } else {
                            // If text is NOT Hello, assume it is a Freezer ID search
                            handleFreezerSearch(text, mobile);
                        }
                    }

                    if (message.has("interactive")) {
                        JsonNode interactive = message.get("interactive");
                        String responseId = "";
                        if (interactive.has("button_reply")) responseId = interactive.get("button_reply").get("id").asText();
                        if (interactive.has("list_reply")) responseId = interactive.get("list_reply").get("id").asText();

                        if (responseId.startsWith("ACK_")) {
                            String freezerId = responseId.substring(4);
                            stateRepository.findById(freezerId).ifPresent(state -> {
                                state.setAcknowledged(true);
                                stateRepository.save(state);
                                sendText(mobile, "✅ Acknowledged: " + freezerId);
                            });
                        } else if (responseId.startsWith("MENU_")) {
                            handleMenu(responseId, mobile);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Webhook Error", e);
        }
        return ResponseEntity.ok().build();
    }

    // --- Helpers ---

    private void handleHello(String mobile) {
        // Simple logic: If they say Hello, just show the menu.
        // We do strict Auth checks only when they ask for data.
        whatsappSmsService.sendMainMenu(mobile);
    }

    // ✅ NEW: Handle Search Logic (Robust Version)
    private void handleFreezerSearch(String query, String mobile) {
        logger.info("🔍 Searching for '{}' (User: {})", query, mobile);

        String ownerId = null;

        // 1. ROBUST AUTH LOOKUP (Try +91 and 91)
        try {
            try {
                // Try WITH +
                String url1 = authServiceUrl + "/auth/internal/mobile/+" + mobile;
                ownerId = restTemplate.getForObject(url1, String.class);
            } catch (Exception e) {
                // Try WITHOUT +
                String url2 = authServiceUrl + "/auth/internal/mobile/" + mobile;
                ownerId = restTemplate.getForObject(url2, String.class);
            }
        } catch (Exception e) {
            logger.error("Auth Error", e);
        }

        if (ownerId == null || ownerId.isEmpty()) {
            sendText(mobile, "🚫 Access Denied. Your number is not registered.");
            return;
        }

        // 2. Search Logic
        try {
            String freezerUrl = freezerServiceUrl + "/freezers/api/internal/" + ownerId;
            FreezerStatusResponse[] allFreezers = restTemplate.getForObject(freezerUrl, FreezerStatusResponse[].class);

            if (allFreezers == null || allFreezers.length == 0) {
                sendText(mobile, "❌ You have no freezers.");
                return;
            }

            List<FreezerStatusResponse> matches = new ArrayList<>();
            for (var f : allFreezers) {
                String fId = (f.getFreezerId() != null) ? f.getFreezerId().toLowerCase() : "";
                String fName = (f.getName() != null) ? f.getName().toLowerCase() : ""; // Uses getName()
                String q = query.toLowerCase();

                if (fId.contains(q) || fName.contains(q)) {
                    matches.add(f);
                }
            }

            if (matches.isEmpty()) {
                sendText(mobile, "🔍 No freezer found for: '" + query + "'");
            } else {
                for (int i = 0; i < Math.min(matches.size(), 3); i++) {
                    sendFreezerDetails(mobile, matches.get(i));
                }
            }
        } catch (Exception e) {
            logger.error("Search Error", e);
            sendText(mobile, "⚠️ System Error.");
        }
    }

    // ✅ NEW: Send Beautiful Detail Card
    private void sendFreezerDetails(String mobile, FreezerStatusResponse f) {
        String icon = Boolean.TRUE.equals(f.getIsRedAlert()) ? "🔴" : "🟢";
        String temp = (f.getCurrentTemp() != null) ? String.format("%.1f", f.getCurrentTemp()) : "N/A";
        String status = Boolean.TRUE.equals(f.getIsFreezerOn()) ? "ON" : "OFF";
        String alertStatus = Boolean.TRUE.equals(f.getIsRedAlert()) ? "ACTIVE" : "None";

        String msg = String.format(
                "%s *DEVICE DETAILS*\n" +
                        "────────────────\n" +
                        "🆔 *ID:* %s\n" +
                        "🏷️ *Name:* %s\n" +
                        "🌡️ *Temp:* %s°C\n" +
                        "⚡ *Power:* %s\n" +
                        "⚠️ *Alert:* %s",
                icon, f.getFreezerId(), f.getName(), temp, status, alertStatus
        );
        sendText(mobile, msg);
    }

    // ✅ UPDATED: Handle Menu Selection
    private void handleMenu(String menuId, String mobile) {

        // 1. HANDLE NEW SEARCH OPTION
        if (menuId.equals("MENU_SEARCH")) {
            sendText(mobile, "🔍 *Search Mode*\n\nPlease type the *Freezer ID* or *Name* below and hit send.\n(Example: 'DL2025' or 'Lab')");
            return;
        }

        // --- EXISTING MENU LOGIC BELOW (Unchanged) ---
        // Auth Lookup for Dashboard/Lists
        String authUrl = authServiceUrl + "/auth/internal/mobile/+" + mobile;
        String ownerId;
        try { ownerId = restTemplate.getForObject(authUrl, String.class); } catch(Exception e) { return; }

        String freezerUrl = freezerServiceUrl + "/freezers/api/internal/" + ownerId;
        FreezerStatusResponse[] allFreezers = restTemplate.getForObject(freezerUrl, FreezerStatusResponse[].class);
        if(allFreezers == null) allFreezers = new FreezerStatusResponse[0];

        // DASHBOARD
        if (menuId.equals("MENU_DASHBOARD")) {
            long on = 0, alerts = 0;
            for(var f : allFreezers) {
                if(Boolean.TRUE.equals(f.getIsFreezerOn())) on++;
                if(Boolean.TRUE.equals(f.getIsRedAlert())) alerts++;
            }
            String msg = String.format("📊 *EXECUTIVE SUMMARY*\n🕒 %s\n────────────────\n🔹 Total:   %d\n✅ Online:  %d\n⚠️ Alerts:  %d\n🔌 Offline: %d",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM HH:mm")),
                    allFreezers.length, on, alerts, (allFreezers.length - on));
            sendText(mobile, msg);
            return;
        }

        // FILTERS
        List<FreezerStatusResponse> filteredList = new ArrayList<>();
        String title = "📋 STATUS REPORT";
        switch (menuId) {
            case "MENU_ALERTS": title = "⚠️ ALERTS TABLE"; for (var f : allFreezers) if (Boolean.TRUE.equals(f.getIsRedAlert())) filteredList.add(f); break;
            case "MENU_ACTIVE": title = "⚡ ACTIVE FREEZERS"; for (var f : allFreezers) if (Boolean.TRUE.equals(f.getIsFreezerOn())) filteredList.add(f); break;
            default: title = "📋 FULL REPORT"; filteredList = Arrays.asList(allFreezers); break;
        }

        if (filteredList.isEmpty()) { sendText(mobile, "ℹ️ No freezers found."); return; }

        // BUILD TABLE
        List<FreezerStatusResponse> critical = new ArrayList<>();
        List<FreezerStatusResponse> healthy = new ArrayList<>();
        List<FreezerStatusResponse> offline = new ArrayList<>();

        for (var f : filteredList) {
            if (Boolean.TRUE.equals(f.getIsRedAlert())) critical.add(f);
            else if (Boolean.TRUE.equals(f.getIsFreezerOn())) healthy.add(f);
            else offline.add(f);
        }

        StringBuilder msg = new StringBuilder();
        msg.append("*").append(title).append("*\n").append("```\n");

        if (!critical.isEmpty()) {
            msg.append("[ ⚠️ CRITICAL ]\n");
            for (var f : critical) msg.append(formatRow(f));
        }
        if (!healthy.isEmpty()) {
            msg.append("[ ✅ NORMAL ]\n");
            for (var f : healthy) msg.append(formatRow(f));
        }
        if (!offline.isEmpty()) {
            msg.append("[ 🔌 OFFLINE ]\n");
            for (var f : offline) msg.append(String.format("%-12s | %6s\n", f.getFreezerId().length()>12?f.getFreezerId().substring(0,12):f.getFreezerId(), "OFF"));
        }
        msg.append("```");
        sendText(mobile, msg.toString());
    }

    private String formatRow(FreezerStatusResponse f) {
        String id = f.getFreezerId();
        if(id.length() > 12) id = id.substring(0, 12);
        String temp = (f.getCurrentTemp() != null) ? String.format("%.1f", f.getCurrentTemp()) : "N/A";
        return String.format("%-12s | %6s\n", id, temp);
    }

    private void sendText(String to, String body) {
        try {
            String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("messaging_product", "whatsapp");
            bodyMap.put("to", to);
            bodyMap.put("type", "text");
            bodyMap.put("text", Map.of("body", body));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            restTemplate.postForEntity(url, new HttpEntity<>(bodyMap, headers), String.class);
        } catch (Exception e) {
            logger.error("Send Text Error", e);
        }
    }
}