package com.cryo.auth.repository;
import com.cryo.auth.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByUserIdAndUsedFalse(Long userId);

    @Query("SELECT o FROM Otp o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Otp> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}

