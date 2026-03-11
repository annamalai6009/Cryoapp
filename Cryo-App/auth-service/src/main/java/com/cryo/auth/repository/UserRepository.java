package com.cryo.auth.repository;
import com.cryo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    // ✅ KEEPING YOUR OLD METHOD (As requested)
    @Query(value = "select u.owner_user_id from users u order By u.id desc limit 1", nativeQuery = true)
    public Optional<String> findByUserId();

    Optional<User> findByOwnerUserId(String ownerUserId);

    Optional<User> findByMobileNumber(String mobileNumber);

    // ✅ NEW FIX 1: Find the last CUSTOMER ID (Starts with 'C')
    @Query(value = "SELECT owner_user_id FROM users WHERE owner_user_id LIKE 'C%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastCustomerUserId();

    // ✅ NEW FIX 2: Find the last ADMIN ID (Starts with 'A')
    @Query(value = "SELECT owner_user_id FROM users WHERE owner_user_id LIKE 'A%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastAdminUserId();
}