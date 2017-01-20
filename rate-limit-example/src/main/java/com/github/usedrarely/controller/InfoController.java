package com.github.usedrarely.controller;

import com.github.usedrarely.service.ExampleService;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/info")
public class InfoController {

  private final Collection<ExampleService> exampleServices;

  @Autowired
  public InfoController(final Collection<ExampleService> exampleServices) {
    this.exampleServices = exampleServices;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String list() {
    return null;
  }
}
