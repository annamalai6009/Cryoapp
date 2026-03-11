package com.cryo.auth.service;

import com.cryo.auth.entity.Otp;
import com.cryo.auth.repository.OtpRepository;
import com.cryo.common.exception.OtpExpiredException;
import com.cryo.common.exception.OtpInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;

    private final OtpRepository otpRepository;
    private final Random random = new Random();

    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public String generateOtp() {
        int otp = random.nextInt(900000) + 100000;
        return String.format("%06d", otp);
    }

    @Transactional
    public Otp createOtp(Long userId) {
        invalidateExistingOtps(userId);

        String otpCode = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        Otp otp = new Otp();
        otp.setUserId(userId);
        otp.setOtpCode(otpCode);
        otp.setExpiryTime(expiryTime);
        otp.setAttemptCount(0);
        otp.setUsed(false);

        Otp savedOtp = otpRepository.save(otp);
        // In Docker/dev environments email delivery may be disabled; logging OTP helps complete verification.
        logger.info("OTP generated for user: {}, otp: {}, expires at: {}", userId, otpCode, expiryTime);
        return savedOtp;
    }

    @Transactional
    public void invalidateExistingOtps(Long userId) {
        List<Otp> existingOtps = otpRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Otp otp : existingOtps) {
            if (!otp.getUsed()) {
                otp.setUsed(true);
                otpRepository.save(otp);
            }
        }
    }

    @Transactional
    public void validateOtp(Long userId, String otpCode) {
        Optional<Otp> otpOpt = otpRepository.findByUserIdAndUsedFalse(userId);

        if (otpOpt.isEmpty()) {
            throw new OtpInvalidException("No active OTP found. Please request a new OTP.");
        }

        Otp otp = otpOpt.get();

        if (otp.isExpired()) {
            otp.setUsed(true);
            otpRepository.save(otp);
            throw new OtpExpiredException("OTP has expired. Please request a new OTP.");
        }

        if (otp.getAttemptCount() >= MAX_ATTEMPTS) {
            otp.setUsed(true);
            otpRepository.save(otp);
            throw new OtpInvalidException("Maximum attempts exceeded. Please request a new OTP.");
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            otpRepository.save(otp);
            int remainingAttempts = MAX_ATTEMPTS - otp.getAttemptCount();
            if (remainingAttempts > 0) {
                throw new OtpInvalidException(
                        String.format("Invalid OTP. %d attempts remaining.", remainingAttempts));
            } else {
                otp.setUsed(true);
                otpRepository.save(otp);
                throw new OtpInvalidException("Maximum attempts exceeded. Please request a new OTP.");
            } 
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        logger.info("OTP validated successfully for user: {}", userId);
    }
}

