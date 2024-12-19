package com.yhkim.fave.entities;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ReportsEntity {
    private int index;
    private String userEmail;
    private String reportUserEmail;
    private String reportedPostId;
    private String reportedCommentId;
    private String status;
    private String currentStatus;
    private String reason;
    private String reasonDetail;
    private LocalDateTime reportedAt;

    private UserEntity user;
}
