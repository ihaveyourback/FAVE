package com.yhkim.fave.services;

import com.yhkim.fave.entities.Report;
import com.yhkim.fave.repository.ReportRepository;
import com.yhkim.fave.vos.PageVo;
import org.apache.commons.lang3.tuple.Pair;
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



    // 로그인한 사용자의 이메일을 기준으로 신고 내역을 가져오는 메서드 (페이징 처리)
    public Pair<PageVo, List<Report>> getReportsByLoggedInUser(int page, int size) {
        String loggedInUserEmail = getLoggedInUserEmail();
        List<Report> allReports = reportRepository.findReportsByUserEmailOrderByReportedAtDesc(loggedInUserEmail).orElse(List.of());
        int totalCount = allReports.size();
        PageVo pageVo = new PageVo(page, totalCount);
        List<Report> reports = allReports.stream()
                .skip(pageVo.offsetCount)
                .limit(pageVo.countPerPage)
                .toList();
        return Pair.of(pageVo, reports);
    }
}