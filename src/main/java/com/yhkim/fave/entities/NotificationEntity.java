package com.yhkim.fave.entities;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "index")
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntity {
    private int index;
    private String userEmail;
    private String message;
    private String url;
    private boolean isRead;
    private boolean isDeleted;
    private LocalDateTime createdAt;
}
