package com.yhkim.fave.vos;

import com.yhkim.fave.entities.ArticleEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleVo extends ArticleEntity {
    private int commentCount;
}
