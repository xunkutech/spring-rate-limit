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

package com.github.fzilic.spring.rate.limiting.checker;

import com.github.fzilic.spring.rate.limiting.options.OptionsInterval;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class, UUID.class, RedisRateChecker.Callback.class})
public class RedisRateCheckerTest {

  @Test
  public void shouldAllow() {
    PowerMockito.mockStatic(System.class);
    PowerMockito.mockStatic(UUID.class);

    @SuppressWarnings("unchecked")
    final RedisOperations<String, String> redisOperations = mock(RedisOperations.class);
    @SuppressWarnings("unchecked")
    final ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
    final UUID uuid = PowerMockito.mock(UUID.class);
    final OptionsInterval interval = mock(OptionsInterval.class);

    // static stuff
    PowerMockito.when(System.currentTimeMillis()).thenReturn(1448037976717L);
    when(UUID.randomUUID()).thenReturn(uuid);
    when(uuid.toString()).thenReturn("43f5fe42-540a-47f8-a36e-2c1b0718103e");

    // redis mock
    when(redisOperations.execute(any(SessionCallback.class))).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        return invocation.getArgumentAt(0, SessionCallback.class).execute(redisOperations);
      }
    });
    when(redisOperations.opsForZSet()).thenReturn(zSetOperations);
    when(redisOperations.exec()).thenReturn(Arrays.asList(new Object[]{0L, Boolean.TRUE, Boolean.TRUE, 1L}));
    // interval
    when(interval.interval()).thenReturn(10L);
    when(interval.unit()).thenReturn(TimeUnit.SECONDS);

    final RateChecker rateChecker = new RedisRateChecker(redisOperations);

    assertThat(rateChecker.check("test", 10L, interval)).isTrue();

    PowerMockito.verifyStatic();
    System.currentTimeMillis();
    PowerMockito.verifyStatic();
    //noinspection ResultOfMethodCallIgnored
    UUID.randomUUID();

    verify(redisOperations).multi();
    verify(redisOperations, times(3)).opsForZSet();
    verify(redisOperations).exec();
    verify(redisOperations).expire(eq("test"), eq(10L), eq(TimeUnit.SECONDS));

    verify(zSetOperations).removeRangeByScore(eq("test"), eq(1448037976717D), eq(1448037976717D - 10000));
    verify(zSetOperations).add(eq("test"), eq("43f5fe42-540a-47f8-a36e-2c1b0718103e-1448037976717"), eq(1448037976717D));
    verify(zSetOperations).count(eq("test"), eq(Double.MIN_VALUE), eq(Double.MAX_VALUE));

    verify(interval, times(2)).interval();
    verify(interval, times(2)).unit();
    verify(redisOperations, times(1)).execute(any(RedisRateChecker.Callback.class));

    PowerMockito.verifyNoMoreInteractions(redisOperations, zSetOperations, uuid, interval);
  }

  @Test
  public void shouldDeny() {
    PowerMockito.mockStatic(System.class);
    PowerMockito.mockStatic(UUID.class);

    @SuppressWarnings("unchecked")
    final RedisOperations<String, String> redisOperations = mock(RedisOperations.class);
    @SuppressWarnings("unchecked")
    final ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
    final UUID uuid = PowerMockito.mock(UUID.class);
    final OptionsInterval interval = mock(OptionsInterval.class);

    // static stuff
    PowerMockito.when(System.currentTimeMillis()).thenReturn(1448037976717L);
    when(UUID.randomUUID()).thenReturn(uuid);
    when(uuid.toString()).thenReturn("43f5fe42-540a-47f8-a36e-2c1b0718103e");

    // redis mock
    when(redisOperations.execute(any(SessionCallback.class))).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        return invocation.getArgumentAt(0, SessionCallback.class).execute(redisOperations);
      }
    });
    when(redisOperations.opsForZSet()).thenReturn(zSetOperations);
    when(redisOperations.exec()).thenReturn(Arrays.asList(new Object[]{0L, Boolean.TRUE, Boolean.TRUE, 11L}));
    // interval
    when(interval.interval()).thenReturn(10L);
    when(interval.unit()).thenReturn(TimeUnit.SECONDS);

    final RateChecker rateChecker = new RedisRateChecker(redisOperations);

    assertThat(rateChecker.check("test", 10L, interval)).isFalse();

    PowerMockito.verifyStatic();
    System.currentTimeMillis();
    PowerMockito.verifyStatic();
    //noinspection ResultOfMethodCallIgnored
    UUID.randomUUID();

    verify(redisOperations).multi();
    verify(redisOperations, times(4)).opsForZSet();
    verify(redisOperations).exec();
    verify(redisOperations).expire(eq("test"), eq(10L), eq(TimeUnit.SECONDS));

    verify(zSetOperations).removeRangeByScore(eq("test"), eq(1448037976717D), eq(1448037976717D - 10000));
    verify(zSetOperations).add(eq("test"), eq("43f5fe42-540a-47f8-a36e-2c1b0718103e-1448037976717"), eq(1448037976717D));
    verify(zSetOperations).count(eq("test"), eq(Double.MIN_VALUE), eq(Double.MAX_VALUE));
    verify(zSetOperations).remove(eq("test"), eq("43f5fe42-540a-47f8-a36e-2c1b0718103e-1448037976717"));

    verify(interval, times(2)).interval();
    verify(interval, times(2)).unit();
    verify(redisOperations, times(1)).execute(any(RedisRateChecker.Callback.class));

    PowerMockito.verifyNoMoreInteractions(redisOperations, zSetOperations, uuid, interval);
  }


  @Test
  public void shouldDenyInCaseOfError() {
    PowerMockito.mockStatic(System.class);
    PowerMockito.mockStatic(UUID.class);

    @SuppressWarnings("unchecked")
    final RedisOperations<String, String> redisOperations = mock(RedisOperations.class);
    @SuppressWarnings("unchecked")
    final ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
    final UUID uuid = PowerMockito.mock(UUID.class);
    final OptionsInterval interval = mock(OptionsInterval.class);

    // static stuff
    PowerMockito.when(System.currentTimeMillis()).thenReturn(1448037976717L);
    when(UUID.randomUUID()).thenReturn(uuid);
    when(uuid.toString()).thenReturn("43f5fe42-540a-47f8-a36e-2c1b0718103e");

    // redis mock
    when(redisOperations.execute(any(SessionCallback.class))).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        return invocation.getArgumentAt(0, SessionCallback.class).execute(redisOperations);
      }
    });
    when(redisOperations.opsForZSet()).thenReturn(zSetOperations);
    when(redisOperations.exec()).thenReturn(Arrays.asList(new Object[]{0L, Boolean.TRUE, 11L}));
    // interval
    when(interval.interval()).thenReturn(10L);
    when(interval.unit()).thenReturn(TimeUnit.SECONDS);

    final RateChecker rateChecker = new RedisRateChecker(redisOperations);

    assertThat(rateChecker.check("test", 10L, interval)).isFalse();

    PowerMockito.verifyStatic();
    System.currentTimeMillis();
    PowerMockito.verifyStatic();
    //noinspection ResultOfMethodCallIgnored
    UUID.randomUUID();

    verify(redisOperations).multi();
    verify(redisOperations, times(3)).opsForZSet();
    verify(redisOperations).exec();
    verify(redisOperations).expire(eq("test"), eq(10L), eq(TimeUnit.SECONDS));

    verify(zSetOperations).removeRangeByScore(eq("test"), eq(1448037976717D), eq(1448037976717D - 10000));
    verify(zSetOperations).add(eq("test"), eq("43f5fe42-540a-47f8-a36e-2c1b0718103e-1448037976717"), eq(1448037976717D));
    verify(zSetOperations).count(eq("test"), eq(Double.MIN_VALUE), eq(Double.MAX_VALUE));

    verify(interval, times(2)).interval();
    verify(interval, times(2)).unit();
    verify(redisOperations, times(1)).execute(any(RedisRateChecker.Callback.class));

    PowerMockito.verifyNoMoreInteractions(redisOperations, zSetOperations, uuid, interval);
  }

}
