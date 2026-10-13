package com.sivion.api.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Records metadata for successful and failed write requests without storing request bodies.
 */
@Entity
@Table(
        name = "audit_log",
        indexes = @Index(
                name = "idx_audit_tenant_occurred",
                columnList = "tenant_id, occurred_at"
        )
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(nullable = false, length = 255)
    private String actor;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(nullable = false, length = 512)
    private String endpoint;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "client_address", length = 64)
    private String clientAddress;

    protected AuditLog() {
    }

    public AuditLog(
            String tenantId,
            String actor,
            String method,
            String endpoint,
            int statusCode,
            Instant occurredAt,
            String clientAddress) {
        this.tenantId = tenantId;
        this.actor = actor;
        this.method = method;
        this.endpoint = endpoint;
        this.statusCode = statusCode;
        this.occurredAt = occurredAt;
        this.clientAddress = clientAddress;
    }

    public Long getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getActor() {
        return actor;
    }

    public String getMethod() {
        return method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getClientAddress() {
        return clientAddress;
    }
}
