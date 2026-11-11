package com.mycloud.backend.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/api/health")
    public HealthResponse getStatus() {
        String status = healthService.getStatus();
        return new HealthResponse(status);
    }
}
