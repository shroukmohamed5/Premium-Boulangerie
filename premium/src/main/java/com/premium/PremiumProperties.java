package com.premium;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "premium")
public record PremiumProperties(BigDecimal deliveryFee, BigDecimal freeDeliveryOver, String timezone) {
}
