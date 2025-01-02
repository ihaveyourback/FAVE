package com.yhkim.fave.schedulers;

import com.yhkim.fave.entities.ReportEntity;
import com.yhkim.fave.entities.UserEntity;

import com.yhkim.fave.repository.ReportRepository;
import com.yhkim.fave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // 이미 처리된 이메일을 추적하는 HashSet (스케줄 실행마다 초기화됨)
    private final Set<String> processedEmails = new HashSet<>();

    @Transactional
//    @Scheduled(cron = "0 */1 * * * *") // 1분마다 실행
    public void countUserWarning() {
        System.out.println("countUserWarning 실행함");

        // 1. '신고 처리 완료' 상태인 신고들 가져오기
        List<ReportEntity> reports = reportRepository.findByCurrentStatus("신고 처리 완료");

        // 2. 상태를 우선 '처리 중'으로 변경
        for (ReportEntity report : reports) {
            report.setCurrentStatus("처리 중");
            reportRepository.save(report);
        }

        // 3. 신고 처리 로직
        for (ReportEntity report : reports) {
            String reportedUserEmail = report.getReportedUserEmail();

            // 4. 이미 처리된 이메일인지 확인
            if (processedEmails.contains(reportedUserEmail)) {
                continue; // 중복 처리 방지
            }

            // 5. 사용자 찾기
            UserEntity user = userRepository.findById(reportedUserEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + reportedUserEmail));

            // 6. 경고 수 증가
            int currentWarning = user.getWarning();
            int newWarning = currentWarning + 1;

            // 7. 경고 값 제한: 최대 3
            if (newWarning >= 3) {
                newWarning = 3;
                user.setSuspended(true); // 계정 정지
            }

            user.setWarning(newWarning);
            userRepository.save(user);

            // 8. 최종 상태 변경
            report.setCurrentStatus("경고 처리 완료");
            reportRepository.save(report);

            // 9. 처리된 이메일을 기록
            processedEmails.add(reportedUserEmail);
            System.out.println("경고 처리된 이메일: " + reportedUserEmail);
        }

        // 10. 스케줄 실행마다 처리된 이메일 초기화
        processedEmails.clear();
    }
}
