package com.attus.financial.service;

import com.attus.financial.domain.entity.AuditLog;
import com.attus.financial.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, String entityId, String action, String payload) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .payload(payload)
                    .performedBy("system")
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("[AUDIT_LOG] {} {} {} - {}", action, entityType, entityId, payload);
        } catch (Exception e) {
            log.error("[AUDIT_LOG] Falha ao persistir audit log: {}", e.getMessage());
        }
    }
}
