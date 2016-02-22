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

package com.github.usedrarely.spring.rate.limit.options;

import com.github.usedrarely.spring.rate.limit.RateLimited;
import com.github.usedrarely.spring.rate.limit.options.exception.AmbiguousOptionsException;
import com.github.usedrarely.spring.rate.limit.options.exception.OptionsException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DelegatingOptionsResolverTest {

  class Tested {

    @RateLimited(key = "test")
    public void aMethod() {
    }
  }

  @Test
  public void shouldDisableMissing() {
    final List<OptionsResolver> objects = Collections.emptyList();
    assertThat(new DelegatingOptionsResolver(objects, true)
        .resolve("test", find("aMethod"), mock(JoinPoint.class)).enabled())
        .isFalse();
  }

  private RateLimited find(final String methodName) {
    return AnnotationUtils.findAnnotation(ClassUtils.getMethod(Tested.class, methodName), RateLimited.class);
  }

  @Test
  public void shouldResolve() {
    final Options resolve = new DelegatingOptionsResolver(Arrays.asList(new OptionsResolver() {
      @Override
      public Options resolve(final String key, final RateLimited rateLimited, final JoinPoint joinPoint) throws OptionsException {
        return null;
      }

      @Override
      public boolean supports(final String key, final RateLimited rateLimited) {
        return false;
      }
    }, new OptionsResolver() {
      @Override
      public Options resolve(final String key, final RateLimited rateLimited, final JoinPoint joinPoint) throws OptionsException {
        return new Options() {
          @Override
          public boolean blocked() {
            return false;
          }

          @Override
          public boolean enabled() {
            return true;
          }

          @Override
          public OptionsInterval interval() {
            return null;
          }

          @Override
          public Long maxRequests() {
            return 15L;
          }

          @Override
          public String resolvedKey() {
            return null;
          }

          @Override
          public OptionsRetry retry() {
            return null;
          }

          @Override
          public boolean retryEnabled() {
            return false;
          }
        };
      }

      @Override
      public boolean supports(final String key, final RateLimited rateLimited) {
        return true;
      }
    }))
        .resolve("test", find("aMethod"), mock(JoinPoint.class));
    assertThat(resolve).isNotNull();
    assertThat(resolve.enabled()).isTrue();
    assertThat(resolve.maxRequests()).isEqualTo(15L);
  }

  @Test(expected = AmbiguousOptionsException.class)
  public void shouldThrowExceptionIfMoreThenOne() {
    new DelegatingOptionsResolver(Arrays.asList(new OptionsResolver() {
      @Override
      public Options resolve(final String key, final RateLimited rateLimited, final JoinPoint joinPoint) throws OptionsException {
        return null;
      }

      @Override
      public boolean supports(final String key, final RateLimited rateLimited) {
        return true;
      }
    }, new OptionsResolver() {
      @Override
      public Options resolve(final String key, final RateLimited rateLimited, final JoinPoint joinPoint) throws OptionsException {
        return null;
      }

      @Override
      public boolean supports(final String key, final RateLimited rateLimited) {
        return true;
      }
    }))
        .resolve("test", find("aMethod"), mock(JoinPoint.class));
  }

  @Test(expected = AmbiguousOptionsException.class)
  public void shouldThrowExceptionOnMissing() {
    final List<OptionsResolver> objects = Collections.emptyList();
    new DelegatingOptionsResolver(objects)
        .resolve("test", find("aMethod"), mock(JoinPoint.class));
  }

}
