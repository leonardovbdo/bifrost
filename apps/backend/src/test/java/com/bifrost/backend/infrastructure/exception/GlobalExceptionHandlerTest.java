package com.bifrost.backend.infrastructure.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

  @Test
  void genericHandlerDoesNotExposeExceptionMessageInBody() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    var response =
        handler.generic(new RuntimeException("secret-internal-detail-should-not-leak"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
    assertThat(response.getBody().error().message()).isEqualTo("Unexpected error");
    assertThat(response.getBody().error().message()).doesNotContain("secret");
  }
}
