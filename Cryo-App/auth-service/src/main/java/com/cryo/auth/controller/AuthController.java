package com.cryo.auth.controller;
import com.cryo.auth.dto.*;
import com.cryo.auth.entity.User;
import com.cryo.auth.repository.UserRepository; // ✅ Import Repository
import com.cryo.auth.service.AuthService;
import com.cryo.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    //private final GoogleAuthService googleAuthService;// ✅ Inject Repository

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        //this.googleAuthService=googleAuthService;
    }
    // ==========================================
    // ✅ FIXED ENDPOINT: Get Profile
    // ==========================================
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyProfile() {
        // 1. Get the current logged-in user's ID from the token (e.g., C00009)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String ownerUserId = auth.getName();

        // 2. Fetch full details using the ID (NOT email)
        User user = authService.getProfile(ownerUserId);

        // 3. Map to a response
        Map<String, Object> response = new HashMap<>();
        response.put("ownerUserId", user.getOwnerUserId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("mobileNumber", user.getMobileNumber());
        // 🔴 ADD THIS MISSING LINE:
        response.put("alternativeMobileNumber", user.getAlternativeMobileNumber());
        response.put("roles", user.getRoles());
        // ✅ CRITICAL: Make sure these lines exist!
        response.put("notifyWhatsapp", user.getNotifyWhatsapp());
        response.put("notifySms", user.getNotifySms());
        response.put("notifyEmail", user.getNotifyEmail());
        response.put("status", user.getStatus());
        response.put("isVerified", user.getStatus() == User.UserStatus.ACTIVE);

        return ResponseEntity.ok(ApiResponse.success("User profile fetched successfully", response));
    }
    // Inside AuthController.java

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(@RequestBody UpdateProfileRequest request) {
        // 1. Get Logged in ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String ownerUserId = auth.getName();

        // 2. Update
        User updatedUser = authService.updateProfile(ownerUserId, request);

        // 3. Prepare Response
        Map<String, Object> response = new HashMap<>();
        response.put("ownerUserId", updatedUser.getOwnerUserId());
        response.put("name", updatedUser.getName());
        response.put("email", updatedUser.getEmail());
        response.put("mobileNumber", updatedUser.getMobileNumber());
        response.put("alternativeMobileNumber", updatedUser.getAlternativeMobileNumber()); // ✅ Return new field
        // ✅ Return new settings to frontend
        response.put("notifyWhatsapp", updatedUser.getNotifyWhatsapp());
        response.put("notifySms", updatedUser.getNotifySms());
        response.put("notifyEmail", updatedUser.getNotifyEmail());
        response.put("roles", updatedUser.getRoles());

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
    // ==========================================
    // ✅ NEW ENDPOINT FOR FREEZER SERVICE
    // ==========================================
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable("userId") String userId) {

        // We use findByOwnerUserId because that represents "C00009"
        User user = userRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Map to DTO
        // We use user.getOwnerUserId() because that is the name in your Entity
        // ✅ IMPORTANT: You must pass the notification flags here!
        UserProfileDto dto = new UserProfileDto(
                user.getOwnerUserId(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getAlternativeMobileNumber(),
                user.getNotifyWhatsapp(), // <--- Sending to Freezer
                user.getNotifySms(),      // <--- Sending to Freezer
                user.getNotifyEmail()     // <--- Sending to Freezer
        );

        return ResponseEntity.ok(dto);
    }

    // =========================
    // POST endpoints (Existing)
    // =========================

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully. Please check your email for OTP.", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        authService.verifyOtp(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully. Account activated.", null));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestBody OtpVerificationRequest request) {
        authService.resendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully. Please check your email.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }
    // ✅ NEW: Internal API for Chatbot to find User ID
    @GetMapping("/internal/mobile/{mobileNumber}")
    public ResponseEntity<String> getUserIdByMobile(@PathVariable("mobileNumber") String mobileNumber) {
        // Simple lookup
        return userRepository.findByMobileNumber(mobileNumber)
                .map(user -> ResponseEntity.ok(user.getOwnerUserId()))
                .orElse(ResponseEntity.notFound().build());
    }



//    // ✅ NEW: Step 1 - Verify Google Token
//    @PostMapping("/google")
//    public ResponseEntity<ApiResponse<Map<String, Object>>> googleLogin(@RequestBody Map<String, String> request) {
//        String idToken = request.get("token");
//
//        if (idToken == null || idToken.isBlank()) {
//            return ResponseEntity.badRequest().body(ApiResponse.error("Token is required"));
//        }
//
//        try {
//            Map<String, Object> authData = googleAuthService.verifyGoogleToken(idToken);
//            return ResponseEntity.ok(ApiResponse.success("Google Check Successful", authData));
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    // ✅ NEW: Step 2 - Complete Signup with Mobile
//    @PostMapping("/google/complete")
//    public ResponseEntity<ApiResponse<Map<String, Object>>> completeGoogleSignup(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//        String mobile = request.get("mobileNumber");
//        String name = request.get("name"); // Optional
//
//        if (email == null || mobile == null) {
//            return ResponseEntity.badRequest().body(ApiResponse.error("Email and Mobile are required"));
//        }
//
//        try {
//            Map<String, Object> authData = googleAuthService.completeGoogleRegistration(email, mobile, name);
//            return ResponseEntity.ok(ApiResponse.success("Registration Complete", authData));
//        } catch (Exception e) {
//            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
//        }
//    }


    // =========================
    // Safe GET fallbacks
    // =========================

//    @GetMapping("/signup")
//    public ResponseEntity<ApiResponse<Void>> signupGetFallback() {
//        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error("Use POST /auth/signup"));
//    }
//
//    @GetMapping("/verify-otp")
//    public ResponseEntity<ApiResponse<Void>> verifyOtpGetFallback() {
//        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error("Use POST /auth/verify-otp"));
//    }
//
//    @GetMapping("/resend-otp")
//    public ResponseEntity<ApiResponse<Void>> resendOtpGetFallback() {
//        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error("Use POST /auth/resend-otp"));
//    }
//
//    @GetMapping("/login")
//    public ResponseEntity<ApiResponse<Void>> loginGetFallback() {
//        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error("Use POST /auth/login"));
//    }
//
//    @GetMapping("/refresh")
//    public ResponseEntity<ApiResponse<Void>> refreshGetFallback() {
//        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error("Use POST /auth/refresh"));
//    }
   }