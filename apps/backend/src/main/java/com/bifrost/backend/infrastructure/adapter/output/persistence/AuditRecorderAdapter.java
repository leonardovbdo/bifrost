package com.bifrost.backend.infrastructure.adapter.output.persistence;

import com.bifrost.backend.domain.model.AuditEvent;
import com.bifrost.backend.domain.port.output.AuditRecorder;
import com.bifrost.backend.domain.repository.AuditEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditRecorderAdapter implements AuditRecorder {
  private final AuditEventRepository auditEventRepository;

  public AuditRecorderAdapter(AuditEventRepository auditEventRepository) {
    this.auditEventRepository = auditEventRepository;
  }

  @Override
  public void record(String type, UUID userId, UUID robotProfileId, Map<String, Object> payload) {
    Map<String, Object> enriched = new LinkedHashMap<>();
    if (payload != null) {
      enriched.putAll(payload);
    }
    enrichRequestMeta(enriched);
    auditEventRepository.save(AuditEvent.create(type, userId, robotProfileId, enriched));
  }

  private void enrichRequestMeta(Map<String, Object> payload) {
    try {
      var attrs = RequestContextHolder.getRequestAttributes();
      if (attrs instanceof ServletRequestAttributes servletAttrs) {
        HttpServletRequest request = servletAttrs.getRequest();
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        if (ip != null && !payload.containsKey("ip")) {
          payload.put("ip", ip);
        }
        if (ua != null && !payload.containsKey("userAgent")) {
          payload.put("userAgent", ua.length() > 180 ? ua.substring(0, 180) : ua);
        }
      }
    } catch (Exception ignored) {
      // no request context (scheduler/tests)
    }
  }
}
