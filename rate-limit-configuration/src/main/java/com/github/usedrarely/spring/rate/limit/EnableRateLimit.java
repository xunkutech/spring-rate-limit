package com.github.usedrarely.spring.rate.limit;

import com.github.usedrarely.spring.rate.limit.configuration.RateLimitConfigurationSelector;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RateLimitConfigurationSelector.class)
public @interface EnableRateLimit {

  /**
   * Indicate how async advice should be applied. The default is
   * {@link AdviceMode#PROXY}.
   *
   * @see AdviceMode
   */
  AdviceMode mode() default AdviceMode.PROXY;

  /**
   * Indicate the order in which the
   * {@link org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor}
   * should be applied. The default is {@link Ordered#LOWEST_PRECEDENCE} in order to run
   * after all other post-processors, so that it can add an advisor to
   * existing proxies rather than double-proxy.
   */
  int order() default Ordered.LOWEST_PRECEDENCE;

}
