package com.github.usedrarely;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.web.WebApplicationInitializer;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
public class Application extends SpringBootServletInitializer implements WebApplicationInitializer {

  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
