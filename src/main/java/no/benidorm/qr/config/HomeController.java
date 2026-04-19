package no.benidorm.qr.config;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    Map<String, String> home() {
        return Map.of(
                "name", "qr-backend",
                "health", "/health",
                "actuatorHealth", "/actuator/health",
                "publicDemo", "/api/public/q/demo"
        );
    }

    @GetMapping("/health")
    Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
