package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.RefreshToken;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends EntityRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteAllByUserId(long userId);

    void deleteAllByExpireDateLessThan(Instant now);

    void deleteById(Long id);

    Long count();
}
