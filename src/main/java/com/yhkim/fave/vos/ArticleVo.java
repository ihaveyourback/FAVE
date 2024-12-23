package com.yhkim.fave.vos;

import com.cykim.teamproject.entities.ArticleEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleVo extends ArticleEntity {
    private int commentCount;
}
