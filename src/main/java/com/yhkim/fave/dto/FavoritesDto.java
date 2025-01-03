package com.yhkim.fave.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class FavoritesDto {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int index;

        @Column(nullable = false)
        private String userEmail;

        @Column(nullable = false)
        private int festivalId;
    }

