package com.yhkim.fave.services;

import com.yhkim.fave.entities.Report;
import com.yhkim.fave.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    private String getLoggedInUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("사용자가 인증되지 않았습니다.");
        }
        return authentication.getName();  // 인증된 사용자의 이메일을 반환
    }

    // 로그인한 사용자의 이메일을 기준으로 신고 내역을 가져오는 메서드
    public List<Report> getReportsByLoggedInUser() {
        String loggedInUserEmail = getLoggedInUserEmail();
        Optional<List<Report>> report = reportRepository.findReportsByUserEmailOrderByReportedAtDesc(loggedInUserEmail);
        if(report.isPresent()) {
            return report.get();
        }
        return null;
    }
}

