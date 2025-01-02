package com.yhkim.fave.services;

import com.yhkim.fave.entities.EmailTokenEntity;
import com.yhkim.fave.entities.FaveInfoEntity;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.exceptions.TransactionalException;
import com.yhkim.fave.mappers.EmailTokenMapper;
import com.yhkim.fave.mappers.UserMapper;
import com.yhkim.fave.repository.UserRepository;
import com.yhkim.fave.results.CommonResult;
import com.yhkim.fave.results.Result;
import com.yhkim.fave.results.user.LoginResult;
import com.yhkim.fave.results.user.RegisterResult;
import com.yhkim.fave.results.user.ResolveRecoverPasswordResulit;
import com.yhkim.fave.results.user.ValidateEmailTokenResult;
import com.yhkim.fave.utils.CryptoUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final EmailTokenMapper emailTokenMapper;
    private final JavaMailSender mailSender; //2.  메일로 보낸다
    private final SpringTemplateEngine templateEngine; //1.  문자열로 받아와.
    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserMapper userMapper, EmailTokenMapper emailTokenMapper, JavaMailSender mailSender, UserRepository userRepository, SpringTemplateEngine templateEngine) {
        this.userMapper = userMapper;
        this.emailTokenMapper = emailTokenMapper;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.encoder = new BCryptPasswordEncoder();
        this.userRepository = userRepository;
    }

    /**
     * 사용자가 제공한 인증 정보를 확인하여 로그인 처리
     *
     * @param user 로그인하려는 사용자의 정보가 담긴 UserEntity 객체
     * @return 인증 결과를 나타내는 Result 객체
     */
    public Result login(UserEntity user) {
        // 입력 데이터의 유효성을 검사
        if (isInvalidUserInput(user)) {
            return CommonResult.FAILURE;
        }
        // 사용자 이메일로 DB에서 사용자 조회
        UserEntity dbUser = this.userMapper.selectUserByEmail(user.getEmail());
        // 사용자가 없거나 삭제된 경우 실패 반환
        if (isInvalidDbUser(dbUser)) {
            return CommonResult.FAILURE;
        }
        // 비밀번호 검증
        if (!encoder.matches(user.getPassword(), dbUser.getPassword())) {
            return CommonResult.FAILURE;
        }
        // 이메일 미인증자 체크
        if (!dbUser.isVerified()) {
            return LoginResult.FAILURE_NOT_VERIFIED;
        }
        // 계정 정지여부 체크
        if (dbUser.isSuspended()) {
            return LoginResult.FAILURE_SUSPENDED;
        }
        // 사용자 정보 업데이트
        updateUserDetails(user, dbUser);
        // 모든 유효성 검사 통과 시 성공 반환
        return CommonResult.SUCCESS;
    }

    // 사용자 입력 유효성 체크
    private boolean isInvalidUserInput(UserEntity user) {
        return user == null ||
                user.getEmail() == null || user.getEmail().length() < 8 || user.getEmail().length() > 50 ||
                user.getPassword() == null || user.getPassword().length() < 6 || user.getPassword().length() > 50;
    }

    // DB 사용자 유효성 체크
    private boolean isInvalidDbUser(UserEntity dbUser) {
        return dbUser == null || dbUser.getDeletedAt() != null;
    }

    // 사용자 정보를 DB 사용자 정보로 업데이트
    private void updateUserDetails(UserEntity user, UserEntity dbUser) {
        user.setPassword(dbUser.getPassword());
        user.setNickname(dbUser.getNickname());
        user.setContact(dbUser.getContact());
        user.setCreatedAt(dbUser.getCreatedAt());
        user.setDeletedAt(dbUser.getDeletedAt());
        user.setAdmin(dbUser.isAdmin());
        user.setSuspended(dbUser.isSuspended());
        user.setVerified(dbUser.isVerified());
    }

    @Transactional
    public Result provokeRecoverPassword(HttpServletRequest request, String email) throws MessagingException {
        if (email == null || email.length() < 8 || email.length() > 50) {
            return CommonResult.FAILURE;
        }
        UserEntity user = this.userMapper.selectUserByEmail(email);
        if (user == null || user.getDeletedAt() != null) {
            return CommonResult.FAILURE;
        }
        EmailTokenEntity emailToken = new EmailTokenEntity();
        emailToken.setUserEmail(user.getEmail());
        emailToken.setKey(CryptoUtils.hashSha512(String.format("%s%s%f%f",
                user.getEmail(),
                user.getPassword(),
                Math.random(),
                Math.random())));
        emailToken.setCreatedAt(LocalDateTime.now());
        emailToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        emailToken.setUsed(false);
        if (this.emailTokenMapper.insertEmailToken(emailToken) == 0) {
            throw new TransactionalException();
        }
        String validationLink = String.format("%s://%s:%d/user/recover-password?userEmail=%s&key=%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                emailToken.getUserEmail(),
                emailToken.getKey());
        Context context = new Context();
        context.setVariable("validationLink", validationLink);
        String mailText = this.templateEngine.process("email/recoverPassword", context);
        MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom("yellow077@naver.com"); //발신자
        mimeMessageHelper.setTo(emailToken.getUserEmail());
        mimeMessageHelper.setSubject("[FAVE] 비밀번호 재설정 인증 링크");
        mimeMessageHelper.setText(mailText, true); //html에 기반한 내용인지 확인 boolean타입
        this.mailSender.send(mimeMessage);
        return CommonResult.SUCCESS;
    }


    //결과값을 돌려줌과 동시에 이메일도 돌려줘야 하기에
    // 이메일을 돌려주되 결과값은 user
    public Result recoverEmail(UserEntity user) {
        if (user == null ||
                user.getContact() == null || user.getContact().length() < 10 || user.getContact().length() > 12) {
            return CommonResult.FAILURE;
        }
        UserEntity dbUser = this.userMapper.selectUserByContact(user.getContact());
        if (dbUser == null || dbUser.getDeletedAt() != null) {
            return CommonResult.FAILURE;
        }
        user.setEmail(dbUser.getEmail());
        return CommonResult.SUCCESS;
    }

    public Result resolveRecoverPassword(EmailTokenEntity emailToken, String password) {
        if (emailToken == null ||
                emailToken.getUserEmail() == null || emailToken.getUserEmail().length() < 8 || emailToken.getUserEmail().length() > 50 ||
                emailToken.getKey() == null || emailToken.getKey().length() != 128 ||
                password == null || password.length() < 6 || password.length() > 50) {
            return CommonResult.FAILURE;
        }
        EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectEmailTokenByUserEmailAndKey(emailToken.getUserEmail(), emailToken.getKey());
        if (dbEmailToken == null || dbEmailToken.isUsed()) {
            return CommonResult.FAILURE;
        }
        if (dbEmailToken.getExpiresAt().isBefore(LocalDateTime.now())) { //만료시간이 지났다.
            return ResolveRecoverPasswordResulit.FAILURE_EXPIRED;
        }
        dbEmailToken.setUsed(true);
        if (this.emailTokenMapper.updateEmailToken(dbEmailToken) == 0) {
            throw new TransactionalException();
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserEntity user = this.userMapper.selectUserByEmail(emailToken.getUserEmail());
        user.setPassword(encoder.encode(password));
        if (this.userMapper.updateUser(user) == 0) {
            throw new TransactionalException();
        }
        return CommonResult.SUCCESS;
    }


    @Transactional
    public Result register(HttpServletRequest request, UserEntity user) throws MessagingException {
        if (user == null ||
                user.getEmail() == null || user.getEmail().length() < 8 || user.getEmail().length() > 50 ||
                user.getPassword() == null || user.getPassword().length() < 6 || user.getPassword().length() > 50 ||
                user.getNickname() == null || user.getNickname().length() < 2 || user.getNickname().length() > 10 ||
                user.getContact() == null || user.getContact().length() < 10 || user.getContact().length() > 12) {
            return CommonResult.FAILURE;
        }
        if (this.userMapper.selectUserByEmail(user.getEmail()) != null) {
            return RegisterResult.FAILURE_DUPLICATE_EMAIL;
        }
        if (this.userMapper.selectUserByContact(user.getContact()) != null) {
            return RegisterResult.FAILURE_DUPLICATE_CONTACT;
        }
        if (this.userMapper.selectUserByNickname(user.getNickname()) != null) {
            return RegisterResult.FAILURE_DUPLICATE_NICKNAME;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedAt(null);
        user.setAdmin(false);
        user.setSuspended(false);
        user.setVerified(false);
        if (this.userMapper.insertUser(user) == 0) {
            throw new TransactionalException();
        }
        EmailTokenEntity emailToken = new EmailTokenEntity();
        emailToken.setUserEmail(user.getEmail());
        emailToken.setKey(CryptoUtils.hashSha512(String.format("%s%s%f%f",
                user.getEmail(),
                user.getPassword(),
                Math.random(),
                Math.random())));
        emailToken.setCreatedAt(LocalDateTime.now());
        emailToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        emailToken.setUsed(false);
        if (this.emailTokenMapper.insertEmailToken(emailToken) == 0) {
            throw new TransactionalException();
        }
        String validationLink = String.format("%s://%s:%d/user/validate-email-token?userEmail=%s&key=%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                emailToken.getUserEmail(),
                emailToken.getKey());
        Context context = new Context();
        context.setVariable("validationLink", validationLink);
        String mailText = this.templateEngine.process("email/register", context);
        MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom("yellow6480@gmail.com"); //발신자
        mimeMessageHelper.setTo(emailToken.getUserEmail());
        mimeMessageHelper.setSubject("[FAVE] 회원가입 인증 링크");
        mimeMessageHelper.setText(mailText, true); //html에 기반한 내용인지 확인 boolean타입
        this.mailSender.send(mimeMessage);
        return CommonResult.SUCCESS;
    }


    // 회원탈퇴 메서드
    public boolean deactivateAccount(String email) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            user.setDeletedAt(LocalDateTime.now());
            // 탈퇴 처리
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void handleUserNotVerified(UserEntity user, String validationLink) throws MessagingException {
        // 사용자가 이메일 인증을 하지 않은 경우
        // 이메일 토큰 생성
        EmailTokenEntity emailToken = new EmailTokenEntity();
        emailToken.setUserEmail(user.getEmail());
        emailToken.setKey(CryptoUtils.hashSha512(String.format("%s%s%f%f",
                user.getEmail(),
                user.getPassword(),
                Math.random(),
                Math.random()))); // 실제 구현에 맞게 키 생성
        emailToken.setCreatedAt(LocalDateTime.now());
        emailToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        emailToken.setUsed(false);

        // 이메일 토큰을 DB에 저장
        if (this.emailTokenMapper.insertEmailToken(emailToken) == 0) {
            throw new RuntimeException("Email token insert failed.");
        }

        // 이메일 인증 링크 생성 (validationLink를 생성 후 사용)
        String emailValidationLink = String.format("http://localhost:8080/user/validate-email-token?email=%s&key=%s",
                emailToken.getUserEmail(), emailToken.getKey());

        // Thymeleaf 템플릿을 사용하여 이메일 내용 생성
        Context context = new Context();
        context.setVariable("validationLink", emailValidationLink);  // 이메일 링크를 템플릿에 전달
        String mailText = templateEngine.process("email/register", context);

        // 이메일 전송
        MimeMessage mimeMessage = mailSender.createMimeMessage(); // MimeMessage 생성
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom("your-email@example.com"); // 발신자 이메일
        mimeMessageHelper.setTo(user.getEmail()); // 수신자 이메일
        mimeMessageHelper.setSubject("[FAVE] 회원가입 인증 링크");
        mimeMessageHelper.setText(mailText, true); // HTML 내용

        mailSender.send(mimeMessage);  // 이메일 전송
    }

    public Result validateEmailToken(String email, String key) { // 이메일 인증 토큰을 검증하는 메서드
        // 1. 이메일 및 키의 유효성 검사
        if (email == null || email.length() < 8 || email.length() > 50) {
            System.out.println("유효하지 않은 이메일 길이: " + email); // 디버그 로그
            return CommonResult.FAILURE;
        }
        if (key == null || key.length() != 128) {
            System.out.println("유효하지 않은 키 길이: " + (key != null ? key.length() : "null")); // 디버그 로그
            return CommonResult.FAILURE;
        }

        // 2. 이메일과 키를 사용하여 이메일 인증 토큰 조회
        EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectEmailTokenByUserEmailAndKey(email, key);
        if (dbEmailToken == null || dbEmailToken.isUsed()) { // DB에 존재하지 않거나, 이미 사용된 토큰이면
            System.out.println("토큰을 찾을 수 없거나 이미 사용된 토큰입니다."); // 디버그 로그
            return CommonResult.FAILURE;
        }

        // 3. 토큰 만료 여부 확인
        if (dbEmailToken.getExpiresAt().isBefore(LocalDateTime.now())) { // 이메일 토큰의 만료 일시가 현재 일시보다 과거이면
            System.out.println("토큰이 만료되었습니다."); // 디버그 로그
            return ValidateEmailTokenResult.FAILURE_EXPIRED;
        }

        // 4. 토큰을 사용된 것으로 처리 (인증은 한 번만 가능함으로)
        dbEmailToken.setUsed(true);
        if (this.emailTokenMapper.updateEmailToken(dbEmailToken) == 0) {
            throw new TransactionalException(); // 토큰 상태 업데이트 실패 시 예외 발생
        }

        // 5. 사용자 가져오기
        UserEntity user = this.userMapper.selectUserByEmail(email);
        if (user == null) {
            System.out.println("사용자를 찾을 수 없습니다."); // 디버그 로그
            return CommonResult.FAILURE;
        }

        // 6. 사용자 인증 처리
        user.setVerified(true);
        if (this.userMapper.updateUser(user) == 0) {
            throw new TransactionalException(); // 사용자 상태 업데이트 실패 시 예외 발생
        }

        // 7. 인증 성공
        System.out.println("이메일 인증 성공: " + user.getEmail()); // 디버그 로그
        return CommonResult.SUCCESS;
    }



    @Transactional
    public boolean updateNickname(String email, String newNickname) {   // 닉네임 업데이트 메서드
        // 새로운 닉네임이 이미 사용 중인지 확인
        if (userMapper.selectUserByNickname(newNickname) != null) {
            return false; // 중복된 닉네임이면 false 반환
        }
        // 이메일로 사용자 정보 조회
        UserEntity user = userMapper.selectUserByEmail(email);
        // 사용자 닉네임 업데이트
        user.setNickname(newNickname);
        // 업데이트 시간 설정
        user.setUpdatedAt(LocalDateTime.now());
        // 사용자 정보 업데이트 후 성공 여부 반환
        return userMapper.updateUser(user) > 0;
    }

    @Transactional
    public void updatePassword(String email, String newPassword) { // 비밀번호 업데이트 메서드
        // 이메일로 사용자 정보 조회
        UserEntity user = userMapper.selectUserByEmail(email);
        // 비밀번호 암호화 후 업데이트
        user.setPassword(encoder.encode(newPassword));
        // 업데이트 시간 설정
        user.setUpdatedAt(LocalDateTime.now());
        // 사용자 정보 업데이트
        userMapper.updateUser(user);
    }


    public List<FaveInfoEntity> getFavoritePostsByUserEmail(String email) {
        System.out.println(email);
        return userMapper.selectFavoritePostsByUserEmail(email); // 사용자 이메일로 즐겨찾기 게시물 조회
    }
}