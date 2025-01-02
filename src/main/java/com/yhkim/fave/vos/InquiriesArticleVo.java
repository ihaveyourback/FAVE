package com.yhkim.fave.vos;

import com.yhkim.fave.entities.InquiriesArticleEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiriesArticleVo extends InquiriesArticleEntity {
    private int commentCount;
}
