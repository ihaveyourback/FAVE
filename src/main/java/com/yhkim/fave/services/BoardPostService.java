package com.yhkim.fave.services;

import com.yhkim.fave.entities.BoardPostEntity;
import com.yhkim.fave.mappers.BoardPostMapper;
import com.yhkim.fave.vos.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardPostService {
    private final BoardPostMapper boardPostMapper;

    @Autowired
    public BoardPostService(BoardPostMapper boardPostMapper) {
        this.boardPostMapper = boardPostMapper;
    }

    public List<BoardPostEntity> getPostsByUserEmail(String userEmail, PageVo pageVo) {
        return boardPostMapper.selectPostsByUserEmail(userEmail, pageVo);
    }

    public int countPostsByUserEmail(String userEmail) {
        return boardPostMapper.countPostsByUserEmail(userEmail);
    }
}