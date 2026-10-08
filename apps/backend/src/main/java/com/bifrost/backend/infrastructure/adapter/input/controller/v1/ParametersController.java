package com.bifrost.backend.infrastructure.adapter.input.controller.v1;

import com.bifrost.backend.application.usecase.parameter.ListParametersUseCase;
import com.bifrost.backend.application.usecase.parameter.UpsertParameterUseCase;
import com.bifrost.backend.domain.enums.ParameterScope;
import com.bifrost.backend.domain.model.Parameter;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.ParameterListResponse;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.ParameterResponse;
import com.bifrost.backend.infrastructure.adapter.input.controller.v1.dto.ParameterUpsertRequest;
import com.bifrost.backend.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parameters")
public class ParametersController {
  private final ListParametersUseCase listParametersUseCase;
  private final UpsertParameterUseCase upsertParameterUseCase;

  public ParametersController(
      ListParametersUseCase listParametersUseCase, UpsertParameterUseCase upsertParameterUseCase) {
    this.listParametersUseCase = listParametersUseCase;
    this.upsertParameterUseCase = upsertParameterUseCase;
  }

  @GetMapping
  public ParameterListResponse list(@RequestParam String scope) {
    ParameterScope parameterScope = ParameterScope.fromDb(scope);
    List<ParameterResponse> items =
        listParametersUseCase.execute(SecurityUtils.currentUserId(), parameterScope).stream()
            .map(ParameterResponse::from)
            .toList();
    return new ParameterListResponse(items);
  }

  @PutMapping
  public ParameterResponse upsert(@Valid @RequestBody ParameterUpsertRequest request) {
    ParameterScope scope = ParameterScope.fromDb(request.scope());
    Parameter saved =
        upsertParameterUseCase.execute(
            SecurityUtils.currentUserId(), scope, request.key(), request.value());
    return ParameterResponse.from(saved);
  }
}
