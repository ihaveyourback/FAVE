package com.yhkim.fave.repository;

import com.yhkim.fave.entities.ReportEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {


    @Query("SELECT r FROM fave r WHERE r.userEmail = :userEmail AND r.reportedPostId IS NOT NULL AND r.reportedPostId = :reportedPostId")
    Optional<ReportEntity> findFirstByUserEmailAndReportedPostId(
            @Param("userEmail") String userEmail,
            @Param("reportedPostId") Integer reportedPostId
    );

    @Query("SELECT r FROM fave r WHERE r.userEmail = :userEmail AND r.reportedCommentId IS NOT NULL AND r.reportedCommentId = :reportedCommentId")
    Optional<ReportEntity> findFirstByUserEmailAndReportedCommentId(
            @Param("userEmail") String userEmail,
            @Param("reportedCommentId") Integer reportedCommentId
    );


    List<ReportEntity> findByCurrentStatus(String currentStatus);


    // 로그인한 사용자의 이메일을 기준으로 신고 내역을 찾는 메서드
    Optional<List<ReportEntity>> findReportsByUserEmailOrderByReportedAtDesc(String loggedInUserEmail);
}
