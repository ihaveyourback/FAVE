package com.yhkim.fave.controllers;

import com.yhkim.fave.dto.FavoritesDto;
import com.yhkim.fave.entities.CustomOAuth2User;
import com.yhkim.fave.entities.FaveInfoEntity;
import com.yhkim.fave.entities.FavoritesEntity;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.repository.FavoriteRepository;
import com.yhkim.fave.services.FaveService;
import com.yhkim.fave.services.FavoriteService;
import com.yhkim.fave.vos.FaveBoardVo;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(value = "/fave")
public class FaveBoardController {

    private final FaveService faveService;
    private final FavoriteService favoriteService;
    private final FavoriteRepository favoriteRepository;

    @Autowired
    public FaveBoardController(FaveService faveService, FavoriteService favoriteService, FavoriteRepository favoriteRepository) {
        this.faveService = faveService;
        this.favoriteService = favoriteService;
        this.favoriteRepository = favoriteRepository;

    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView getBoard(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 @AuthenticationPrincipal Object principal) {
        ModelAndView modelAndView = new ModelAndView();
        if (userDetails instanceof UserEntity user) {// 사용자 정보가 UserEntity 객체인 경우
            modelAndView.addObject("user", user); // user 객체 생성
            modelAndView.addObject("isAdmin", user.isAdmin()); // 관리자 여부를 가져옴
            modelAndView.addObject("email", user.getEmail());
            modelAndView.addObject("nickname", user.getNickname());
        }else if (principal instanceof CustomOAuth2User){ // 소셜 이메일 가져오기
            String email = ((CustomOAuth2User) principal).getEmail();
            modelAndView.addObject("email", email);
        }
        Pair<FaveBoardVo, FaveInfoEntity[]> pair = this.faveService.selectFaveInfo(page);
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("fave", pair.getRight());
        modelAndView.setViewName("board/faveBoard");
        return modelAndView;
    }

    @RequestMapping(value = "/read/", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getReadBoard(
            @RequestParam(value = "index") int index,
            @AuthenticationPrincipal UserDetails userDetails,
            @AuthenticationPrincipal Object principal) {

        ModelAndView modelAndView = new ModelAndView("board/faveRead");
        String userEmail = extractUserEmail(userDetails, principal);
        boolean isLoggedIn = (userEmail != null);

        // 사용자 정보 추가 (웹소켓 활용 가능)
        if (userDetails instanceof UserEntity user) {
            modelAndView.addObject("user", user);
            modelAndView.addObject("isAdmin", user.isAdmin());
            modelAndView.addObject("email", user.getEmail());
            modelAndView.addObject("nickname", user.getNickname());
        } else if (principal instanceof CustomOAuth2User oAuthUser) {
            modelAndView.addObject("email", oAuthUser.getEmail());
        }

        // 찜 상태 확인
        boolean isLiked = isLoggedIn && favoriteRepository
                .findByUserEmailAndFestivalId(userEmail, index)
                .isPresent();

        // 축제 정보 조회 및 업데이트
        FaveInfoEntity fave = faveService.selectFaveInfoById(index);
        faveService.updateFaveInfo(fave);

        // 모델에 데이터 추가
        modelAndView.addObject("fave", fave);
        modelAndView.addObject("isLiked", isLiked);
        modelAndView.addObject("userEmail", userEmail);

        return modelAndView;
    }

    /**
     * 사용자 이메일 추출 메서드 (일반 로그인 및 소셜 로그인 대응)
     */
    private String extractUserEmail(UserDetails userDetails, Object principal) {
        if (userDetails instanceof UserEntity user) {
            return user.getEmail();
        } else if (principal instanceof CustomOAuth2User oAuthUser) {
            return oAuthUser.getEmail();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User oauth2User) {
            Map<String, Object> attributes = oauth2User.getAttributes();
            return (String) attributes.get("email");
        }
        return null;
    }


    @RequestMapping(value = "/read/status", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> getLikeStatus(@RequestParam(value = "index") int index,
                                                              @RequestParam(value = "userEmail") String userEmail) {
        // 찜 상태 확인
        Optional<FavoritesEntity> existingLike = favoriteRepository.findByUserEmailAndFestivalId(userEmail, index);
        boolean isLiked = existingLike.isPresent(); // 찜한 상태 여부

        // 응답을 JSON 형태로 반환
        Map<String, Boolean> response = new HashMap<>();
        response.put("isLiked", isLiked);

        return ResponseEntity.ok(response);
    }


    @RequestMapping(value = "/read/", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleLike(@RequestBody FavoritesDto favoritesDto) {
        favoriteService.saveSpotLike(favoritesDto);
        System.out.println("Received userEmail: " + favoritesDto.getUserEmail());
        System.out.println("Received festivalId: " + favoritesDto.getFestivalId());
        Map<String, String> response = new HashMap<>();
        response.put("message", "찜이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 찜 취소하기 처리 (DELETE)
    @DeleteMapping("/read/")
    public ResponseEntity<Map<String, String>> cancelSpotLike(@RequestBody FavoritesDto favoritesDto) {
        favoriteService.removeSpotLike(favoritesDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "찜이 취소되었습니다.");
        return ResponseEntity.ok(response);
    }


    @RequestMapping(value = "image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@RequestParam(value = "index", required = false, defaultValue = "0") int index) {
        FaveInfoEntity fave = this.faveService.selectFaveInfoById(index);
        if (fave == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentLength(fave.getCoverData().length)
                .contentType(MediaType.parseMediaType(fave.getCoverContentType()))
                .body(fave.getCoverData());
    }

    @RequestMapping(value = "/get-address", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> getAddress(@RequestParam(value = "index", required = false, defaultValue = "0") int index) {
        FaveInfoEntity fave = this.faveService.selectFaveInfoById(index);
        if (fave == null || fave.getLocation() == null) {
            return ResponseEntity.notFound().build();
        }
        JSONObject response = new JSONObject();
        response.put("address", fave.getLocation());
        return ResponseEntity.ok(response.toString());
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView searchBoard(@RequestParam(value = "keyword", required = false) String keyword,
                                    @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                    @RequestParam(value = "filter", required = false, defaultValue = "all") String filter,
                                    @AuthenticationPrincipal Object principal,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        Pair<FaveBoardVo, FaveInfoEntity[]> pair = this.faveService.searchFaveInfo(page, filter, keyword);
        // 사용자 정보 추가 (웹소켓 활용 가능)
        if (userDetails instanceof UserEntity user) {
            modelAndView.addObject("user", user);
            modelAndView.addObject("isAdmin", user.isAdmin());
            modelAndView.addObject("email", user.getEmail());
            modelAndView.addObject("nickname", user.getNickname());
        } else if (principal instanceof CustomOAuth2User oAuthUser) {
            modelAndView.addObject("email", oAuthUser.getEmail());
        }
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("fave", pair.getRight());
        modelAndView.setViewName("board/faveBoard");
        return modelAndView;
    }
}

