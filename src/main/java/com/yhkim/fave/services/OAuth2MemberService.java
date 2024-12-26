package com.yhkim.fave.services;

import com.yhkim.fave.entities.CustomOAuth2User;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

@Service
public class OAuth2MemberService extends DefaultOAuth2UserService { // OAuth2UserService를 상속받아 사용자 정보를 로드하는 서비스 클래스

    private static final Logger logger = LoggerFactory.getLogger(OAuth2MemberService.class); // 로거 객체 생성

    @Autowired // 사용자 레포지토리 주입
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2UserRequest를 통해 사용자 정보를 로드
        OAuth2User oAuth2User = super.loadUser(userRequest); // OAuth2User 객체를 가져옴

        // 제공자 이름을 대문자로 변환하여 가져옴
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase(); // 제공자 이름을 가져옴
        Map<String, Object> attributes = oAuth2User.getAttributes(); // 속성을 가져옴

        // 제공자 ID, 이메일, 닉네임, 연락처를 가져옴
        String providerId = getProviderId(attributes, provider); // 제공자 ID를 가져옴
        String email = getEmail(attributes, provider); // 이메일을 가져옴
        String nickname = getNickname(attributes, provider); // 닉네임을 가져옴
        String contact = getContact(attributes, provider); // 연락처를 가져옴

        // 이메일로 사용자 존재 여부 확인
        if (userRepository.existsByEmail(email)) { // 이메일이 존재하면 (이미 가입한 사용자)
            UserEntity userEntity = userRepository.findByEmail(email) // 사용자 엔티티를 가져옴 (이메일로)
                    .orElseThrow(() -> new OAuth2AuthenticationException("The account has been deleted.")); // 사용자가 삭제되었을 경우 예외 발생
            if (userEntity.getDeletedAt() != null) { // 사용자가 삭제되었을 경우 예외 발생
                throw new OAuth2AuthenticationException("The account has been deleted.");
            }
            email = userEntity.getEmail(); // 이메일은 고정
            // nickname = userEntity.getNickname(); // 고유 닉네임을 보장하기 위해 주석 처리
            contact = userEntity.getContact(); // 연락처는 고정
        } else {
            // 고유 닉네임을 보장하고 사용자 생성
            nickname = ensureUniqueNickname(nickname); // 고유 닉네임을 보장
            createUser(email, provider, providerId, nickname, contact); // 사용자 생성
        }

        // CustomOAuth2User 객체를 반환
        return new CustomOAuth2User(oAuth2User, provider, email, nickname, contact, email);
    } // OAuth2User 객체 반환

    private void createUser(String email, String provider, String providerId, String nickname, String contact) {
        // 새로운 사용자 생성
        UserEntity userEntity = UserEntity.builder() // 사용자 엔티티 빌더
                .email(email) // 이메일
                .password("") // 비밀번호 없음
                .oauth2Provider(provider) // 제공자 이름
                .oauth2Id(providerId) // 제공자 ID
                .nickname(nickname) // 닉네임
                .contact(contact) // 연락처
                .verified(true) // 인증된 사용자
                .build(); // 사용자 엔티티 생성
        userRepository.save(userEntity); // 사용자 저장
    }

    private String getProviderId(Map<String, Object> attributes, String provider) {
        // 제공자 ID를 가져옴
        if ("KAKAO".equals(provider)) { // 카카오인 경우
            return attributes.get("id").toString(); // ID를 가져옴
        } else if ("NAVER".equals(provider)) { // 네이버인 경우
            return getAttribute(attributes, "response.id", "defaultProviderId");
        } // ID를 가져옴
        logger.debug("Provider ID not found for provider: {}", provider); // 제공자 ID를 찾을 수 없음
        return "defaultProviderId"; // 기본 제공자 ID 반환
    } // ID 반환

    private String getEmail(Map<String, Object> attributes, String provider) {
        // 이메일을 가져옴
        if ("KAKAO".equals(provider)) { // 카카오인 경우
            return "kakao_" + attributes.get("id") + "@kakao.com";
        } else if ("NAVER".equals(provider)) { // 네이버인 경우
            return getAttribute(attributes, "response.email", "defaultEmail@example.com"); // 이메일을 가져옴
        }
        logger.debug("Email not found for provider: {}", provider); // 이메일을 찾을 수 없음
        return "defaultEmail@example.com"; // 기본 이메일 반환  // 카카오는 이메일을 가져올 수 없어서 그럼, 네이버는 이메일을 가져올 수 있음
    } // 이메일 반환

    private String getNickname(Map<String, Object> attributes, String provider) {
        // 닉네임을 가져옴
        if ("KAKAO".equals(provider)) {
            return getAttribute(attributes, "properties.nickname", "defaultNickname"); // 카카오인 경우
        } else if ("NAVER".equals(provider)) {
            return getAttribute(attributes, "response.nickname", "defaultNickname"); // 네이버인 경우
        }
        logger.debug("Nickname not found for provider: {}", provider);
        return "defaultNickname";
    }

    private String getContact(Map<String, Object> attributes, String provider) {
        // 연락처를 가져옴
        if ("KAKAO".equals(provider)) {
            return getAttribute(attributes, "kakao_account.phone_number", generateRandomPhoneNumber());
        } else if ("NAVER".equals(provider)) {
            return getAttribute(attributes, "response.mobile", generateRandomPhoneNumber());
        }
        logger.debug("Contact not found for provider: {}", provider);
        return generateRandomPhoneNumber();
    }

    private String generateRandomPhoneNumber() {
        // 랜덤 전화번호 생성
        Random random = new Random(); // 랜덤 객체 생성
        StringBuilder phoneNumber = new StringBuilder(); // 전화번호 문자열 생성
        phoneNumber.append("000-"); // 전화번호 앞자리
        for (int i = 0; i < 8; i++) { // 8자리 랜덤 숫자 생성
            phoneNumber.append(random.nextInt(10)); // 0부터 9까지 랜덤 숫자 생성
            if (i == 3) phoneNumber.append("-"); // 4번째 자리에 하이픈 추가
        } // 8자리 랜덤 숫자 생성
        return phoneNumber.toString(); // 전화번호 문자열 반환
    }

    private String ensureUniqueNickname(String nickname) {
        // 고유 닉네임을 보장
        String originalNickname = nickname; // 원래 닉네임
        int count = 1; // 카운트
        while (userRepository.existsByNickname(nickname)) { // 닉네임이 존재하면
            nickname = originalNickname + count; // 카운트를 추가하여 닉네임 생성
            if (nickname.length() > 10) { //  닉네임이 10자 이상이면
                nickname = nickname.substring(0, 10); // 10자로 자름
            }
            count++; // 카운트 증가
        }
        return nickname; // 고유 닉네임 반환
    }

    private String getAttribute(Map<String, Object> attributes, String keyPath, String defaultValue) {
        // 속성을 가져옴
        String[] keys = keyPath.split("\\."); // 키 경로를 분리
        Map<String, Object> current = attributes; // 현재 속성

        for (int i = 0; i < keys.length; i++) { // 키 경로 순회
            Object value = current.get(keys[i]); // 값 가져옴
            if (value == null) return defaultValue; // 값이 없으면 기본값 반환

            if (i == keys.length - 1) return value.toString(); // 마지막 키인 경우 문자열로 반환
            if (!(value instanceof Map)) return defaultValue; // 값이 Map이 아니면 기본값 반환

            current = castToMap(value); // Map으로 캐스팅
        }
        return defaultValue; // 기본값 반환
    }

    @SuppressWarnings("unchecked") // 경고 억제
    private Map<String, Object> castToMap(Object obj) {
        // 객체를 Map으로 캐스팅
        if (obj instanceof Map) { // Map인 경우
            return (Map<String, Object>) obj; // Map으로 캐스팅
        }
        return null; // Map이 아닌 경우 null 반환
    }
}