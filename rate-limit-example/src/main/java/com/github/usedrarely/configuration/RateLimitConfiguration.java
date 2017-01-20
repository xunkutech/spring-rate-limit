/*
 * Copyright (c) 2017 Franjo Žilić <frenky666@gmail.com>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

package com.github.usedrarely.configuration;

import com.github.usedrarely.spring.rate.limit.aspect.RateLimitingAdvice;
import com.github.usedrarely.spring.rate.limit.checker.RateChecker;
import com.github.usedrarely.spring.rate.limit.key.DefaultKeyGenerator;
import com.github.usedrarely.spring.rate.limit.key.KeyGenerator;
import com.github.usedrarely.spring.rate.limit.options.AnnotationOptionsResolver;
import com.github.usedrarely.spring.rate.limit.options.OptionsResolver;
import com.github.usedrarely.spring.rate.limit.redis.checker.RedisRateChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

/**
 * @author Franjo Zilic
 */
@Configuration
public class RateLimitConfiguration {


  @Bean
  public KeyGenerator keyGenerator() {
    return new DefaultKeyGenerator();
  }

  @Bean
  public OptionsResolver optionsResolver() {
    return new AnnotationOptionsResolver();
  }

  @Bean
  public RateLimitingAdvice rateLimitingAdvice(final KeyGenerator keyGenerator, final OptionsResolver optionsResolver, final RateChecker rateChecker) {
    return new RateLimitingAdvice(keyGenerator, optionsResolver, rateChecker);
  }

  @Bean
  public RateChecker redisTokenBucketRateChecker(final RedisOperations<String, String> redisOperations) {
    return new RedisRateChecker(redisOperations);
  }

}
