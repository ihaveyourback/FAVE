package com.yhkim.fave.repository;

import com.yhkim.fave.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {


    // 로그인한 사용자의 이메일을 기준으로 신고 내역을 찾는 메서드
    Optional<List<Report>> findReportsByUserEmailOrderByReportedAtDesc(String loggedInUserEmail);
}
