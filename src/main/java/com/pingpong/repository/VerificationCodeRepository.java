package com.pingpong.repository;

import com.pingpong.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByCodeAndTypeAndUsedFalse(String code, String type);

    List<VerificationCode> findByUserIdAndTypeAndUsedFalse(Long userId, String type);

    void deleteByUserId(Long userId);
}
