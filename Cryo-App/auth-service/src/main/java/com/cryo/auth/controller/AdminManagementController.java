package com.cryo.auth.controller;

import com.cryo.auth.dto.SignupRequest;
import com.cryo.auth.entity.User;
import com.cryo.auth.repository.UserRepository;
import com.cryo.common.dto.ApiResponse;
import com.cryo.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/admin")
public class AdminManagementController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminManagementController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ✅ SECURE ENDPOINT: Creates a new Admin user
     * 🔒 RESTRICTION: Only 'ROLE_ROOT' can access this.
     * This enforces the hierarchy: Root -> Admin -> Customer
     */
    @PostMapping("/create-staff")
    // @PreAuthorize("hasRole('ROOT')") // Uncomment if you have @EnableMethodSecurity in config
    public ResponseEntity<ApiResponse<String>> createStaffAdmin(@Valid @RequestBody SignupRequest request) {

        // 1. 🛡️ SECURITY CHECK: Verify the caller is ROOT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if the current logged-in user has ROLE_ROOT
        boolean isRoot = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROOT") || a.getAuthority().equals("ROLE_ROOT"));
        if (!isRoot) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Only Root Admin can create new Admins."));
        }

        // 2. Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email already exists"));
        }

        // 3. Create the Admin User
        User newAdmin = new User();
        newAdmin.setName(request.getName());
        newAdmin.setEmail(request.getEmail());
        newAdmin.setMobileNumber(request.getMobileNumber());
        newAdmin.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // ✅ KEY LOGIC: Force Role to ADMIN
        // This user is now a "Manager" but cannot create other Admins (because they lack ROLE_ROOT)
        newAdmin.setRoles("ADMIN");

        // Admins are activated immediately (Root verified them personally)
        newAdmin.setStatus(User.UserStatus.ACTIVE);

        // 4. Generate Admin ID
        // We use "A" prefix + Timestamp to differentiate from Customers (C0001)
        String adminId = "A" + System.currentTimeMillis();
        newAdmin.setOwnerUserId(adminId);

        userRepository.save(newAdmin);

        return ResponseEntity.ok(ApiResponse.success("New Staff Admin created successfully.", adminId));
    }
    // ✅ SECURE ENDPOINT: Deactivate/Fire a Staff Admin
    // 🔒 RESTRICTION: Only 'ROOT' can do this.
    @PutMapping("/deactivate-staff/{email}")
    public ResponseEntity<ApiResponse<String>> deactivateStaff(@PathVariable("email") String email) {

        // 1. 🛡️ SECURITY CHECK: Verify caller is ROOT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isRoot = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROOT") || a.getAuthority().equals("ROLE_ROOT"));

        if (!isRoot) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Only Root Admin can manage staff."));
        }

        // 2. Find the Staff Member
        User staffUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        // 3. 🚨 SAFETY CHECK: Don't let Root delete themselves!
        if (staffUser.getEmail().equals(auth.getName())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("You cannot deactivate yourself!"));
        }

        // 4. Update Status to INACTIVE (Soft Delete)
        staffUser.setStatus(User.UserStatus.INACTIVE); // or BLOCKED
        userRepository.save(staffUser);

        return ResponseEntity.ok(ApiResponse.success("Staff member access revoked successfully.", null));
    }
}