package com.github.usedrarely.spring.rate.limit.configuration;

import com.github.usedrarely.spring.rate.limit.EnableRateLimit;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;

public class RateLimitConfigurationSelector extends AdviceModeImportSelector<EnableRateLimit> {

  @Override
  protected String[] selectImports(final AdviceMode adviceMode) {
    switch (adviceMode) {
      case PROXY:
        return new String[]{ProxyRateLimitConfiguration.class.getName()};
      case ASPECTJ:
        return new String[]{AspectjRateLimitConfiguration.class.getName()};
      default:
        return null;
    }
  }

}
