package com.bifrost.backend.infrastructure.adapter.input.controller.v1;

import com.bifrost.backend.application.usecase.audit.ListAuditEventsUseCase;
import com.bifrost.backend.application.usecase.audit.RecordClientAuditEventUseCase;
import com.bifrost.backend.domain.model.AuditEvent;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.AuditEventListResponse;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.AuditEventRequest;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.AuditEventResponse;
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
@RequestMapping("/api/v1/audit/events")
public class AuditEventsController {
  private final RecordClientAuditEventUseCase recordClientAuditEventUseCase;
  private final ListAuditEventsUseCase listAuditEventsUseCase;

  public AuditEventsController(
      RecordClientAuditEventUseCase recordClientAuditEventUseCase,
      ListAuditEventsUseCase listAuditEventsUseCase) {
    this.recordClientAuditEventUseCase = recordClientAuditEventUseCase;
    this.listAuditEventsUseCase = listAuditEventsUseCase;
  }

  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody AuditEventRequest request) {
    recordClientAuditEventUseCase.execute(
        SecurityUtils.currentUserId(),
        request.type(),
        request.robotProfileId(),
        request.payload());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public AuditEventListResponse list(
      @RequestParam(required = false) String type,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false, defaultValue = "50") int limit) {
    List<AuditEventResponse> items =
        listAuditEventsUseCase.execute(type, userId, limit).stream()
            .map(AuditEventResponse::from)
            .toList();
    return new AuditEventListResponse(items);
  }
}
