package com.yhkim.fave.controllers;

import com.yhkim.fave.dto.FavoritesDto;
import com.yhkim.fave.entities.FaveInfoEntity;
import com.yhkim.fave.entities.FavoritesEntity;
import com.yhkim.fave.repository.FavoriteRepository;
import com.yhkim.fave.services.FaveService;
import com.yhkim.fave.services.FavoriteService;
import com.yhkim.fave.vos.FaveBoardVo;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final FavoriteRepository favoriteRepository;
    private final FavoriteService favoriteService;

    @Autowired
    public FaveBoardController(FaveService faveService, FavoriteRepository favoriteRepository, FavoriteService favoriteService) {
        this.faveService = faveService;
        this.favoriteRepository = favoriteRepository;
        this.favoriteService = favoriteService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView getBoard(@RequestParam(value = "page", required = false, defaultValue = "1")int page) {
        ModelAndView modelAndView = new ModelAndView();
        Pair<FaveBoardVo, FaveInfoEntity[]> pair = this.faveService.selectFaveInfo(page);
        modelAndView.addObject("page", pair.getLeft());
        modelAndView.addObject("fave", pair.getRight());
        modelAndView.setViewName("board/faveBoard");
        return modelAndView;
    }

    @RequestMapping(value = "/read/", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getReadBoard(@RequestParam(value = "index") int index,
                                     @RequestParam(value = "userEmail") String userEmail) {
        // FaveInfo 조회
        FaveInfoEntity fave = this.faveService.selectFaveInfoById(index);

        // 찜 상태 확인
        Optional<FavoritesEntity> existingLike = favoriteRepository.findByUserEmailAndFestivalId(userEmail, index);
        boolean isLiked = existingLike.isPresent(); // 찜한 상태 여부
        System.out.println("isLiked 상태: " + existingLike.isPresent()); // 디버깅용 로그

        // 찜 상태를 모델에 추가
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("fave", fave);
        modelAndView.addObject("isLiked", isLiked); // 찜 상태 (true/false) 추가
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



    @RequestMapping(value = "/read/" ,method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> handleLike(@RequestBody FavoritesDto favoritesDto) {
        favoriteService.saveSpotLike(favoritesDto);
        return ResponseEntity.ok("찜 상태가 변경되었습니다.");
    }
    // 찜 취소하기 처리 (DELETE)
    @DeleteMapping("/read/")
    public ResponseEntity<String> cancelSpotLike(@RequestBody FavoritesDto favoritesDto) {
        favoriteService.removeSpotLike(favoritesDto);
        return ResponseEntity.ok("찜이 취소되었습니다.");
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
