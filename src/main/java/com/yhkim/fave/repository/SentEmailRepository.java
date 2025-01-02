package com.yhkim.fave.repository;


import com.yhkim.fave.entities.SentEmailEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SentEmailRepository extends CrudRepository<SentEmailEntity, Long> {
}
