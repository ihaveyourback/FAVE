package com.yhkim.fave.controllers;

import com.yhkim.fave.dto.FavoritesDto;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ModelAndView getBoard(@RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        ModelAndView modelAndView = new ModelAndView();
        Pair<FaveBoardVo, FaveInfoEntity[]> pair = this.faveService.selectFaveInfo(page);
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("fave", pair.getRight());
        modelAndView.setViewName("board/faveBoard");
        return modelAndView;
    }

    @RequestMapping(value = "/read/", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getReadBoard(@RequestParam(value = "index") int index) {
        // 현재 로그인한 사용자의 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = null;
        boolean isLoggedIn = false; // 로그인 여부 확인을 위한 변수
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            // 로그인한 사용자 정보 가져오기
            UserEntity user = (UserEntity) authentication.getPrincipal();
            userEmail = user.getEmail();
            isLoggedIn = true;
        }

        // FaveInfo 조회
        FaveInfoEntity fave = this.faveService.selectFaveInfoById(index);
        this.faveService.updateFaveInfo(fave);
        // 찜 상태 확인
        boolean isLiked = false;
        if (isLoggedIn) {
            Optional<FavoritesEntity> existingLike = favoriteRepository.findByUserEmailAndFestivalId(userEmail, index);
            isLiked = existingLike.isPresent();
        }

        // 모델에 전달할 메시지
        String errorMessage = null;

        // 로그인하지 않은 상태에서 찜하기 버튼을 눌렀을 경우
        if (!isLoggedIn) {
            errorMessage = "로그인 후 찜할 수 있습니다.";
        }

        // 찜 상태를 모델에 추가
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("fave", fave);
        modelAndView.addObject("isLiked", isLiked);
        modelAndView.addObject("userEmail", userEmail);  // 수정된 부분: userEmail을 템플릿으로 전달
        modelAndView.addObject("errorMessage", errorMessage); // 로그인하지 않으면 에러 메시지를 전달
        modelAndView.setViewName("board/faveRead");

        return modelAndView;
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
        response.put("message", "찜 상태가 변경되었습니다.");
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
                                    @RequestParam(value = "filter", required = false, defaultValue = "all") String filter) {
        ModelAndView modelAndView = new ModelAndView();
        Pair<FaveBoardVo, FaveInfoEntity[]> pair = this.faveService.searchFaveInfo(page, filter, keyword);
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("fave", pair.getRight());
        modelAndView.setViewName("board/faveBoard");
        return modelAndView;
    }
}

