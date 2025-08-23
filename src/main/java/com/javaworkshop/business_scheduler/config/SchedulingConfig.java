package com.javaworkshop.business_scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

// This class enables scheduling in the application unless the "disable-scheduling" profile is active.
@Configuration
@EnableScheduling
@Profile("!disable-scheduling")
public class SchedulingConfig {
}
