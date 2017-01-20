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

package com.github.usedrarely.spring.rate.limit.aspect;

import com.github.usedrarely.spring.rate.limit.Interval;
import com.github.usedrarely.spring.rate.limit.RateLimited;
import com.github.usedrarely.spring.rate.limit.checker.RateChecker;
import com.github.usedrarely.spring.rate.limit.key.KeyGenerator;
import com.github.usedrarely.spring.rate.limit.options.Options;
import com.github.usedrarely.spring.rate.limit.options.OptionsInterval;
import com.github.usedrarely.spring.rate.limit.options.OptionsResolver;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class RateLimitingAdvicePointcutTest {

  interface LimitedInterface {
    void aMethod();

    void bMethod();
  }

  class LimitedMethodLevel implements LimitedInterface {

    @Override
    @RateLimited
    public void aMethod() {
    }

    @Override
    public void bMethod() {
    }
  }

  @RateLimited()
  class LimitedTypeAndMethodLevel implements LimitedInterface {

    @Override
    @RateLimited(interval = @Interval(interval = -1, unit = TimeUnit.DAYS))
    public void aMethod() {

    }

    @Override
    public void bMethod() {

    }
  }

  @RateLimited()
  class LimitedTypeLevel implements LimitedInterface {
    @Override
    public void aMethod() {
    }

    @Override
    public void bMethod() {
    }
  }

  @Test
  public void shouldExecuteOnMethodLevel() {
    final OptionsResolver configurationResolver = mock(OptionsResolver.class);

    final AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new LimitedMethodLevel());
    final KeyGenerator keyGenerator = mock(KeyGenerator.class);
    when(keyGenerator.key(anyString(), anyString(), any(JoinPoint.class))).thenReturn("key");
    final RateChecker rateChecker = mock(RateChecker.class);
    when(rateChecker.check(anyString(), anyLong(), any(OptionsInterval.class))).thenReturn(true);
    proxyFactory.addAspect(new RateLimitingAdvice(keyGenerator, configurationResolver, rateChecker));
    final LimitedInterface test = proxyFactory.getProxy();

    final Options mock = mock(Options.class);
    when(mock.enabled()).thenReturn(true);
    when(mock.retryEnabled()).thenReturn(false);
    when(configurationResolver.resolve(anyString(), any(JoinPoint.class))).thenReturn(mock);

    test.aMethod();
    test.bMethod();

    final ArgumentCaptor<JoinPoint> joinPointCaptor = ArgumentCaptor.forClass(JoinPoint.class);

    verify(configurationResolver, times(1)).resolve(anyString(), joinPointCaptor.capture());
    verifyNoMoreInteractions(configurationResolver);

    assertThat(joinPointCaptor.getAllValues()).hasSize(1);
    assertThat(joinPointCaptor.getAllValues().get(0)).isNotNull();
    assertThat(joinPointCaptor.getAllValues().get(0)).isInstanceOf(ProceedingJoinPoint.class);
    assertThat(joinPointCaptor.getAllValues().get(0).getSignature().getName()).isEqualTo("aMethod");
  }

  @Test
  public void shouldExecuteOnTypeLevel() {
    final OptionsResolver configurationResolver = mock(OptionsResolver.class);

    final AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new LimitedTypeLevel());
    final KeyGenerator keyGenerator = mock(KeyGenerator.class);
    when(keyGenerator.key(anyString(), anyString(), any(JoinPoint.class))).thenReturn("key");
    final RateChecker rateChecker = mock(RateChecker.class);
    when(rateChecker.check(anyString(), anyLong(), any(OptionsInterval.class))).thenReturn(true);
    proxyFactory.addAspect(new RateLimitingAdvice(keyGenerator, configurationResolver, rateChecker));
    final LimitedInterface test = proxyFactory.getProxy();

    final Options mock = mock(Options.class);
    when(mock.enabled()).thenReturn(true);
    when(configurationResolver.resolve(anyString(), any(JoinPoint.class))).thenReturn(mock);

    test.aMethod();
    test.bMethod();

    final ArgumentCaptor<JoinPoint> joinPointCaptor = ArgumentCaptor.forClass(JoinPoint.class);

    verify(configurationResolver, times(2)).resolve(anyString(), joinPointCaptor.capture());
    verifyNoMoreInteractions(configurationResolver);

    assertThat(joinPointCaptor.getAllValues()).hasSize(2);
    assertThat(joinPointCaptor.getAllValues().get(0).getSignature().getName()).isEqualTo("aMethod");
    assertThat(joinPointCaptor.getAllValues().get(1).getSignature().getName()).isEqualTo("bMethod");

  }

}
