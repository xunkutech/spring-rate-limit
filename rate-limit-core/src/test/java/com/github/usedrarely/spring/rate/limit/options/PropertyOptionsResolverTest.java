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

import com.github.usedrarely.spring.rate.limit.options.exception.IllegalConfigurationException;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PropertyOptionsResolverTest {

  @Test(expected = IllegalConfigurationException.class)
  public void shouldFailForInvalid() {
    final PropertyOptionsResolver resolver = new PropertyOptionsResolver();
    resolver.setEnvironment(new MockEnvironment()
        .withProperty("rate.limited.invalid.enabled", "true")
        .withProperty("rate.limited.invalid.requests", "1")
        .withProperty("rate.limited.invalid.interval", "0")
    );

    resolver.resolve("invalid", mock(JoinPoint.class));
  }

  @Test(expected = IllegalConfigurationException.class)
  public void shouldFailForMissing() {
    final PropertyOptionsResolver resolver = new PropertyOptionsResolver();
    resolver.setEnvironment(new MockEnvironment()
        .withProperty("rate.limited.disabled.enabled", "false")
    );

    resolver.resolve("testDisabled", mock(JoinPoint.class));
  }

  @Test(expected = IllegalConfigurationException.class)
  public void shouldFailWithIncorrectRetry() {
    final PropertyOptionsResolver resolver = new PropertyOptionsResolver();
    resolver.setEnvironment(new MockEnvironment()
        .withProperty("rate.limited.simple.enabled", "true")
        .withProperty("rate.limited.simple.requests", "5")
        .withProperty("rate.limited.simple.interval", "6")
        .withProperty("rate.limited.simple.retry.enabled", "true")
    );

    resolver.resolve("simple", mock(JoinPoint.class));
  }

  @Test
  public void shouldResolveCorrect() {
    final PropertyOptionsResolver resolver = new PropertyOptionsResolver();
    resolver.setEnvironment(new MockEnvironment()
        .withProperty("rate.limited.simple.enabled", "true")
        .withProperty("rate.limited.simple.requests", "5")
        .withProperty("rate.limited.simple.interval", "1")

        .withProperty("rate.limited.detailed.enabled", "true")
        .withProperty("rate.limited.detailed.requests", "7")
        .withProperty("rate.limited.detailed.interval", "5")
        .withProperty("rate.limited.detailed.interval.unit", "SECONDS")
    );

    assertThat(resolver.resolve("simple", mock(JoinPoint.class))).isEqualTo(InternalOptions.enabled("simple", 5, InternalOptions.intervalOf(1L, TimeUnit.MINUTES)));
    assertThat(resolver.resolve("detailed", mock(JoinPoint.class))).isEqualTo(InternalOptions.enabled("detailed", 7, InternalOptions.intervalOf(5L, TimeUnit.SECONDS)));

  }

  @Test
  public void shouldResolveCorrectWithRetry() {
    final PropertyOptionsResolver resolver = new PropertyOptionsResolver();
    resolver.setEnvironment(new MockEnvironment()
        .withProperty("rate.limited.simple.enabled", "true")
        .withProperty("rate.limited.simple.requests", "2")
        .withProperty("rate.limited.simple.interval", "4")
        .withProperty("rate.limited.simple.retry.enabled", "true")
        .withProperty("rate.limited.simple.retry.count", "6")
        .withProperty("rate.limited.simple.retry.interval", "8")

        .withProperty("rate.limited.detailed.enabled", "true")
        .withProperty("rate.limited.detailed.requests", "9")
        .withProperty("rate.limited.detailed.interval", "3")
        .withProperty("rate.limited.detailed.interval.unit", "SECONDS")
        .withProperty("rate.limited.detailed.retry.enabled", "true")
        .withProperty("rate.limited.detailed.retry.count", "5")
        .withProperty("rate.limited.detailed.retry.interval", "7")
        .withProperty("rate.limited.detailed.retry.interval.unit", "HOURS")
    );

    assertThat(resolver.resolve("simple", mock(JoinPoint.class))).isEqualTo(
        InternalOptions.enabled("simple", 2, InternalOptions.intervalOf(4L, TimeUnit.MINUTES))
            .enableRetry(6, InternalOptions.intervalOf(8L, TimeUnit.MINUTES)));

    assertThat(resolver.resolve("detailed", mock(JoinPoint.class))).isEqualTo(
        InternalOptions.enabled("detailed", 9, InternalOptions.intervalOf(3L, TimeUnit.SECONDS))
            .enableRetry(5, InternalOptions.intervalOf(7L, TimeUnit.HOURS)));

  }


  @Test
  public void shouldResolveDisabled() {
    final PropertyOptionsResolver resolver = new PropertyOptionsResolver();
    resolver.setEnvironment(new MockEnvironment()
        .withProperty("rate.limited.testDisabled.enabled", "false")
    );

    assertThat(resolver.resolve("testDisabled", mock(JoinPoint.class))).isEqualTo(InternalOptions.disabled("testDisabled"));
  }
}
