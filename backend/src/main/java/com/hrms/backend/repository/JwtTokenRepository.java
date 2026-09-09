package com.hrms.backend.repository;
import com.hrms.backend.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface JwtTokenRepository extends JpaRepository<JwtToken, Integer> {
    Optional<JwtToken> findByToken(String token);
    
    void deleteByExpiresAtBefore(java.time.LocalDateTime date);
    void deleteByToken(String token);
}
