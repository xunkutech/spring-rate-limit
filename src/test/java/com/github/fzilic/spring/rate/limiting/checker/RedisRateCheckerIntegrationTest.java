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
import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static redis.embedded.RedisServer.builder;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({System.class, UUID.class, RedisRateChecker.Callback.class})
public class RedisRateCheckerIntegrationTest {

  private static Integer port;

  private static RedisServer server;

  private RedisOperations<String, String> redisOperations;

  @BeforeClass
  public static void startRedisServer() throws IOException {
    final ServerSocket serverSocket = new ServerSocket(0);
    port = serverSocket.getLocalPort();
    serverSocket.close();
    server = builder().port(port).build();
    server.start();
    assertThat(server.isActive());
  }

  @AfterClass
  public static void stopRedisServer() {
    server.stop();
  }

  @Before
  public void configureRedis() {
    final JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
    connectionFactory.setPort(port);
    connectionFactory.setHostName("localhost");
    connectionFactory.setDatabase(0);
    connectionFactory.afterPropertiesSet();
    final RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setDefaultSerializer(new GenericToStringSerializer<>(Object.class));
    redisTemplate.setExposeConnection(true);
    redisTemplate.afterPropertiesSet();

    redisOperations = redisTemplate;
    redisOperations.execute(
        new RedisCallback<Object>() {
          @Override
          public Object doInRedis(final RedisConnection connection) throws DataAccessException {
      connection.flushDb();
      return null;
          }
        }
    );
  }

  @Test
  public void shouldAllow() {
    for (int idx = 1; idx < 6; idx++) {
      long value = 1448037976717L - (idx * 1000);
      redisOperations.opsForZSet().add("test", UUID.randomUUID().toString().concat("-").concat(Long.toString(value)), value);
    }

    assertThat(redisOperations.opsForZSet().count("test", Double.MIN_VALUE, Double.MAX_VALUE)).isEqualTo(5);
    PowerMockito.mockStatic(System.class);
    final OptionsInterval interval = mock(OptionsInterval.class);

    // static stuff
    PowerMockito.when(System.currentTimeMillis()).thenReturn(1448037976717L);

    when(interval.interval()).thenReturn(10L);
    when(interval.unit()).thenReturn(TimeUnit.SECONDS);

    final RateChecker rateChecker = new RedisRateChecker(redisOperations);

    assertThat(rateChecker.check("test", 10L, interval)).isTrue();

    PowerMockito.verifyStatic();
    System.currentTimeMillis();

    verify(interval, times(2)).interval();
    verify(interval, times(2)).unit();

    assertThat(redisOperations.opsForZSet().count("test", Double.MIN_VALUE, Double.MAX_VALUE)).isEqualTo(6);
    PowerMockito.verifyNoMoreInteractions(interval);

  }

  @Test
  public void shouldAllowThenDeny() {
    for (int idx = 1; idx < 10; idx++) {
      long value = 1448037976717L - (idx * 1000);
      redisOperations.opsForZSet().add("test", UUID.randomUUID().toString().concat("-").concat(Long.toString(value)), value);
    }

    assertThat(redisOperations.opsForZSet().count("test", Double.MIN_VALUE, Double.MAX_VALUE)).isEqualTo(9);
    PowerMockito.mockStatic(System.class);
    final OptionsInterval interval = mock(OptionsInterval.class);

    // static stuff
    PowerMockito.when(System.currentTimeMillis()).thenReturn(1448037976717L);

    when(interval.interval()).thenReturn(10L);
    when(interval.unit()).thenReturn(TimeUnit.SECONDS);

    final RateChecker rateChecker = new RedisRateChecker(redisOperations);

    assertThat(rateChecker.check("test", 10L, interval)).isTrue();
    assertThat(rateChecker.check("test", 10L, interval)).isFalse();

    PowerMockito.verifyStatic(times(2));
    System.currentTimeMillis();

    verify(interval, times(4)).interval();
    verify(interval, times(4)).unit();

    assertThat(redisOperations.opsForZSet().count("test", Double.MIN_VALUE, Double.MAX_VALUE)).isEqualTo(10);
    PowerMockito.verifyNoMoreInteractions(interval);
  }

  @Test
  public void shouldDeny() {
    for (int idx = 1; idx < 11; idx++) {
      long value = 1448037976717L - (idx * 1000);
      redisOperations.opsForZSet().add("test", UUID.randomUUID().toString().concat("-").concat(Long.toString(value)), value);
    }

    assertThat(redisOperations.opsForZSet().count("test", Double.MIN_VALUE, Double.MAX_VALUE)).isEqualTo(10);
    PowerMockito.mockStatic(System.class);
    final OptionsInterval interval = mock(OptionsInterval.class);

    // static stuff
    PowerMockito.when(System.currentTimeMillis()).thenReturn(1448037976717L);

    when(interval.interval()).thenReturn(20L);
    when(interval.unit()).thenReturn(TimeUnit.SECONDS);

    final RateChecker rateChecker = new RedisRateChecker(redisOperations);

    assertThat(rateChecker.check("test", 10L, interval)).isFalse();

    PowerMockito.verifyStatic();
    System.currentTimeMillis();

    verify(interval, times(2)).interval();
    verify(interval, times(2)).unit();

    assertThat(redisOperations.opsForZSet().count("test", Double.MIN_VALUE, Double.MAX_VALUE)).isEqualTo(10);
    PowerMockito.verifyNoMoreInteractions(interval);
  }


  @Test
  public void shouldDenyAndRemove() {
    for (int idx = 0; idx < 20; idx++) {
      long value = 1448037976717L - (idx * 1000);
      redisOperations.opsForZSet().add("test", UUID.randomUUID().toString().concat("-").concat(Long.toString(value)), value);
    }

    assertThat(redisOperations.opsForZSet().count("test", Double.MIN_VALUE, Double.MAX_VALUE)).isEqualTo(20);
    PowerMockito.mockStatic(System.class);
    final OptionsInterval interval = mock(OptionsInterval.class);

    // static stuff
    PowerMockito.when(System.currentTimeMillis()).thenReturn(1448037976717L);

    when(interval.interval()).thenReturn(10L);
    when(interval.unit()).thenReturn(TimeUnit.SECONDS);

    final RateChecker rateChecker = new RedisRateChecker(redisOperations);

    assertThat(rateChecker.check("test", 10L, interval)).isFalse();

    PowerMockito.verifyStatic();
    System.currentTimeMillis();

    verify(interval, times(2)).interval();
    verify(interval, times(2)).unit();

    assertThat(redisOperations.opsForZSet().count("test", Double.MIN_VALUE, Double.MAX_VALUE)).isEqualTo(10);
    PowerMockito.verifyNoMoreInteractions(interval);
  }

}
