package com.yhkim.fave.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class FaveInfoEntity {
    private int index;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String userEmail;
    private byte[] coverData; // 파일 바이너리 데이터 저장
    private String coverContentType; // MIME 타입 저장
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int view;
}
