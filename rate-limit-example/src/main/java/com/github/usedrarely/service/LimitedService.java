package com.github.usedrarely.service;

public interface LimitedService {

  Boolean retrying();

  Boolean strict();
}
