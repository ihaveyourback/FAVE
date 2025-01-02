package com.yhkim.fave.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class MailDto {
    private String address;
    private String title;
    private String content;
    private String nickname;

}
