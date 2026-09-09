package com.hrms.backend.service;

import com.hrms.backend.entity.AuditLog;
import com.hrms.backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String actionType, String actorEmail, String targetEntity, String targetId, String details) {
        auditLogRepository.save(new AuditLog(actionType, actorEmail, targetEntity, targetId, details));
    }
}
