package com.hrms.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auditId;

    private String actionType;
    private String actorEmail;
    private String targetEntity;
    private String targetId;
    private String actionDetails;
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(String actionType, String actorEmail, String targetEntity, String targetId, String actionDetails) {
        this.actionType = actionType;
        this.actorEmail = actorEmail;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.actionDetails = actionDetails;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public Integer getAuditId() { return auditId; }
    public String getActionType() { return actionType; }
    public String getActorEmail() { return actorEmail; }
    public String getTargetEntity() { return targetEntity; }
    public String getTargetId() { return targetId; }
    public String getActionDetails() { return actionDetails; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
