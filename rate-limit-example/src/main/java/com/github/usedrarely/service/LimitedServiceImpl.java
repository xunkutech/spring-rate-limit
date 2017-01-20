package com.github.usedrarely.service;

import com.github.usedrarely.spring.rate.limit.Interval;
import com.github.usedrarely.spring.rate.limit.RateLimited;
import com.github.usedrarely.spring.rate.limit.RateLimitedRetry;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class LimitedServiceImpl implements LimitedService, ExampleService {

  @Override
  @RateLimited(key = "retrying", maxRequests = 10, interval = @Interval(interval = 30, unit = TimeUnit.SECONDS))
  @RateLimitedRetry(retryCount = 10, interval = @Interval(interval = 5, unit = TimeUnit.SECONDS))
  public Boolean retrying() {
    return Boolean.TRUE;
  }

  @Override
  @RateLimited(key = "strict", maxRequests = 5, interval = @Interval(interval = 30, unit = TimeUnit.SECONDS))
  public Boolean strict() {
    return Boolean.TRUE;
  }

}
