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

package com.github.fzilic.spring.rate.limiting.options;

import com.github.fzilic.spring.rate.limiting.RateLimited;
import com.github.fzilic.spring.rate.limiting.aspect.RateLimitingAdvice;
import com.github.fzilic.spring.rate.limiting.options.exception.AmbiguousOptionsException;
import com.github.fzilic.spring.rate.limiting.options.exception.OptionsException;
import org.aspectj.lang.JoinPoint;

/**
 * Used by {@link RateLimitingAdvice} to determine options for rate limiting
 * <p/>
 * See {@link DelegatingOptionsResolver} if you have need for more then one resolver
 *
 * @author franjozilic
 */
public interface OptionsResolver {

  /**
   * Resolved options for rate limiting
   *
   * @param key         resolved key for rate limited operation
   * @param rateLimited resolved {@link RateLimited} annotation
   * @param joinPoint   actual join point
   * @return options for rate limiting, never null
   * @throws AmbiguousOptionsException when there is more then one unique option set for key and annotation combination
   */
  Options resolve(String key, RateLimited rateLimited, final JoinPoint joinPoint) throws OptionsException;

  /**
   * TODO
   *
   * @param key         resolved key for rate limited operation
   * @param rateLimited resolved {@link RateLimited} annotation
   * @return true if this resolver supports key and annotation combination
   */
  boolean supports(String key, RateLimited rateLimited);

}
