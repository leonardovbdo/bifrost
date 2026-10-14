package com.bifrost.backend.infrastructure.adapter.input.controller.v1;

import com.bifrost.backend.application.usecase.telemetry.IngestTelemetrySampleUseCase;
import com.bifrost.backend.application.usecase.telemetry.ListTelemetrySamplesUseCase;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.TelemetrySampleListResponse;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.TelemetrySampleRequest;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.TelemetrySampleResponse;
import com.bifrost.backend.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/telemetry/samples")
public class TelemetrySamplesController {
  private final IngestTelemetrySampleUseCase ingestTelemetrySampleUseCase;
  private final ListTelemetrySamplesUseCase listTelemetrySamplesUseCase;

  public TelemetrySamplesController(
      IngestTelemetrySampleUseCase ingestTelemetrySampleUseCase,
      ListTelemetrySamplesUseCase listTelemetrySamplesUseCase) {
    this.ingestTelemetrySampleUseCase = ingestTelemetrySampleUseCase;
    this.listTelemetrySamplesUseCase = listTelemetrySamplesUseCase;
  }

  @PostMapping
  public ResponseEntity<TelemetrySampleResponse> create(
      @Valid @RequestBody TelemetrySampleRequest request) {
    TelemetrySampleResponse body =
        TelemetrySampleResponse.from(
            ingestTelemetrySampleUseCase.execute(
                SecurityUtils.currentUserId(),
                request.robotProfileId(),
                request.metric(),
                request.value()));
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public TelemetrySampleListResponse list(
      @RequestParam(required = false) String metric,
      @RequestParam(required = false) UUID robotProfileId,
      @RequestParam(required = false, defaultValue = "50") int limit) {
    List<TelemetrySampleResponse> items =
        listTelemetrySamplesUseCase.execute(metric, robotProfileId, limit).stream()
            .map(TelemetrySampleResponse::from)
            .toList();
    return new TelemetrySampleListResponse(items);
  }
}
