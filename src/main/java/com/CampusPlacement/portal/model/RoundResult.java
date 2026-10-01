package com.CampusPlacement.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "round_result",
       uniqueConstraints = @UniqueConstraint(columnNames = {"application_id","round_id"}))
public class RoundResult {

    public enum ResultStatus {
        PENDING, PASSED, FAILED, NOT_STARTED;

        public String getLabel() {
            return switch (this) {
                case PENDING     -> "In Progress";
                case PASSED      -> "Passed";
                case FAILED      -> "Failed";
                case NOT_STARTED -> "Waiting";
            };
        }

        public String getCssClass() {
            return switch (this) {
                case PENDING     -> "round-pending";
                case PASSED      -> "round-passed";
                case FAILED      -> "round-failed";
                case NOT_STARTED -> "round-waiting";
            };
        }

        public String getIcon() {
            return switch (this) {
                case PASSED      -> "✓";
                case FAILED      -> "✗";
                case PENDING     -> "🔄";
                case NOT_STARTED -> "○";
            };
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private int resultId;

    @Column(name = "application_id", nullable = false)
    private int applicationId;

    @Column(name = "round_id", nullable = false)
    private int roundId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ResultStatus status = ResultStatus.NOT_STARTED;

    @Column(name = "remarks")
    private String remarks = "";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Transient display fields
    @Transient private String roundName;
    @Transient private int    roundNumber;
    @Transient private String roundTypeIcon;
    @Transient private String roundTypeName;

    public RoundResult() {}

    public RoundResult(int applicationId, int roundId, ResultStatus status) {
        this.applicationId = applicationId;
        this.roundId       = roundId;
        this.status        = status;
        this.updatedAt     = LocalDateTime.now();
    }

    // Getters
    public int getResultId()           { return resultId; }
    public int getApplicationId()      { return applicationId; }
    public int getRoundId()            { return roundId; }
    public ResultStatus getStatus()    { return status; }
    public String getRemarks()         { return remarks; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
    public String getRoundName()       { return roundName; }
    public int getRoundNumber()        { return roundNumber; }
    public String getRoundTypeIcon()   { return roundTypeIcon; }
    public String getRoundTypeName()   { return roundTypeName; }

    // Setters
    public void setResultId(int resultId)              { this.resultId = resultId; }
    public void setApplicationId(int applicationId)    { this.applicationId = applicationId; }
    public void setRoundId(int roundId)                { this.roundId = roundId; }
    public void setStatus(ResultStatus status)         { this.status = status; this.updatedAt = LocalDateTime.now(); }
    public void setRemarks(String remarks)             { this.remarks = remarks; }
    public void setUpdatedAt(LocalDateTime updatedAt)  { this.updatedAt = updatedAt; }
    public void setRoundName(String roundName)         { this.roundName = roundName; }
    public void setRoundNumber(int roundNumber)        { this.roundNumber = roundNumber; }
    public void setRoundTypeIcon(String icon)          { this.roundTypeIcon = icon; }
    public void setRoundTypeName(String name)          { this.roundTypeName = name; }
}
