package com.cryo.alert.service;
import com.cryo.alert.dto.FreezerDto;
import com.cryo.alert.dto.FreezerReadingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class WhatsappSmsService implements SmsNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsappSmsService.class);

    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${whatsapp.access.token}")
    private String accessToken;

    private final RestTemplate restTemplate;

    public WhatsappSmsService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    // 1. Send Alert with Button (UPDATED)
    @Override
    public void sendTemperatureAlert(String mobileNumber, FreezerDto freezer, FreezerReadingDto reading) {
        try {
            String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

            // ✅ DYNAMIC HEADER & BODY
            String headerText;
            String bodyDetails;

            if (Boolean.FALSE.equals(reading.getFreezerOn())) {
                headerText = "🔌 POWER FAILURE";
                bodyDetails = "CRITICAL: Power is OFF ❌";
            } else if (Boolean.TRUE.equals(reading.getDoorOpen())) {
                headerText = "🚪 DOOR OPEN";
                bodyDetails = "WARNING: Door is OPEN ⚠️";
            } else {
                headerText = "🌡️ HIGH TEMP";
                bodyDetails = String.format("Temp: *%.2f°C* (High)", reading.getTemperature());
            }

            String bodyText = String.format(
                    "🚨 *%s: %s*\n" +
                            "Name: %s\n" +
                            "%s\n" +
                            "Check immediately.",
                    headerText,
                    freezer.getFreezerId(),
                    freezer.getName(),
                    bodyDetails
            );

            // Add ACK button
            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "button");
            interactive.put("body", Map.of("text", bodyText));
            interactive.put("action", Map.of("buttons", List.of(
                    Map.of("type", "reply", "reply", Map.of("id", "ACK_" + freezer.getFreezerId(), "title", "✅ Acknowledge"))
            )));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("to", mobileNumber);
            requestBody.put("type", "interactive");
            requestBody.put("interactive", interactive);

            sendRequest(url, requestBody);
            logger.info("✅ Alert sent to: {}", mobileNumber);
        } catch (Exception e) {
            logger.error("Error sending Alert", e);
        }
    }

    // 2. Send Main Menu (UNCHANGED)
    public void sendMainMenu(String mobileNumber) {
        try {
            String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "list");
            interactive.put("header", Map.of("type", "text", "text", "❄️ Cryo Monitor Bot"));
            interactive.put("body", Map.of("text", "Hello! Select an option below:"));
            interactive.put("footer", Map.of("text", "👇 Menu"));

            interactive.put("action", Map.of(
                    "button", "Show Options",
                    "sections", List.of(Map.of("title", "Actions", "rows", List.of(
                            Map.of("id", "MENU_DASHBOARD", "title", "📊 Dashboard Summary"),
                            Map.of("id", "MENU_TOTAL", "title", "🧊 Total Freezers"),
                            Map.of("id", "MENU_ACTIVE", "title", "⚡ Active Freezers"),
                            Map.of("id", "MENU_ALERTS", "title", "🚨 Alert Freezers"),
                            Map.of("id", "MENU_SEARCH", "title", "🔍 Search Device", "description", "Find by ID or Name")
                    )))
            ));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("to", mobileNumber);
            requestBody.put("type", "interactive");
            requestBody.put("interactive", interactive);

            sendRequest(url, requestBody);
            logger.info("✅ Menu sent to: {}", mobileNumber);
        } catch (Exception e) {
            logger.error("Error sending Menu", e);
        }
    }

    private void sendRequest(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }
}