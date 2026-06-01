package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dispute_no", unique = true, nullable = false, length = 50)
    private String disputeNo;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "respondent_id", nullable = false)
    private Long respondentId;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_images", columnDefinition = "TEXT")
    private String evidenceImages;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispute_status", nullable = false)
    private DisputeStatus disputeStatus = DisputeStatus.PENDING;

    @Column(name = "dispute_type")
    private Integer disputeType = 1;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "result", length = 500)
    private String result;

    @Column(name = "expect_result", length = 500)
    private String expectResult;

    @Column(name = "expect_refund_amount", precision = 10, scale = 2)
    private BigDecimal expectRefundAmount;

    @Column(name = "actual_refund_amount", precision = 10, scale = 2)
    private BigDecimal actualRefundAmount;

    @Column(name = "process_remark", length = 1000)
    private String processRemark;

    @Column(name = "process_logs", columnDefinition = "TEXT")
    private String processLogs;

    @Column(name = "is_urgent")
    private Boolean isUrgent = false;

    @Column(name = "priority")
    private Integer priority = 1;

    @Column(name = "assign_time")
    private LocalDateTime assignTime;

    @Column(name = "start_process_time")
    private LocalDateTime startProcessTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Column(name = "close_time")
    private LocalDateTime closeTime;

    @Column(name = "close_type")
    private Integer closeType;

    @Column(name = "is_escalated")
    private Boolean isEscalated = false;

    @Column(name = "escalated_to")
    private Long escalatedTo;

    @Column(name = "escalated_time")
    private LocalDateTime escalatedTime;

    @Column(name = "escalated_reason", length = 255)
    private String escalatedReason;

    @Column(name = "satisfaction")
    private Integer satisfaction;

    @Column(name = "satisfaction_remark", length = 500)
    private String satisfactionRemark;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DisputeStatus {
        PENDING,
        ASSIGNED,
        PROCESSING,
        ESCALATED,
        RESOLVED,
        CLOSED,
        CANCELLED
    }

    public enum DisputeType {
        PRODUCT_ISSUE(1, "商品问题"),
        LOGISTICS_ISSUE(2, "物流问题"),
        REFUND_ISSUE(3, "退款问题"),
        OTHER(4, "其他");

        private final int value;
        private final String label;

        DisputeType(int value, String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public static DisputeType fromValue(int value) {
            for (DisputeType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return OTHER;
        }
    }

    public enum HandleResult {
        APPROVE_REFUND("同意退款"),
        PARTIAL_REFUND("部分退款"),
        REJECT("驳回"),
        CLOSE_NO_RESPONSE("无人响应关闭"),
        MEDIATION("转人工调解");

        private final String label;

        HandleResult(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum CloseType {
        USER_WITHDRAW(1, "用户撤销"),
        ADMIN_CLOSE(2, "管理员关闭"),
        TIME_OUT(3, "超时自动关闭"),
        RESOLVED(4, "已解决"),
        OTHER(5, "其他");

        private final int value;
        private final String label;

        CloseType(int value, String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}