package br.com.edmilson.bndes.projects.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

  @Operation(summary = "Healthcheck API")
  @GetMapping(produces = "text/plain")
  public String health() {
    return "OK";
  }
}
