package com.github.usedrarely.spring.rate.limit.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
public class ProxyRateLimitConfiguration {

  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public RateLimitAnnotationBeanPostProcessor rateLimitAdvisor() {

    return null;
  }
}
