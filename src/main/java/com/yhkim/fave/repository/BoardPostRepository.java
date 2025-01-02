package com.yhkim.fave.repository;

import com.yhkim.fave.entities.BoardPostEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardPostRepository extends JpaRepository<BoardPostEntity, Integer> {
    List<BoardPostEntity>findByIndex(@Param("index") Integer index);
    @Query("SELECT b FROM BoardPostEntity b WHERE b.userEmail = :userEmail")
    List<BoardPostEntity> findPostsByUserEmail(@Param("userEmail") String userEmail);
}

