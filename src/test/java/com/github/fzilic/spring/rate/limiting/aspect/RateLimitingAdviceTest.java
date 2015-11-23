/*
 * Copyright (c) 2015 Franjo Žilić <frenky666@gmail.com>
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

package com.github.fzilic.spring.rate.limiting.aspect;


import com.github.fzilic.spring.rate.limiting.CallBlockedException;
import com.github.fzilic.spring.rate.limiting.Interval;
import com.github.fzilic.spring.rate.limiting.RateLimitExceededException;
import com.github.fzilic.spring.rate.limiting.RateLimited;
import com.github.fzilic.spring.rate.limiting.RateLimitedRetry;
import com.github.fzilic.spring.rate.limiting.checker.RateChecker;
import com.github.fzilic.spring.rate.limiting.key.DefaultKeyGenerator;
import com.github.fzilic.spring.rate.limiting.options.Options;
import com.github.fzilic.spring.rate.limiting.options.OptionsInterval;
import com.github.fzilic.spring.rate.limiting.options.OptionsResolver;
import com.github.fzilic.spring.rate.limiting.options.annotation.AnnotationOptions;
import com.github.fzilic.spring.rate.limiting.options.annotation.AnnotationOptionsResolver;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.JoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RateLimitingAdviceTest {

  interface LimitedInterface {

    void aMethod();

    void bMethod();

    void cMethod();
  }

  class LimitedService implements LimitedInterface {
    @Override
    @RateLimited(key = "test", maxRequests = 10, interval = @Interval(interval = 1))
    public void aMethod() {
    }

    @Override
    @RateLimited(key = "test", maxRequests = 10, interval = @Interval(interval = 1))
    @RateLimitedRetry(retryCount = 2, interval = @Interval(interval = 100, unit = TimeUnit.MILLISECONDS))
    public void bMethod() {
    }

    @Override
    @RateLimited(key = "blocked")
    public void cMethod() {

    }
  }

  @Mock
  RateChecker rateChecker;

  @Before
  public void setUp() {
    reset(rateChecker);
  }

  @Test
  public void shouldAllow() {
    final AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new LimitedService());
    proxyFactory.addAspect(new RateLimitingAdvice(new DefaultKeyGenerator(), new AnnotationOptionsResolver(), rateChecker));
    final LimitedInterface limited = proxyFactory.getProxy();

    final OptionsInterval value = AnnotationOptions.intervalOf(1L, TimeUnit.MINUTES);
    when(rateChecker.check(eq("test"), eq(10L), eq(value))).thenReturn(true);
    limited.aMethod();
    verify(rateChecker).check(eq("test"), eq(10L), eq(value));
  }

  @Test
  public void shouldAllowWithRetry() {
    final AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new LimitedService());
    proxyFactory.addAspect(new RateLimitingAdvice(new DefaultKeyGenerator(), new AnnotationOptionsResolver(), rateChecker));
    final LimitedInterface limited = proxyFactory.getProxy();

    final OptionsInterval value = AnnotationOptions.intervalOf(1L, TimeUnit.MINUTES);
    when(rateChecker.check(eq("test"), eq(10L), eq(value))).thenReturn(false, false, true);
    limited.bMethod();
    verify(rateChecker, times(3)).check(eq("test"), eq(10L), eq(value));

  }

  @Test(expected = CallBlockedException.class)
  public void shouldBlock() {
    final AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new LimitedService());
    final OptionsResolver optionsResolver = mock(OptionsResolver.class);
    proxyFactory.addAspect(new RateLimitingAdvice(new DefaultKeyGenerator(), optionsResolver, rateChecker));
    final LimitedInterface limited = proxyFactory.getProxy();

    final Options options = mock(Options.class);
    when(optionsResolver.supports(eq("blocked"), any(RateLimited.class))).thenReturn(true);
    when(optionsResolver.resolve(eq("blocked"), any(RateLimited.class), any(JoinPoint.class))).thenReturn(options);
    when(options.blocked()).thenReturn(true);

    limited.cMethod();
  }

  @Test(expected = RateLimitExceededException.class)
  public void shouldFail() {
    final AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new LimitedService());
    proxyFactory.addAspect(new RateLimitingAdvice(new DefaultKeyGenerator(), new AnnotationOptionsResolver(), rateChecker));
    final LimitedInterface limited = proxyFactory.getProxy();

    final OptionsInterval value = AnnotationOptions.intervalOf(1L, TimeUnit.MINUTES);
    when(rateChecker.check(eq("test"), eq(10L), eq(value))).thenReturn(false);
    limited.aMethod();
    verify(rateChecker).check(eq("test"), eq(10L), eq(value));
  }

  @Test(expected = RateLimitExceededException.class)
  public void shouldFailWithRetry() {
    final AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new LimitedService());
    proxyFactory.addAspect(new RateLimitingAdvice(new DefaultKeyGenerator(), new AnnotationOptionsResolver(), rateChecker));
    final LimitedInterface limited = proxyFactory.getProxy();

    final OptionsInterval value = AnnotationOptions.intervalOf(1L, TimeUnit.MINUTES);
    when(rateChecker.check(eq("test"), eq(10L), eq(value))).thenReturn(false, false, false, true);
    limited.bMethod();
    verify(rateChecker, times(3)).check(eq("test"), eq(10L), eq(value));

  }

}
