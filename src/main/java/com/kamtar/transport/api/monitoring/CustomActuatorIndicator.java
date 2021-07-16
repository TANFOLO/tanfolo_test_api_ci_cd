package com.kamtar.transport.api.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomActuatorIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up()
               // .withDetail("test de javamind", "Super green")
               // .withDetail("test database", "OK call in 10ms")
                .build();
    }
    
   
}
