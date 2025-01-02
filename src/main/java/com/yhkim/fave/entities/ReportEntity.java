package com.yhkim.fave.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(schema = "fave", name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer index;

    @Column(name = "user_email", nullable = false, length = 50)
    private String userEmail;  // 신고한 사용자 이메일

    @Column(name = "reported_user_email", nullable = false, length = 50)
    private String reportedUserEmail;  // 신고된 사용자 이메일

    @Column(name = "reported_post_id")
    private Integer reportedPostId;  // 신고된 게시물 ID (옵션)

    @Column(name = "reported_comment_id")
    private Integer reportedCommentId;  // 신고된 댓글 ID (옵션)

    @Column(name = "status", length = 255)
    private String status;  // 신고 상태

    @Column(name = "current_status")
    private String currentStatus = ReportStatus.PROCESSING.getDescription(); // 기본값 설정

    @Column(name = "reason", length = 50)
    private String reason;  // 신고 이유

    @Column(name = "reason_detail", length = 255)
    private String reasonDetail;  // 신고 상세 이유

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;  // 신고 시간

    // 사용자와 연관 관계를 맺기 위한 설정
    @ManyToOne
    @JoinColumn(name = "user_email", referencedColumnName = "email", insertable = false, updatable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "reported_user_email", referencedColumnName = "email", insertable = false, updatable = false)
    private UserEntity reportedUser;

}

enum ReportStatus {
    PROCESSING("신고 처리 중"),
    COMPLETED("신고 처리 완료");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}