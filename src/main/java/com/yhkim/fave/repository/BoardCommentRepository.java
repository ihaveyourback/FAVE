
package com.yhkim.fave.repository;


import com.yhkim.fave.entities.BoardCommentEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardCommentEntity, Long> {

    @Query("SELECT c FROM BoardCommentEntity c WHERE c.commentIndex = :commentIndex")
    List<BoardCommentEntity> findByCommentIndex(@Param("commentIndex") Integer commentIndex);

    //    @Query("SELECT r FROM fave r WHERE r.userEmail = :userEmail AND r.reportedCommentId = :reportedCommentId" )
    Optional<BoardCommentEntity> findFirstByUserEmailAndCommentIndex(
            @Param("userEmail") String userEmail,
            @Param("reportedCommentId") Integer reportedCommentId
    );
}
