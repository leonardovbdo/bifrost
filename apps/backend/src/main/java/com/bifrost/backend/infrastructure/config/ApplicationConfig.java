package com.bifrost.backend.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BifrostProperties.class)
public class ApplicationConfig {}
