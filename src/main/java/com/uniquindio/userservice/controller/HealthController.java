package com.uniquindio.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Instant START_TIME = Instant.now();
    private static final String VERSION = "1.0.0"; // Puedes obtener esto de BuildProperties si está disponible

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("version", VERSION);

        Duration uptime = Duration.between(START_TIME, Instant.now());
        response.put("uptime", formatUptime(uptime));
        response.put("uptimeSeconds", uptime.getSeconds());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("version", VERSION);

        Duration uptime = Duration.between(START_TIME, Instant.now());
        response.put("uptime", formatUptime(uptime));
        response.put("uptimeSeconds", uptime.getSeconds());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> live() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "LIVE");
        response.put("version", VERSION);

        Duration uptime = Duration.between(START_TIME, Instant.now());
        response.put("uptime", formatUptime(uptime));
        response.put("uptimeSeconds", uptime.getSeconds());

        return ResponseEntity.ok(response);
    }

    private String formatUptime(Duration duration) {
        long seconds = duration.getSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, secs);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}

