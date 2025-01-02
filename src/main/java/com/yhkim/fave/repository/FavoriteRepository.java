package com.yhkim.fave.repository;


import com.yhkim.fave.entities.FavoritesEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoritesEntity, Long> {

    Optional<FavoritesEntity> findByUserEmailAndFestivalId(@Param("userEmail")String userEmail,
                                                           @Param("festivalId")Integer festivalId);



}

