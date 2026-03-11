//package com.cryo.auth.service;
//
//.auth.oauth2.GoogleIdToken;import com.cryo.auth.entity.User;
////import com.cryo.auth.repository.UserRepository;
////import com.cryo.common.util.JwtTokenUtil;
////import com.google.api.client.googleapis
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//public class GoogleAuthService {
//
//    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);
//
//    @Value("${google.client.id}")
//    private String googleClientId;
//
//    private final UserRepository userRepository;
//    private final JwtTokenUtil jwtTokenUtil;
//
//    public GoogleAuthService(UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
//        this.userRepository = userRepository;
//        this.jwtTokenUtil = jwtTokenUtil;
//    }
//
//    /**
//     * Step 1: Verify Token and Check if User Exists
//     */
//    @Transactional
//    public Map<String, Object> verifyGoogleToken(String idTokenString) {
//        try {
//            // 1. Configure the Google Verifier
//            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
//                    .setAudience(Collections.singletonList(googleClientId))
//                    .build();
//
//            // 2. Verify the Token
//            GoogleIdToken idToken = verifier.verify(idTokenString);
//            if (idToken == null) {
//                throw new RuntimeException("Invalid Google ID Token");
//            }
//
//            // 3. Extract User Info from Google
//            GoogleIdToken.Payload payload = idToken.getPayload();
//            String email = payload.getEmail();
//            String name = (String) payload.get("name");
//
//            // 4. Check if User Exists in DB
//            User user = userRepository.findByEmail(email).orElse(null);
//
//            if (user == null) {
//                // === CASE A: NEW USER (INCOMPLETE) ===
//                // Return "INCOMPLETE" status so Frontend asks for Mobile Number
//                Map<String, Object> response = new HashMap<>();
//                response.put("status", "INCOMPLETE");
//                response.put("email", email);
//                response.put("name", name);
//                response.put("message", "Mobile number required to complete registration");
//                return response;
//            }
//
//            // === CASE B: EXISTING USER (LOGIN) ===
//            return generateLoginResponse(user);
//
//        } catch (GeneralSecurityException | IOException e) {
//            logger.error("Google Auth Failed", e);
//            throw new RuntimeException("Google Authentication Failed");
//        }
//    }
//
//    /**
//     * Step 2: Complete Registration with Mobile Number
//     */
//    @Transactional
//    public Map<String, Object> completeGoogleRegistration(String email, String mobileNumber, String name) {
//        // 1. Double check if user already exists
//        if (userRepository.existsByEmail(email)) {
//            throw new RuntimeException("User already registered. Please login.");
//        }
//
//        // 2. Create the User (Now we have the mandatory Mobile Number!)
//        User user = new User();
//        user.setEmail(email);
//        user.setMobileNumber(mobileNumber);
//        user.setName(name != null ? name : "Google User");
//        user.setRoles("CUSTOMER");
//        user.setStatus(User.UserStatus.ACTIVE); // Direct active, trusted email
//
//        // Generate a random ID and Password
//        user.setOwnerUserId("C" + System.currentTimeMillis());
//        user.setPasswordHash("GOOGLE_AUTH_" + UUID.randomUUID());
//
//        // 3. Save to DB
//        userRepository.save(user);
//        logger.info("Google User Registered: {}", email);
//
//        // 4. Login
//        return generateLoginResponse(user);
//    }
//
//    private Map<String, Object> generateLoginResponse(User user) {
//        // ✅ Corrected: Added user.getMobileNumber() as 4th arg
//        String accessToken = jwtTokenUtil.generateAccessToken(
//                user.getOwnerUserId(),
//                user.getRoles(),
//                user.getEmail(),
//                user.getMobileNumber()
//        );
//
//        // ✅ Corrected: Added user.getMobileNumber() as 4th arg here too!
//        String refreshToken = jwtTokenUtil.generateRefreshToken(
//                user.getOwnerUserId(),
//                user.getRoles(),
//                user.getEmail(),
//                user.getMobileNumber()
//        );
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "SUCCESS");
//        response.put("accessToken", accessToken);
//        response.put("refreshToken", refreshToken);
//        response.put("email", user.getEmail());
//        response.put("roles", user.getRoles());
//        response.put("ownerUserId", user.getOwnerUserId());
//        return response;
//    }
//}