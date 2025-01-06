package com.yhkim.fave.services;



import com.yhkim.fave.dto.FavoritesDto;
import com.yhkim.fave.entities.FavoritesEntity;
import com.yhkim.fave.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    @Autowired
    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public void saveSpotLike(FavoritesDto favoritesDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // 로그인된 사용자의 이메일

        FavoritesEntity favoritesEntity = new FavoritesEntity();
        favoritesEntity.setUserEmail(userEmail);
        favoritesEntity.setFestivalId(favoritesDto.getFestivalId());
        favoriteRepository.save(favoritesEntity);
    }

    public void removeSpotLike(FavoritesDto favoritesDto) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 해당 데이터를 DB에서 삭제
        Optional<FavoritesEntity> like = favoriteRepository.findByUserEmailAndFestivalId(userEmail, favoritesDto.getFestivalId());
        like.ifPresent(favoriteRepository::delete);  // 값이 있으면 삭제
    }
}
