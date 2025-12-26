package br.com.edmilson.bndes.projects.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @Operation(summary = "Healthcheck API")
  @GetMapping("/health")
  public String health() {
    return "OK";
  }
}
