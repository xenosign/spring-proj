package com.tetz.spring_proj.user.repository;

import com.tetz.spring_proj.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByKakaoId(Long kakaoId);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByKakaoId(Long kakaoId);
}