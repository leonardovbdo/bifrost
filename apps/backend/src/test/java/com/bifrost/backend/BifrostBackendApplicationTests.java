package com.bifrost.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BifrostBackendApplicationTests {

  @Test
  void applicationPackageMatchesConvention() {
    assertEquals("com.bifrost.backend", BifrostBackendApplication.class.getPackageName());
  }
}
