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

package com.github.usedrarely.spring.rate.limit.options;

import com.github.usedrarely.spring.rate.limit.Interval;
import com.github.usedrarely.spring.rate.limit.RateLimited;
import com.github.usedrarely.spring.rate.limit.RateLimitedRetry;
import com.github.usedrarely.spring.rate.limit.options.exception.IllegalConfigurationException;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationOptionsResolverTest {

  class Tested {

    @RateLimited(maxRequests = 50, interval = @Interval(interval = 10, unit = TimeUnit.HOURS))
    public void correct() {
    }

    @RateLimited(maxRequests = 5, interval = @Interval(interval = 30, unit = TimeUnit.SECONDS))
    @RateLimitedRetry(retryCount = 4, interval = @Interval(interval = 100, unit = TimeUnit.MILLISECONDS))
    public void correctWithRetry() {

    }

    @RateLimited(enabled = false)
    public void disabled() {
    }

    @RateLimited
    public void invalidInterval() {
    }

  }

  @Mock
  private JoinPoint joinPoint;

  @Mock
  private MethodSignature methodSignature;

  private RateLimited find(final String methodName) {
    return AnnotationUtils.findAnnotation(ClassUtils.getMethod(Tested.class, methodName), RateLimited.class);
  }

  @Before
  public void setUp() {
    reset(joinPoint);
  }

  @Test(expected = IllegalConfigurationException.class)
  public void shouldFailForIllegalConfiguration() {
    initMocks("invalidInterval");
    new AnnotationOptionsResolver().resolve("test", joinPoint);
  }

  private void initMocks(final String method) {
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(joinPoint.getTarget()).thenReturn(new Tested());
    when(methodSignature.getMethod()).thenReturn(ReflectionUtils.findMethod(Tested.class, method));
  }

  @Test
  public void shouldResolveCorrect() {
    initMocks("correct");
    assertThat(new AnnotationOptionsResolver().resolve("test", joinPoint)).isEqualTo(InternalOptions.enabled("test", 50, InternalOptions.intervalOf(10L, TimeUnit.HOURS)));
  }

  @Test
  public void shouldResolveCorrectWithRetry() {
    initMocks("correctWithRetry");
    assertThat(new AnnotationOptionsResolver().resolve("test", joinPoint)).isEqualTo(InternalOptions
        .enabled("test", 5, InternalOptions.intervalOf(30L, TimeUnit.SECONDS))
        .enableRetry(4, InternalOptions.intervalOf(100L, TimeUnit.MILLISECONDS)));
  }

  @Test
  public void shouldResolveDisabled() {
    initMocks("disabled");
    assertThat(new AnnotationOptionsResolver().resolve("test", joinPoint)).isEqualTo(InternalOptions.disabled("test"));
  }

}
