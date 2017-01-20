package com.github.usedrarely.controller;

import com.github.usedrarely.dto.Response;
import com.github.usedrarely.service.LimitedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/limited")
public class LimitedController {

  private final LimitedService limitedService;

  @Autowired
  public LimitedController(final LimitedService limitedService) {
    this.limitedService = limitedService;
  }

  @RequestMapping(value = "/retrying", method = RequestMethod.GET)
  public Response retrying() {
    final Boolean result = limitedService.retrying();
    return Response.builder().status(result).build();
  }

  @RequestMapping(value = "/strict", method = RequestMethod.GET)
  public Response strict() {
    final Boolean result = limitedService.strict();
    return Response.builder().status(result).build();
  }

}
