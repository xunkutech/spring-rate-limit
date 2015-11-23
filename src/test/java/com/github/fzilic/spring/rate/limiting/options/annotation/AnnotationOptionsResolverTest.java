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

package com.github.fzilic.spring.rate.limiting.options.annotation;

import com.github.fzilic.spring.rate.limiting.Interval;
import com.github.fzilic.spring.rate.limiting.RateLimited;
import com.github.fzilic.spring.rate.limiting.RateLimitedRetry;
import com.github.fzilic.spring.rate.limiting.options.exception.IllegalConfigurationException;
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

    @RateLimited(configuration = RateLimited.Configuration.DATABASE)
    public void unsupported() {
    }
  }

  @Mock
  private JoinPoint joinPoint;

  @Mock
  private MethodSignature methodSignature;

  @Before
  public void setUp() {
    reset(joinPoint);
  }

  @Test(expected = IllegalConfigurationException.class)
  public void shouldFailForIllegalConfiguration() {
    new AnnotationOptionsResolver().resolve("test", find("invalidInterval"), joinPoint);
  }

  private RateLimited find(final String methodName) {
    return AnnotationUtils.findAnnotation(ClassUtils.getMethod(Tested.class, methodName), RateLimited.class);
  }

  @Test(expected = IllegalConfigurationException.class)
  public void shouldFailForUnsupported() {
    new AnnotationOptionsResolver().resolve("test", find("unsupported"), joinPoint);
  }

  @Test
  public void shouldResolveCorrect() {
    assertThat(new AnnotationOptionsResolver().resolve("test", find("correct"), joinPoint)).isEqualTo(AnnotationOptions.enabled(50, AnnotationOptions.intervalOf(10L, TimeUnit.HOURS)));
  }

  @Test
  public void shouldResolveCorrectWithRetry() {
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(joinPoint.getTarget()).thenReturn(new Tested());
    when(methodSignature.getMethod()).thenReturn(ReflectionUtils.findMethod(Tested.class, "correctWithRetry"));
    assertThat(new AnnotationOptionsResolver().resolve("test", find("correctWithRetry"), joinPoint)).isEqualTo(AnnotationOptions
        .enabled(5, AnnotationOptions.intervalOf(30L, TimeUnit.SECONDS))
        .enableRetry(4, AnnotationOptions.intervalOf(100L, TimeUnit.MILLISECONDS)));
  }

  @Test
  public void shouldResolveDisabled() {
    assertThat(new AnnotationOptionsResolver().resolve("test", find("disabled"), joinPoint)).isEqualTo(AnnotationOptions.disabled());
  }

}
