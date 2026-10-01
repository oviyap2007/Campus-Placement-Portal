package com.CampusPlacement.portal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_round",
       uniqueConstraints = @UniqueConstraint(columnNames = {"job_id","round_number"}))
public class InterviewRound {

    public enum RoundType {
        RESUME_SHORTLIST, APTITUDE, ONLINE_TEST, CODING, TECHNICAL, GROUP_DISCUSSION, HR;

        public String getDisplayName() {
            return switch (this) {
                case RESUME_SHORTLIST  -> "Resume Shortlist";
                case APTITUDE         -> "Aptitude Test";
                case ONLINE_TEST      -> "Online Test";
                case CODING           -> "Coding Round";
                case TECHNICAL        -> "Technical Interview";
                case GROUP_DISCUSSION -> "Group Discussion";
                case HR               -> "HR Interview";
            };
        }

        public String getIcon() {
            return switch (this) {
                case RESUME_SHORTLIST  -> "📄";
                case APTITUDE         -> "🧠";
                case ONLINE_TEST      -> "💻";
                case CODING           -> "⌨️";
                case TECHNICAL        -> "🔧";
                case GROUP_DISCUSSION -> "👥";
                case HR               -> "🤝";
            };
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id")
    private int roundId;

    @Column(name = "job_id", nullable = false)
    private int jobId;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "round_name", nullable = false)
    private String roundName;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false)
    private RoundType roundType;

    public InterviewRound() {}

    public InterviewRound(int jobId, int roundNumber, String roundName, RoundType roundType) {
        this.jobId       = jobId;
        this.roundNumber = roundNumber;
        this.roundName   = roundName;
        this.roundType   = roundType;
    }

    // Getters
    public int getRoundId()          { return roundId; }
    public int getJobId()             { return jobId; }
    public int getRoundNumber()       { return roundNumber; }
    public String getRoundName()      { return roundName; }
    public RoundType getRoundType()   { return roundType; }

    // Setters
    public void setRoundId(int roundId)            { this.roundId = roundId; }
    public void setJobId(int jobId)                { this.jobId = jobId; }
    public void setRoundNumber(int roundNumber)    { this.roundNumber = roundNumber; }
    public void setRoundName(String roundName)     { this.roundName = roundName; }
    public void setRoundType(RoundType roundType)  { this.roundType = roundType; }
}
