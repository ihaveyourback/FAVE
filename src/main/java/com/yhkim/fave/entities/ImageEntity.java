package com.yhkim.fave.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = {"index"})
public class ImageEntity {
    private int index;
    private byte[] data;
    private String contentType;
    private String name;
    private LocalDateTime createdAt;
}
