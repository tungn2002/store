package com.personal.store_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import com.stripe.Stripe;

@Configuration
public class StripeConfig {

    @Value("${stripe.sr-key}")
    private String srKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey=srKey;
    }
}
