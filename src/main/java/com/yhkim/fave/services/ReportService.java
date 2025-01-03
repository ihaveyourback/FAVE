package com.yhkim.fave.services;

import com.yhkim.fave.entities.ReportEntity;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.repository.ReportRepository;
import com.yhkim.fave.repository.UserRepository;
import com.yhkim.fave.results.CommonResult;
import com.yhkim.fave.results.Result;
import com.yhkim.fave.vos.PageVo;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public Result EmailDuplicate(ReportEntity report) {
        String status = report.getStatus();

        if ("게시글".equals(status)) {
            Optional<ReportEntity> existingReport = reportRepository.findFirstByUserEmailAndReportedPostId(
                    report.getUserEmail(), report.getReportedPostId());

            if (existingReport.isPresent() && existingReport.get().getReportedPostId() != null) {

                throw new IllegalStateException("이미 신고했습니다.");
            }
        } else if ("댓글".equals(status)) {
            Optional<ReportEntity> existingComment = reportRepository.findFirstByUserEmailAndReportedCommentId(
                    report.getUserEmail(), report.getReportedCommentId());

            if (existingComment.isPresent() && existingComment.get().getReportedCommentId() != null) {

                throw new IllegalStateException("이미 신고했습니다.");
            }
        } else {
            throw new IllegalArgumentException("잘못된 신고 상태입니다: " + status);
        }

        // 신고 처리
        report.setReportedAt(LocalDateTime.now());
        reportRepository.save(report);
        return CommonResult.SUCCESS;
    }


    //warning카운트
    @Transactional
    public void increaseWarningForReportedUser(String reportedUserEmail) {
        // 1. 사용자가 존재하는지 확인
        UserEntity user = userRepository.findById(reportedUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + reportedUserEmail));

        // 2. 경고 카운터 증가
        user.setWarning(user.getWarning() + 1);

        // 3. 저장
        userRepository.save(user);
    }

    //삭제 사용자 확인
    @Transactional
    public boolean checkIfSuspended() {
        boolean isSuspended = userRepository.existsByIsSuspended();

        if (isSuspended) {
            throw new IllegalStateException("이미 삭제된 사용자입니다.");
        }
        return isSuspended;
    }






    private String getLoggedInUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("사용자가 인증되지 않았습니다.");
        }
        return authentication.getName();  // 인증된 사용자의 이메일을 반환
    }


    // 로그인한 사용자의 이메일을 기준으로 신고 내역을 가져오는 메서드 (페이징 처리)
    public Pair<PageVo, List<ReportEntity>> getReportsByLoggedInUser(int page, int size) {
        String loggedInUserEmail = getLoggedInUserEmail(); // 로그인한 사용자의 이메일을 가져옴
        List<ReportEntity> allReports = reportRepository.findReportsByUserEmailOrderByReportedAtDesc(loggedInUserEmail).orElse(List.of());
        int totalCount = allReports.size(); // 전체 신고 내역 수 가져오기 (페이징 처리를 위해)
        PageVo pageVo = new PageVo(page, totalCount); // 페이지 정보 생성 (페이지 번호, 전체 수)
        List<ReportEntity> reports = allReports.stream() // 신고 내역을 가져오는 스트림
                .skip(pageVo.offsetCount) // 페이지 번호에 따라 스킵
                .limit(pageVo.countPerPage) // 페이지 크기에 따라 제한
                .toList(); // 리스트로 변환
        return Pair.of(pageVo, reports); // 페이지 정보와 신고 내역을 반환 (Pair 객체로 묶어서 반환)
    }
}