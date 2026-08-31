package org.jejuro.miraero.global.security;

import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestProtectedController {

  @GetMapping("/api/test/protected")
  public ApiResponse<String> protectedApi() {
    return ApiResponse.success("authenticated");
  }

  @GetMapping("/health")
  public ApiResponse<String> publicApi() {
    return ApiResponse.success("ok");
  }
}
