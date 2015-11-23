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

package com.github.fzilic.spring.rate.limiting.key;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class DefaultKeyGeneratorTest {

  @Mock
  private JoinPoint joinPoint;

  private KeyGenerator keyGenerator = new DefaultKeyGenerator();

  @Before
  public void setUp() {
    reset(joinPoint);
  }

  @Test
  public void shouldGenerateDefault() throws NoSuchMethodException {
    when(joinPoint.getTarget()).thenReturn(this);
    final MethodSignature signature = mock(MethodSignature.class);
    when(signature.getMethod()).thenReturn(this.getClass().getMethod("shouldGenerateDefault"));
    when(joinPoint.getSignature()).thenReturn(signature);
    assertThat(keyGenerator.key("", "", joinPoint)).isEqualTo("com.github.fzilic.spring.rate.limiting.key.DefaultKeyGeneratorTest.shouldGenerateDefault");
  }

  @Test
  public void shouldGenerateToKey() throws NoSuchMethodException {
    when(joinPoint.getTarget()).thenReturn(this);
    final MethodSignature signature = mock(MethodSignature.class);
    when(signature.getMethod()).thenReturn(this.getClass().getMethod("shouldGenerateToKey"));
    when(joinPoint.getSignature()).thenReturn(signature);

    assertThat(keyGenerator.key("", "#type", joinPoint)).isEqualTo("com.github.fzilic.spring.rate.limiting.key.DefaultKeyGeneratorTest");
  }

  @Test
  public void shouldReturnSetKey() {
    assertThat(keyGenerator.key("predefine", "", null)).isEqualTo("predefine");
  }

}
