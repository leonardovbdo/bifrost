package com.bifrost.backend.application.usecase.parameter;

import com.bifrost.backend.domain.enums.ParameterScope;
import com.bifrost.backend.domain.exception.NotFoundException;
import com.bifrost.backend.domain.exception.ValidationException;
import com.bifrost.backend.domain.model.Parameter;
import com.bifrost.backend.domain.model.User;
import com.bifrost.backend.domain.repository.ParameterRepository;
import com.bifrost.backend.domain.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListParametersUseCase {
  private final UserRepository userRepository;
  private final ParameterRepository parameterRepository;

  public ListParametersUseCase(UserRepository userRepository, ParameterRepository parameterRepository) {
    this.userRepository = userRepository;
    this.parameterRepository = parameterRepository;
  }

  @Transactional(readOnly = true)
  public List<Parameter> execute(UUID requesterId, ParameterScope scope) {
    User user =
        userRepository
            .findById(requesterId)
            .filter(User::active)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

    if (scope == ParameterScope.GLOBAL) {
      return parameterRepository.findByScope(ParameterScope.GLOBAL, null);
    }
    if (scope == ParameterScope.USER) {
      return parameterRepository.findByScope(ParameterScope.USER, user.id());
    }
    throw new ValidationException("PARAMETER_SCOPE_INVALID", "Unsupported scope");
  }
}
