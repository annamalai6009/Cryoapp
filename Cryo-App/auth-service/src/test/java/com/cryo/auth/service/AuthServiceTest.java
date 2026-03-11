package com.cryo.auth.service;

import com.cryo.auth.dto.LoginRequest;
import com.cryo.auth.dto.SignupRequest;
import com.cryo.auth.entity.User;
import com.cryo.auth.repository.UserRepository;
import com.cryo.common.exception.BadRequestException;
import com.cryo.common.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    // Defined constants for testing
    private static final String JWT_SECRET = "test-secret-key-minimum-32-characters-long";
    private static final Long JWT_ACCESS_EXPIRATION = 900L;       // 15 mins
    private static final Long JWT_REFRESH_EXPIRATION = 604800L;   // 7 days

    @BeforeEach
    void setUp() {
        // ✅ FIX 1: Pass BOTH expiration arguments to the constructor
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                otpService,
                emailService,
                JWT_SECRET,
                JWT_ACCESS_EXPIRATION,
                JWT_REFRESH_EXPIRATION
        );
    }

    @Test
    void testSignup_Success() {
        SignupRequest request = new SignupRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setMobileNumber("+1234567890");
        request.setPassword("Test@1234");

        // 1. Mock Repository checks
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        // Optional: when(userRepository.existsByMobileNumber(anyString())).thenReturn(false);
        when(userRepository.findByUserId()).thenReturn(Optional.empty());

        // 2. Mock Password Encoder
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // 3. Mock User Save
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // ✅ FIX IS HERE: Create a dummy OTP and tell Mockito to return it
        com.cryo.auth.entity.Otp dummyOtp = new com.cryo.auth.entity.Otp();
        dummyOtp.setOtpCode("123456");
        when(otpService.createOtp(any(Long.class))).thenReturn(dummyOtp);

        // 4. Run the method
        assertDoesNotThrow(() -> authService.signup(request));

        // 5. Verify calls
        verify(userRepository, times(1)).save(any(User.class));
        verify(otpService, times(1)).createOtp(any(Long.class));
        verify(emailService, times(1)).sendOtpEmail(anyString(), anyString());
    }
    @Test
    void testSignup_EmailAlreadyExists() {
        SignupRequest request = new SignupRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        User user = new User();
        user.setId(1L);
        user.setOwnerUserId("C00001");
        user.setEmail("test@example.com");
        user.setPasswordHash("encoded-password");
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRoles("CUSTOMER");

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        var response = authService.login(request);

        assertNotNull(response);

        // ✅ FIX 2: Use getAccessToken() instead of getToken()
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        assertEquals(user.getOwnerUserId(), response.getOwnerUserId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRoles(), response.getRoles());
    }

    @Test
    void testLogin_InvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}